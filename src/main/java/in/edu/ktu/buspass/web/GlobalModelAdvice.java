package in.edu.ktu.buspass.web;

import in.edu.ktu.buspass.domain.Student;
import in.edu.ktu.buspass.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private final StudentService studentService;

    public GlobalModelAdvice(StudentService studentService) {
        this.studentService = studentService;
    }

    /** Makes the signed-in student available to every template (null when signed out). */
    @ModelAttribute("currentStudent")
    public Student currentStudent() {
        return studentService.currentStudent();
    }

    @ModelAttribute("today")
    public LocalDate today() {
        return LocalDate.now();
    }

    /** Used by the layout to highlight the current nav item. */
    @ModelAttribute("activeNav")
    public String activeNav(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.equals("/pass/apply")) {
            return "apply";
        }
        if (uri.equals("/dashboard")) {
            return "dashboard";
        }
        if (uri.equals("/passes") || uri.startsWith("/pass/")) {
            return "passes";
        }
        if (uri.startsWith("/admin")) {
            return "admin";
        }
        if (uri.equals("/profile")) {
            return "profile";
        }
        return "";
    }
}
