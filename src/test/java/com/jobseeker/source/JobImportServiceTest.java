package com.jobseeker.source;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobseeker.model.Application;
import com.jobseeker.model.Job;
import com.jobseeker.model.JobType;
import com.jobseeker.repo.ApplicationRepository;
import com.jobseeker.repo.JobRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:import", "jobs.remotive.enabled=false"})
class JobImportServiceTest {

    static class FakeSource implements JobSource {
        List<ExternalJob> next = new ArrayList<>();
        int calls;

        public String name() { return "Fake"; }
        public Duration refreshInterval() { return Duration.ofHours(6); }
        public List<ExternalJob> fetchJobs() { calls++; return next; }
    }

    @TestConfiguration
    static class Config {
        @Bean
        FakeSource fakeSource() { return new FakeSource(); }
    }

    @Autowired FakeSource source;
    @Autowired JobImportService importer;
    @Autowired JobRepository jobs;
    @Autowired ApplicationRepository applications;
    @Autowired SourceSyncRepository syncs;

    @BeforeEach
    void reset() {
        applications.deleteAll();
        jobs.deleteAll();
        syncs.deleteAll();
        source.calls = 0;
    }

    private static ExternalJob ext(String id, String title, String html) {
        return new ExternalJob(id, title, "Co " + id, null, "Worldwide", JobType.FULL_TIME, true,
                "$100k", html, "https://example.com/" + id, LocalDate.of(2026, 10, 1));
    }

    @Test
    void importsUpdatesAndPrunesListings() {
        source.next = List.of(ext("1", "Dev", "<p>Hi</p>"), ext("2", "Ops", "<p>x</p>"),
                ext("3", "QA", "<p>y</p>"), ext("4", "", "<p>no title, skipped</p>"));
        importer.refresh(source);
        assertThat(jobs.findBySource("Fake")).hasSize(3);

        Job saved = jobs.findBySource("Fake").stream().filter(j -> j.getExternalId().equals("2")).findFirst().orElseThrow();
        saved.setSaved(true);
        jobs.save(saved);
        Job appliedTo = jobs.findBySource("Fake").stream().filter(j -> j.getExternalId().equals("3")).findFirst().orElseThrow();
        Application app = new Application();
        app.setJob(appliedTo);
        applications.save(app);

        // Source now only lists job 1 (renamed). 2 is saved and 3 has an application, so both are kept.
        source.next = List.of(ext("1", "Senior Dev", "<p>Hi</p>"));
        importer.refresh(source);

        assertThat(jobs.findBySource("Fake")).extracting(Job::getExternalId).containsExactlyInAnyOrder("1", "2", "3");
        assertThat(jobs.findBySource("Fake")).extracting(Job::getTitle).contains("Senior Dev");

        applications.deleteAll();
        saved.setSaved(false);
        jobs.save(saved);
        importer.refresh(source);
        assertThat(jobs.findBySource("Fake")).extracting(Job::getExternalId).containsExactly("1");
    }

    @Test
    void sanitizesDescriptions() {
        source.next = List.of(ext("9", "Dev", "<p onclick=\"x()\">Hello <script>alert(1)</script><a href=\"https://a.b\">link</a></p>"));
        importer.refresh(source);
        Job job = jobs.findBySource("Fake").get(0);
        assertThat(job.getDescriptionHtml()).doesNotContain("script", "onclick").contains("rel=\"nofollow noopener noreferrer\"");
        assertThat(job.getDescription()).isEqualTo("Hello link");
    }

    @Test
    void onlyFetchesWhenDue() {
        importer.refreshDueSources();
        importer.refreshDueSources();
        assertThat(source.calls).isEqualTo(1);
    }
}
