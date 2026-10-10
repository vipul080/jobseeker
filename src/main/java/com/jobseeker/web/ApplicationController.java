package com.jobseeker.web;

import com.jobseeker.model.ApplicationStatus;
import com.jobseeker.repo.ApplicationRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationRepository applications;

    public ApplicationController(ApplicationRepository applications) {
        this.applications = applications;
    }

    @GetMapping
    public String list(Model model) {
        Map<ApplicationStatus, Long> counts = new LinkedHashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            counts.put(status, applications.countByStatus(status));
        }
        model.addAttribute("applications", applications.findAllByOrderByAppliedOnDescIdDesc());
        model.addAttribute("counts", counts);
        model.addAttribute("statuses", ApplicationStatus.values());
        return "applications/list";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam ApplicationStatus status,
                         @RequestParam(defaultValue = "") String notes,
                         RedirectAttributes flash) {
        var application = applications.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        application.setStatus(status);
        application.setNotes(notes.isBlank() ? null : notes.trim());
        applications.save(application);
        flash.addFlashAttribute("message", "Updated " + application.getJob().getTitle() + ".");
        return "redirect:/applications";
    }

    @PostMapping("/{id}/delete")
    public String withdraw(@PathVariable Long id, RedirectAttributes flash) {
        applications.deleteById(id);
        flash.addFlashAttribute("message", "Application withdrawn.");
        return "redirect:/applications";
    }
}
