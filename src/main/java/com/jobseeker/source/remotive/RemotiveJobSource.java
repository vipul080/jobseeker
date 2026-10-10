package com.jobseeker.source.remotive;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jobseeker.model.JobType;
import com.jobseeker.source.ExternalJob;
import com.jobseeker.source.JobSource;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Remote jobs from Remotive's public API (https://remotive.com/api-documentation).
 * Their terms: credit Remotive, link back to each job's Remotive URL, and fetch at most ~4 times a day.
 */
@Component
@ConditionalOnProperty(name = "jobs.remotive.enabled", havingValue = "true", matchIfMissing = true)
public class RemotiveJobSource implements JobSource {

    private final RestClient http;
    private final String url;
    private final String category;

    @Autowired
    public RemotiveJobSource(@Value("${jobs.remotive.url:https://remotive.com/api/remote-jobs}") String url,
                             @Value("${jobs.remotive.category:}") String category) {
        this(RestClient.builder(), url, category);
    }

    RemotiveJobSource(RestClient.Builder builder, String url, String category) {
        this.http = builder.defaultHeader("User-Agent", "jobseeker-app").build();
        this.url = url;
        this.category = category;
    }

    @Override
    public String name() {
        return "Remotive";
    }

    @Override
    public Duration refreshInterval() {
        return Duration.ofHours(6);
    }

    @Override
    public List<ExternalJob> fetchJobs() {
        var uri = UriComponentsBuilder.fromUriString(url);
        if (!category.isBlank()) uri.queryParam("category", category);
        Response response = http.get().uri(uri.build().toUri()).retrieve().body(Response.class);
        if (response == null || response.jobs() == null) return List.of();
        return response.jobs().stream().map(RemotiveJobSource::toExternal).toList();
    }

    static ExternalJob toExternal(RemotiveJob j) {
        return new ExternalJob(
                String.valueOf(j.id()),
                j.title(),
                j.companyName(),
                null, // Remotive's logo URLs return 403 when embedded on other sites

                j.location() == null || j.location().isBlank() ? "Worldwide" : j.location(),
                mapType(j.jobType()),
                true,
                j.salary(),
                j.description(),
                j.url(),
                parseDate(j.publicationDate()));
    }

    static JobType mapType(String remotiveType) {
        if (remotiveType == null) return JobType.FULL_TIME;
        return switch (remotiveType) {
            case "part_time" -> JobType.PART_TIME;
            case "contract", "freelance" -> JobType.CONTRACT;
            case "internship" -> JobType.INTERNSHIP;
            default -> JobType.FULL_TIME;
        };
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDateTime.parse(value).toLocalDate();
        } catch (DateTimeParseException | NullPointerException e) {
            return LocalDate.now();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Response(List<RemotiveJob> jobs) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RemotiveJob(
            long id,
            String url,
            String title,
            @JsonProperty("company_name") String companyName,
            @JsonProperty("job_type") String jobType,
            @JsonProperty("publication_date") String publicationDate,
            @JsonProperty("candidate_required_location") String location,
            String salary,
            String description) {}
}
