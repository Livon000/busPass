package in.edu.ktu.buspass.web;

import in.edu.ktu.buspass.domain.PassApplication;
import in.edu.ktu.buspass.service.PassService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class VerifyController {

    private final PassService passService;

    public VerifyController(PassService passService) {
        this.passService = passService;
    }

    @GetMapping("/verify/{reference}")
    public String verify(@PathVariable String reference, Model model) {
        PassApplication pass = passService.verify(reference)
                .orElseThrow(() -> new EntityNotFoundException("No pass found for " + reference));
        model.addAttribute("pass", pass);
        model.addAttribute("valid", pass.isActive());
        return "verify";
    }

    @GetMapping("/verify")
    public String verifyForm(@RequestParam(required = false) String reference, Model model) {
        if (reference != null && !reference.isBlank()) {
            return "redirect:/verify/" + reference.trim();
        }
        return "verify";
    }
}
