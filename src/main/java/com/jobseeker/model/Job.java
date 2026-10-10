package com.jobseeker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String company;

    @NotBlank
    private String location;

    @NotNull
    @Enumerated(EnumType.STRING)
    private JobType type = JobType.FULL_TIME;

    private boolean remote;

    @PositiveOrZero
    private Integer salaryMin;

    @PositiveOrZero
    private Integer salaryMax;

    @NotBlank
    @Column(length = 5000)
    private String description;

    private LocalDate postedOn = LocalDate.now();

    private boolean saved;

    public Job() {}

    public Job(String title, String company, String location, JobType type, boolean remote,
               Integer salaryMin, Integer salaryMax, String description, LocalDate postedOn) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.type = type;
        this.remote = remote;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.description = description;
        this.postedOn = postedOn;
    }

    public String getSalaryRange() {
        if (salaryMin == null && salaryMax == null) return "Not disclosed";
        if (salaryMax == null) return "₹" + lakhs(salaryMin) + "+ LPA";
        if (salaryMin == null) return "Up to ₹" + lakhs(salaryMax) + " LPA";
        return "₹" + lakhs(salaryMin) + "–" + lakhs(salaryMax) + " LPA";
    }

    private static String lakhs(int rupees) {
        double l = rupees / 100_000.0;
        return l == Math.floor(l) ? String.valueOf((long) l) : String.format("%.1f", l);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public JobType getType() { return type; }
    public void setType(JobType type) { this.type = type; }
    public boolean isRemote() { return remote; }
    public void setRemote(boolean remote) { this.remote = remote; }
    public Integer getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Integer salaryMin) { this.salaryMin = salaryMin; }
    public Integer getSalaryMax() { return salaryMax; }
    public void setSalaryMax(Integer salaryMax) { this.salaryMax = salaryMax; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getPostedOn() { return postedOn; }
    public void setPostedOn(LocalDate postedOn) { this.postedOn = postedOn; }
    public boolean isSaved() { return saved; }
    public void setSaved(boolean saved) { this.saved = saved; }
}
