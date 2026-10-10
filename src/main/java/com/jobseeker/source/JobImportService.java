package com.jobseeker.source;

import com.jobseeker.model.Job;
import com.jobseeker.repo.ApplicationRepository;
import com.jobseeker.repo.JobRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** Keeps imported listings in sync with every registered {@link JobSource}. */
@Service
public class JobImportService {

    private static final Logger log = LoggerFactory.getLogger(JobImportService.class);

    private final List<JobSource> sources;
    private final JobRepository jobs;
    private final ApplicationRepository applications;
    private final SourceSyncRepository syncs;
    private final Clock clock;

    public JobImportService(List<JobSource> sources, JobRepository jobs,
                            ApplicationRepository applications, SourceSyncRepository syncs, Clock clock) {
        this.sources = sources;
        this.jobs = jobs;
        this.applications = applications;
        this.syncs = syncs;
        this.clock = clock;
    }

    /** Runs shortly after startup and then every 15 minutes, but only fetches sources that are due. */
    @Scheduled(initialDelayString = "PT3S", fixedDelayString = "PT15M")
    public void refreshDueSources() {
        for (JobSource source : sources) {
            if (isDue(source)) {
                refresh(source);
            }
        }
    }

    boolean isDue(JobSource source) {
        return syncs.findById(source.name())
                .map(SourceSync::getLastAttempt)
                .map(last -> !Instant.now(clock).isBefore(last.plus(source.refreshInterval())))
                .orElse(true);
    }

    public void refresh(JobSource source) {
        SourceSync sync = syncs.findById(source.name()).orElseGet(() -> new SourceSync(source.name()));
        // Record the attempt up front so a failing API isn't retried on every tick.
        sync.setLastAttempt(Instant.now(clock));
        try {
            List<ExternalJob> fetched = source.fetchJobs();
            sync.setLastResult(sync(source.name(), fetched));
            log.info("{}: {}", source.name(), sync.getLastResult());
        } catch (RuntimeException e) {
            sync.setLastResult("failed: " + e.getMessage());
            log.warn("Could not fetch jobs from {}: {}", source.name(), e.toString());
        }
        syncs.save(sync);
    }

    /** Upserts fetched listings and removes ones the source dropped, unless you saved or applied to them. */
    String sync(String sourceName, List<ExternalJob> fetched) {
        Map<String, Job> existing = jobs.findBySource(sourceName).stream()
                .collect(Collectors.toMap(Job::getExternalId, Function.identity(), (a, b) -> a));
        Set<String> seen = new HashSet<>();
        List<Job> toSave = new ArrayList<>();
        int added = 0;
        for (ExternalJob ext : fetched) {
            if (ext.externalId() == null || ext.title() == null || ext.title().isBlank()
                    || ext.company() == null || ext.company().isBlank()
                    || !seen.add(ext.externalId())) {
                continue;
            }
            Job job = existing.get(ext.externalId());
            if (job == null) {
                job = new Job();
                job.setSource(sourceName);
                job.setExternalId(ext.externalId());
                added++;
            }
            apply(ext, job);
            toSave.add(job);
        }
        jobs.saveAll(toSave);

        List<Job> stale = existing.values().stream()
                .filter(job -> !seen.contains(job.getExternalId()))
                .filter(job -> !job.isSaved() && !applications.existsByJobId(job.getId()))
                .toList();
        jobs.deleteAll(stale);
        return String.format("%d listings (%d new, %d removed)", toSave.size(), added, stale.size());
    }

    private static void apply(ExternalJob ext, Job job) {
        job.setTitle(truncate(ext.title(), 255));
        job.setCompany(truncate(ext.company(), 255));
        job.setLocation(truncate(ext.location(), 255));
        job.setType(ext.type());
        job.setRemote(ext.remote());
        job.setSalaryText(truncate(ext.salaryText(), 255));
        job.setSalaryMin(null);
        job.setSalaryMax(null);
        job.setCompanyLogo(truncate(ext.companyLogo(), 1000));
        job.setApplyUrl(truncate(ext.applyUrl(), 1000));
        job.setPostedOn(ext.postedOn());
        String html = HtmlSanitizer.clean(ext.descriptionHtml());
        job.setDescriptionHtml(html);
        // Plain-text copy keeps keyword search working; the column is capped at 5000 chars.
        String text = truncate(HtmlSanitizer.toText(html), 5000);
        job.setDescription(text != null ? text : "See the original listing for details.");
    }

    private static String truncate(String value, int max) {
        if (value == null || value.isBlank()) return null;
        String trimmed = value.strip();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max - 1) + "…";
    }
}
