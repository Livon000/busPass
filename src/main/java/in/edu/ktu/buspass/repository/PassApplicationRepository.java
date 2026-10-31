package in.edu.ktu.buspass.repository;

import in.edu.ktu.buspass.domain.PassApplication;
import in.edu.ktu.buspass.domain.PassStatus;
import in.edu.ktu.buspass.domain.Student;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassApplicationRepository extends JpaRepository<PassApplication, Long> {

    List<PassApplication> findByStudentOrderByAppliedAtDesc(Student student);

    List<PassApplication> findByStatusOrderByAppliedAtAsc(PassStatus status);

    List<PassApplication> findAllByOrderByAppliedAtDesc();

    Optional<PassApplication> findByReferenceIgnoreCase(String reference);

    Optional<PassApplication> findFirstByStudentAndStatusAndValidToGreaterThanEqualOrderByValidFromAsc(
            Student student, PassStatus status, LocalDate date);

    long countByStatus(PassStatus status);

    long countByStudent(Student student);
}
