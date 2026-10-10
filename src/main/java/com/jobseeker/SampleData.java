package com.jobseeker;

import com.jobseeker.model.Job;
import com.jobseeker.model.JobType;
import com.jobseeker.repo.JobRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Seeds a few listings so the board isn't empty on first run. */
@Component
public class SampleData implements CommandLineRunner {

    private final JobRepository jobs;

    public SampleData(JobRepository jobs) {
        this.jobs = jobs;
    }

    @Override
    public void run(String... args) {
        if (jobs.count() > 0) return;
        LocalDate today = LocalDate.now();
        jobs.saveAll(List.of(
                new Job("Backend Engineer (Java)", "Northwind Labs", "Bengaluru", JobType.FULL_TIME, false,
                        1800000, 2800000,
                        "Build and scale Spring Boot microservices for our payments platform. "
                                + "You'll own APIs end to end, from design to on-call.\n\n"
                                + "Requirements: 3+ years Java, Spring Boot, SQL, REST. Kafka is a plus.",
                        today.minusDays(1)),
                new Job("Frontend Developer", "Brightpath", "Remote", JobType.FULL_TIME, true,
                        1200000, 2000000,
                        "Craft fast, accessible interfaces in React and TypeScript for a learning platform "
                                + "used by two million students.",
                        today.minusDays(2)),
                new Job("Data Analyst Intern", "Cobalt Retail", "Mumbai", JobType.INTERNSHIP, false,
                        300000, 400000,
                        "Six-month internship working with SQL, Python and dashboards to answer real "
                                + "business questions. Final-year students welcome.",
                        today.minusDays(3)),
                new Job("DevOps Engineer", "Skyforge Cloud", "Hyderabad", JobType.CONTRACT, true,
                        null, null,
                        "Twelve-month contract to migrate CI/CD to GitHub Actions and run Kubernetes "
                                + "clusters on AWS. Terraform experience required.",
                        today.minusDays(5)),
                new Job("Android Developer", "Pocketly", "Pune", JobType.FULL_TIME, false,
                        1500000, null,
                        "Ship features in Kotlin and Jetpack Compose for a personal finance app with a "
                                + "4.7-star rating.",
                        today.minusDays(6)),
                new Job("QA Automation Engineer", "Northwind Labs", "Remote", JobType.PART_TIME, true,
                        600000, 900000,
                        "Write and maintain Selenium and REST-assured suites. 20–25 hours a week, "
                                + "flexible schedule.",
                        today.minusDays(9))));
    }
}
