package in.edu.ktu.buspass.web;

import in.edu.ktu.buspass.domain.PassApplication;
import in.edu.ktu.buspass.domain.Student;
import in.edu.ktu.buspass.service.PassService;
import in.edu.ktu.buspass.service.StudentService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final StudentService studentService;
    private final PassService passService;

    public DashboardController(StudentService studentService, PassService passService) {
        this.studentService = studentService;
        this.passService = passService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Student student = studentService.currentStudent();
        if (student == null) {
            return "redirect:/login";
        }
        List<PassApplication> history = passService.history(student);
        model.addAttribute("student", student);
        model.addAttribute("currentPass", passService.currentPass(student).orElse(null));
        model.addAttribute("pendingPass", passService.pendingPass(student).orElse(null));
        model.addAttribute("recentPasses", history.stream().limit(5).toList());
        model.addAttribute("routes", passService.routes());
        model.addAttribute("hasOpenPass", passService.hasOpenPass(student));
        return "dashboard";
    }
}
