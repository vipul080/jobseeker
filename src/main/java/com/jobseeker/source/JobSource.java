package com.jobseeker.source;

import java.time.Duration;
import java.util.List;

/**
 * An outside job board. Add a new board by implementing this as a Spring bean;
 * {@link JobImportService} picks it up and keeps its listings in sync.
 */
public interface JobSource {

    /** Display name, also stored on each imported job (e.g. "Remotive"). Must stay stable. */
    String name();

    /** Minimum time between fetches. Respect the provider's rate limits here. */
    Duration refreshInterval();

    List<ExternalJob> fetchJobs();
}
