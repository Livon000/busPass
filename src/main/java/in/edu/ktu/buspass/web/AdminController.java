package in.edu.ktu.buspass.web;

import in.edu.ktu.buspass.domain.PassStatus;
import in.edu.ktu.buspass.exception.BusinessException;
import in.edu.ktu.buspass.service.PassService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final PassService passService;

    public AdminController(PassService passService) {
        this.passService = passService;
    }

    @GetMapping("/admin")
    public String admin(@RequestParam(required = false) PassStatus status, Model model) {
        PassStatus filter = status == null ? PassStatus.PENDING : status;
        model.addAttribute("filter", filter);
        model.addAttribute("statuses", PassStatus.values());
        model.addAttribute("applications",
                filter == null ? passService.all() : passService.byStatus(filter));
        model.addAttribute("pendingCount", passService.count(PassStatus.PENDING));
        model.addAttribute("approvedCount", passService.count(PassStatus.APPROVED));
        model.addAttribute("rejectedCount", passService.count(PassStatus.REJECTED));
        model.addAttribute("totalCount", passService.all().size());
        return "admin/applications";
    }

    @PostMapping("/admin/passes/{id}/approve")
    public String approve(@PathVariable Long id,
                          @RequestParam(required = false) String remarks,
                          RedirectAttributes redirectAttributes) {
        try {
            passService.approve(id, remarks);
            redirectAttributes.addFlashAttribute("success", "Application approved.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin?status=PENDING";
    }

    @PostMapping("/admin/passes/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(required = false) String remarks,
                         RedirectAttributes redirectAttributes) {
        try {
            passService.reject(id, remarks);
            redirectAttributes.addFlashAttribute("success", "Application rejected.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin?status=PENDING";
    }
}
