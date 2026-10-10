package com.jobseeker.source;

import com.jobseeker.model.JobType;
import java.time.LocalDate;

/** A listing fetched from an outside job board, normalized so every {@link JobSource} looks the same. */
public record ExternalJob(
        String externalId,
        String title,
        String company,
        String companyLogo,
        String location,
        JobType type,
        boolean remote,
        String salaryText,
        String descriptionHtml,
        String applyUrl,
        LocalDate postedOn) {}
