package com.jobseeker.source;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;

/** When each source was last fetched, persisted so restarts don't trigger extra API calls. */
@Entity
public class SourceSync {

    @Id
    private String source;

    private Instant lastAttempt;

    private String lastResult;

    protected SourceSync() {}

    public SourceSync(String source) {
        this.source = source;
    }

    public String getSource() { return source; }
    public Instant getLastAttempt() { return lastAttempt; }
    public void setLastAttempt(Instant lastAttempt) { this.lastAttempt = lastAttempt; }
    public String getLastResult() { return lastResult; }
    public void setLastResult(String lastResult) { this.lastResult = lastResult; }
}
