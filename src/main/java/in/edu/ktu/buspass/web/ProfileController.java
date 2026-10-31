package in.edu.ktu.buspass.web;

import in.edu.ktu.buspass.domain.Student;
import in.edu.ktu.buspass.exception.BusinessException;
import in.edu.ktu.buspass.service.StudentService;
import in.edu.ktu.buspass.web.dto.ChangePasswordForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final StudentService studentService;

    public ProfileController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new ChangePasswordForm());
        }
        model.addAttribute("student", studentService.currentStudent());
        return "profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(@Valid @ModelAttribute("passwordForm") ChangePasswordForm form,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Student student = studentService.currentStudent();
        model.addAttribute("student", student);
        if (bindingResult.hasErrors()) {
            return "profile";
        }
        try {
            studentService.changePassword(student, form);
        } catch (BusinessException ex) {
            bindingResult.reject("password", ex.getMessage());
            return "profile";
        }
        redirectAttributes.addFlashAttribute("success", "Password updated. Use it the next time you sign in.");
        return "redirect:/profile";
    }
}
