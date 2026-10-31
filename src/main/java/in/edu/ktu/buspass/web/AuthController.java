package in.edu.ktu.buspass.web;

import in.edu.ktu.buspass.exception.BusinessException;
import in.edu.ktu.buspass.service.StudentService;
import in.edu.ktu.buspass.web.dto.RegisterForm;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    static final List<String> DEPARTMENTS = List.of(
            "Computer Science",
            "Information Technology",
            "Electronics",
            "Electrical",
            "Mechanical",
            "Civil",
            "AI & Data Science",
            "Other");

    private final StudentService studentService;

    public AuthController(StudentService studentService) {
        this.studentService = studentService;
    }

    private boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        return isLoggedIn(authentication) ? "redirect:/dashboard" : "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model, Authentication authentication) {
        if (isLoggedIn(authentication)) {
            return "redirect:/dashboard";
        }
        model.addAttribute("registerForm", new RegisterForm());
        model.addAttribute("departments", DEPARTMENTS);
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        model.addAttribute("departments", DEPARTMENTS);
        if (!bindingResult.hasErrors() && !form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "The two passwords do not match");
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            studentService.register(form);
        } catch (BusinessException ex) {
            bindingResult.reject("register", ex.getMessage());
            return "register";
        }
        redirectAttributes.addFlashAttribute("success",
                "Account created. Sign in with your KTU register number.");
        return "redirect:/login";
    }
}
