package com.jobseeker.source.remotive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.jobseeker.model.JobType;
import com.jobseeker.source.ExternalJob;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RemotiveJobSourceTest {

    private static final String RESPONSE = """
            {"job-count": 2, "jobs": [
              {"id": 101, "url": "https://remotive.com/remote-jobs/software-development/java-dev-101",
               "title": "Java Developer", "company_name": "Acme", "company_logo": "https://remotive.com/job/101/logo",
               "category": "Software Development", "tags": ["java"], "job_type": "freelance",
               "publication_date": "2026-10-08T21:47:32", "candidate_required_location": "India",
               "salary": "$40k - $60k", "description": "<p>Build <b>things</b></p>"},
              {"id": 102, "url": "https://remotive.com/remote-jobs/x-102", "title": "Writer", "company_name": "Inkly",
               "company_logo": "", "job_type": "part_time", "publication_date": "not-a-date",
               "candidate_required_location": "", "salary": "", "description": ""}
            ]}""";

    @Test
    void mapsRemotiveResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://remotive.test/api?category=software-dev"))
                .andRespond(withSuccess(RESPONSE, MediaType.APPLICATION_JSON));

        List<ExternalJob> jobs = new RemotiveJobSource(builder, "https://remotive.test/api", "software-dev").fetchJobs();

        server.verify();
        assertThat(jobs).hasSize(2);
        ExternalJob java = jobs.get(0);
        assertThat(java.externalId()).isEqualTo("101");
        assertThat(java.company()).isEqualTo("Acme");
        assertThat(java.type()).isEqualTo(JobType.CONTRACT);
        assertThat(java.location()).isEqualTo("India");
        assertThat(java.salaryText()).isEqualTo("$40k - $60k");
        assertThat(java.postedOn()).isEqualTo(LocalDate.of(2026, 10, 8));
        assertThat(java.applyUrl()).endsWith("java-dev-101");
        assertThat(java.remote()).isTrue();

        ExternalJob writer = jobs.get(1);
        assertThat(writer.type()).isEqualTo(JobType.PART_TIME);
        assertThat(writer.location()).isEqualTo("Worldwide");
        assertThat(writer.postedOn()).isNotNull();
    }
}
