package in.edu.ktu.buspass.repository;

import in.edu.ktu.buspass.domain.Student;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, String> {

    Optional<Student> findByRegisterNoIgnoreCase(String registerNo);

    boolean existsByRegisterNoIgnoreCase(String registerNo);

    boolean existsByEmailIgnoreCase(String email);
}
