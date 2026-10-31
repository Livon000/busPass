package in.edu.ktu.buspass.service;

import in.edu.ktu.buspass.domain.BusRoute;
import in.edu.ktu.buspass.domain.PassApplication;
import in.edu.ktu.buspass.domain.PassStatus;
import in.edu.ktu.buspass.domain.PassType;
import in.edu.ktu.buspass.domain.Student;
import in.edu.ktu.buspass.exception.BusinessException;
import in.edu.ktu.buspass.repository.BusRouteRepository;
import in.edu.ktu.buspass.repository.PassApplicationRepository;
import in.edu.ktu.buspass.web.dto.PassApplicationForm;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PassService {

    private static final BigDecimal SEMESTER_DISCOUNT = new BigDecimal("0.90");
    private static final int MAX_DAYS_AHEAD = 30;
    private static final char[] REFERENCE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final PassApplicationRepository passes;
    private final BusRouteRepository routes;
    private final SecureRandom random = new SecureRandom();

    public PassService(PassApplicationRepository passes, BusRouteRepository routes) {
        this.passes = passes;
        this.routes = routes;
    }

    public List<BusRoute> routes() {
        return routes.findAllByOrderByCodeAsc();
    }

    public List<PassType> passTypes() {
        return Arrays.asList(PassType.values());
    }

    @Transactional
    public PassApplication apply(Student student, PassApplicationForm form) {
        if (student.isAdmin()) {
            throw new BusinessException("Administrator accounts cannot apply for a student pass.");
        }
        BusRoute route = routes.findById(form.getRouteId())
                .orElseThrow(() -> new BusinessException("That route is no longer available."));

        LocalDate today = LocalDate.now();
        if (form.getValidFrom().isAfter(today.plusDays(MAX_DAYS_AHEAD))) {
            throw new BusinessException("A pass can start at most " + MAX_DAYS_AHEAD + " days from today.");
        }
        if (form.getValidFrom().isBefore(today)) {
            throw new BusinessException("The start date cannot be in the past.");
        }
        if (hasOpenPass(student)) {
            throw new BusinessException("You already have a pending or valid pass. "
                    + "You can apply again once the current pass expires.");
        }

        int months = form.getPassType().getMonths();
        BigDecimal fee = route.getMonthlyFare()
                .multiply(BigDecimal.valueOf(months))
                .multiply(form.getPassType() == PassType.SEMESTER ? SEMESTER_DISCOUNT : BigDecimal.ONE)
                .setScale(2, RoundingMode.HALF_UP);

        PassApplication pass = new PassApplication();
        pass.setStudent(student);
        pass.setRoute(route);
        pass.setPassType(form.getPassType());
        pass.setStatus(PassStatus.PENDING);
        pass.setFee(fee);
        pass.setValidFrom(form.getValidFrom());
        pass.setValidTo(form.getValidFrom().plusMonths(months).minusDays(1));
        pass.setReference(newReference());
        return passes.save(pass);
    }

    @Transactional(readOnly = true)
    public Optional<PassApplication> currentPass(Student student) {
        return passes.findFirstByStudentAndStatusAndValidToGreaterThanEqualOrderByValidFromAsc(
                student, PassStatus.APPROVED, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public Optional<PassApplication> pendingPass(Student student) {
        return passes.findByStudentOrderByAppliedAtDesc(student).stream()
                .filter(PassApplication::isPending)
                .findFirst();
    }

    @Transactional(readOnly = true)
    public boolean hasOpenPass(Student student) {
        boolean pending = passes.findByStudentOrderByAppliedAtDesc(student).stream()
                .anyMatch(PassApplication::isPending);
        return pending || currentPass(student).isPresent();
    }

    @Transactional(readOnly = true)
    public List<PassApplication> history(Student student) {
        return passes.findByStudentOrderByAppliedAtDesc(student);
    }

    @Transactional(readOnly = true)
    public PassApplication getForStudent(Student student, Long id) {
        PassApplication pass = passes.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pass not found"));
        boolean owner = pass.getStudent().getRegisterNo().equals(student.getRegisterNo());
        if (!owner && !student.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("This pass belongs to another student.");
        }
        return pass;
    }

    @Transactional
    public void cancel(Student student, Long id) {
        PassApplication pass = getForStudent(student, id);
        if (!pass.getStudent().getRegisterNo().equals(student.getRegisterNo())) {
            throw new BusinessException("You can only cancel your own application.");
        }
        if (!pass.isPending()) {
            throw new BusinessException("Only a pending application can be cancelled.");
        }
        pass.setStatus(PassStatus.CANCELLED);
        pass.setDecidedAt(Instant.now());
        pass.setRemarks("Cancelled by student");
    }

    @Transactional(readOnly = true)
    public Optional<PassApplication> verify(String reference) {
        return passes.findByReferenceIgnoreCase(reference);
    }

    @Transactional(readOnly = true)
    public List<PassApplication> byStatus(PassStatus status) {
        return passes.findByStatusOrderByAppliedAtAsc(status);
    }

    @Transactional(readOnly = true)
    public List<PassApplication> all() {
        return passes.findAllByOrderByAppliedAtDesc();
    }

    @Transactional(readOnly = true)
    public long count(PassStatus status) {
        return passes.countByStatus(status);
    }

    @Transactional
    public void approve(Long id, String remarks) {
        decide(id, PassStatus.APPROVED, remarks);
    }

    @Transactional
    public void reject(Long id, String remarks) {
        decide(id, PassStatus.REJECTED, remarks);
    }

    private void decide(Long id, PassStatus status, String remarks) {
        PassApplication pass = passes.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));
        if (!pass.isPending()) {
            throw new BusinessException("This application has already been " + pass.getStatus().getLabel().toLowerCase() + ".");
        }
        pass.setStatus(status);
        pass.setDecidedAt(Instant.now());
        pass.setRemarks(remarks == null || remarks.isBlank() ? null : remarks.trim());
    }

    private String newReference() {
        for (int attempt = 0; attempt < 20; attempt++) {
            StringBuilder sb = new StringBuilder("KTU-").append(LocalDate.now().getYear()).append('-');
            for (int i = 0; i < 6; i++) {
                sb.append(REFERENCE_ALPHABET[random.nextInt(REFERENCE_ALPHABET.length)]);
            }
            String candidate = sb.toString();
            if (passes.findByReferenceIgnoreCase(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not allocate a unique pass reference");
    }
}
