package in.edu.ktu.buspass.service;

import in.edu.ktu.buspass.domain.Role;
import in.edu.ktu.buspass.domain.Student;
import in.edu.ktu.buspass.exception.BusinessException;
import in.edu.ktu.buspass.repository.StudentRepository;
import in.edu.ktu.buspass.web.dto.ChangePasswordForm;
import in.edu.ktu.buspass.web.dto.RegisterForm;
import java.util.Locale;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService implements UserDetailsService {

    private final StudentRepository students;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository students, PasswordEncoder passwordEncoder) {
        this.students = students;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String registerNo) throws UsernameNotFoundException {
        Student student = students.findByRegisterNoIgnoreCase(registerNo == null ? "" : registerNo.trim())
                .orElseThrow(() -> new UsernameNotFoundException("No account for register number " + registerNo));
        return User.withUsername(student.getRegisterNo())
                .password(student.getPasswordHash())
                .roles(student.getRole().name())
                .build();
    }

    @Transactional
    public Student register(RegisterForm form) {
        String registerNo = form.getRegisterNo().trim().toUpperCase(Locale.ROOT);
        if (students.existsByRegisterNoIgnoreCase(registerNo)) {
            throw new BusinessException("An account already exists for " + registerNo + ".");
        }
        if (students.existsByEmailIgnoreCase(form.getEmail().trim())) {
            throw new BusinessException("That email address is already registered.");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("The two passwords do not match.");
        }

        Student student = new Student();
        student.setRegisterNo(registerNo);
        student.setName(form.getName().trim());
        student.setEmail(form.getEmail().trim().toLowerCase(Locale.ROOT));
        student.setDepartment(form.getDepartment().trim());
        student.setPhone(form.getPhone() == null || form.getPhone().isBlank() ? null : form.getPhone().trim());
        student.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        student.setRole(Role.STUDENT);
        return students.save(student);
    }

    @Transactional(readOnly = true)
    public Student currentStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return students.findByRegisterNoIgnoreCase(auth.getName()).orElse(null);
    }

    @Transactional
    public void changePassword(Student student, ChangePasswordForm form) {
        if (!passwordEncoder.matches(form.getCurrentPassword(), student.getPasswordHash())) {
            throw new BusinessException("Your current password is incorrect.");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("The two new passwords do not match.");
        }
        if (passwordEncoder.matches(form.getNewPassword(), student.getPasswordHash())) {
            throw new BusinessException("Choose a password you have not used before.");
        }
        student.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        students.save(student);
    }
}
