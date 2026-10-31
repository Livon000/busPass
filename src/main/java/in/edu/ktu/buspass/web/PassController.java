package in.edu.ktu.buspass.web;

import in.edu.ktu.buspass.domain.PassApplication;
import in.edu.ktu.buspass.domain.Student;
import in.edu.ktu.buspass.exception.BusinessException;
import in.edu.ktu.buspass.service.PassService;
import in.edu.ktu.buspass.service.QrCodeService;
import in.edu.ktu.buspass.service.StudentService;
import in.edu.ktu.buspass.web.dto.PassApplicationForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
public class PassController {

    private final StudentService studentService;
    private final PassService passService;
    private final QrCodeService qrCodeService;

    public PassController(StudentService studentService,
                          PassService passService,
                          QrCodeService qrCodeService) {
        this.studentService = studentService;
        this.passService = passService;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/passes")
    public String passes(Model model) {
        Student student = studentService.currentStudent();
        model.addAttribute("passes", passService.history(student));
        return "passes";
    }

    @GetMapping("/pass/apply")
    public String applyPage(Model model, RedirectAttributes redirectAttributes) {
        Student student = studentService.currentStudent();
        if (student.isAdmin()) {
            return "redirect:/admin";
        }
        if (passService.hasOpenPass(student)) {
            redirectAttributes.addFlashAttribute("error",
                    "You already have a pending or valid pass. You can apply again once it expires.");
            return "redirect:/dashboard";
        }
        if (!model.containsAttribute("passForm")) {
            model.addAttribute("passForm", new PassApplicationForm());
        }
        addApplyModel(model);
        return "apply";
    }

    @PostMapping("/pass/apply")
    public String apply(@Valid @ModelAttribute("passForm") PassApplicationForm form,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        Student student = studentService.currentStudent();
        if (student.isAdmin()) {
            return "redirect:/admin";
        }
        addApplyModel(model);
        if (bindingResult.hasErrors()) {
            return "apply";
        }
        PassApplication pass;
        try {
            pass = passService.apply(student, form);
        } catch (BusinessException ex) {
            bindingResult.reject("apply", ex.getMessage());
            return "apply";
        }
        redirectAttributes.addFlashAttribute("success",
                "Application submitted. Your reference is " + pass.getReference() + ".");
        return "redirect:/pass/" + pass.getId();
    }

    @GetMapping("/pass/{id}")
    public String pass(@PathVariable Long id, Model model) {
        Student student = studentService.currentStudent();
        PassApplication pass = passService.getForStudent(student, id);
        String verifyUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/verify/{reference}")
                .buildAndExpand(pass.getReference())
                .toUriString();
        model.addAttribute("pass", pass);
        model.addAttribute("verifyUrl", verifyUrl);
        model.addAttribute("qrCode", qrCodeService.dataUri(verifyUrl, 260));
        model.addAttribute("active", pass.isActive());
        return "pass";
    }

    @PostMapping("/pass/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Student student = studentService.currentStudent();
        try {
            passService.cancel(student, id);
            redirectAttributes.addFlashAttribute("success", "Application cancelled.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/passes";
    }

    private void addApplyModel(Model model) {
        model.addAttribute("routes", passService.routes());
        model.addAttribute("passTypes", passService.passTypes());
    }
}
