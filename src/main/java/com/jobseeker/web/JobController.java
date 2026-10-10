package com.jobseeker.web;

import com.jobseeker.model.Application;
import com.jobseeker.model.Job;
import com.jobseeker.model.JobType;
import com.jobseeker.repo.ApplicationRepository;
import com.jobseeker.repo.JobRepository;
import jakarta.validation.groups.Default;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class JobController {

    private final JobRepository jobs;
    private final ApplicationRepository applications;

    public JobController(JobRepository jobs, ApplicationRepository applications) {
        this.jobs = jobs;
        this.applications = applications;
    }

    /** Keep ids server-assigned: otherwise /jobs/{id}/apply would bind the job id onto the new Application. */
    @InitBinder
    void disallowId(WebDataBinder binder) {
        binder.setDisallowedFields("id", "saved", "source", "externalId", "applyUrl",
                "companyLogo", "salaryText", "descriptionHtml");
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/jobs";
    }

    @GetMapping("/jobs")
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "") String location,
                       @RequestParam(required = false) JobType type,
                       @RequestParam(defaultValue = "false") boolean remote,
                       Model model) {
        model.addAttribute("jobs", jobs.search(q.trim(), location.trim(), type, remote));
        model.addAttribute("q", q);
        model.addAttribute("location", location);
        model.addAttribute("type", type);
        model.addAttribute("remote", remote);
        model.addAttribute("types", JobType.values());
        return "jobs/list";
    }

    @GetMapping("/jobs/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("job", find(id));
        model.addAttribute("alreadyApplied", applications.existsByJobId(id));
        if (!model.containsAttribute("application")) {
            model.addAttribute("application", new Application());
        }
        return "jobs/detail";
    }

    @PostMapping("/jobs/{id}/apply")
    public String apply(@PathVariable Long id,
                        @Validated({Application.DirectApply.class, Default.class})
                        @ModelAttribute("application") Application application,
                        BindingResult result,
                        Model model,
                        RedirectAttributes flash) {
        Job job = find(id);
        if (job.isExternal()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apply to external jobs on the source site");
        }
        if (result.hasErrors()) {
            model.addAttribute("job", job);
            model.addAttribute("alreadyApplied", applications.existsByJobId(id));
            return "jobs/detail";
        }
        application.setJob(job);
        applications.save(application);
        flash.addFlashAttribute("message", "Application sent to " + job.getCompany() + ".");
        return "redirect:/applications";
    }

    /** For external listings: you apply on the source site, then record it here to track it. */
    @PostMapping("/jobs/{id}/track")
    public String track(@PathVariable Long id, RedirectAttributes flash) {
        Job job = find(id);
        if (!applications.existsByJobId(id)) {
            Application application = new Application();
            application.setJob(job);
            applications.save(application);
        }
        flash.addFlashAttribute("message", "Tracking your application to " + job.getCompany() + ".");
        return "redirect:/applications";
    }

    @PostMapping("/jobs/{id}/save")
    public String toggleSave(@PathVariable Long id,
                             @RequestHeader(value = "Referer", required = false) String referer) {
        Job job = find(id);
        job.setSaved(!job.isSaved());
        jobs.save(job);
        return "redirect:" + (referer != null && referer.contains("/saved") ? "/saved" : "/jobs/" + id);
    }

    @GetMapping("/saved")
    public String saved(Model model) {
        model.addAttribute("jobs", jobs.findBySavedTrueOrderByPostedOnDesc());
        return "jobs/saved";
    }

    @GetMapping("/jobs/new")
    public String newJob(Model model) {
        model.addAttribute("job", new Job());
        model.addAttribute("types", JobType.values());
        return "jobs/form";
    }

    @PostMapping("/jobs")
    public String create(@Validated @ModelAttribute("job") Job job, BindingResult result,
                         Model model, RedirectAttributes flash) {
        if (job.getSalaryMin() != null && job.getSalaryMax() != null
                && job.getSalaryMin() > job.getSalaryMax()) {
            result.rejectValue("salaryMax", "range", "Must be at least the minimum salary");
        }
        if (result.hasErrors()) {
            model.addAttribute("types", JobType.values());
            return "jobs/form";
        }
        Job savedJob = jobs.save(job);
        flash.addFlashAttribute("message", "Job posted.");
        return "redirect:/jobs/" + savedJob.getId();
    }

    private Job find(Long id) {
        return jobs.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
    }
}
