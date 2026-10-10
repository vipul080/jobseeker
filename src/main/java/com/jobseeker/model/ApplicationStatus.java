package com.jobseeker.model;

public enum ApplicationStatus {
    APPLIED("Applied"), INTERVIEW("Interview"), OFFER("Offer"), REJECTED("Rejected");

    private final String label;

    ApplicationStatus(String label) { this.label = label; }

    public String getLabel() { return label; }
}
