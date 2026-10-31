package in.edu.ktu.buspass.config;

import in.edu.ktu.buspass.domain.BusRoute;
import in.edu.ktu.buspass.domain.PassApplication;
import in.edu.ktu.buspass.domain.PassStatus;
import in.edu.ktu.buspass.domain.PassType;
import in.edu.ktu.buspass.domain.Role;
import in.edu.ktu.buspass.domain.Student;
import in.edu.ktu.buspass.repository.BusRouteRepository;
import in.edu.ktu.buspass.repository.PassApplicationRepository;
import in.edu.ktu.buspass.repository.StudentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Seeds demo routes, accounts and passes the first time the app starts. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final StudentRepository students;
    private final BusRouteRepository routes;
    private final PassApplicationRepository passes;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(StudentRepository students, BusRouteRepository routes,
                      PassApplicationRepository passes, PasswordEncoder passwordEncoder) {
        this.students = students;
        this.routes = routes;
        this.passes = passes;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedRoutes();
        seedStudents();
        seedPasses();
    }

    private void seedRoutes() {
        if (routes.count() > 0) {
            return;
        }
        routes.save(route("R1", "Technopark – Campus", "Kazhakkoottam", "KTU Campus",
                "Kazhakkoottam • Technopark • Karyavattom", 8.5, "700"));
        routes.save(route("R2", "City – Campus", "East Fort", "KTU Campus",
                "East Fort • Palayam • Sreekaryam • Karyavattom", 16.2, "1100"));
        routes.save(route("R3", "Kollam – Campus", "Kollam", "KTU Campus",
                "Kollam • Karunagappally • Chathanoor • Karyavattom", 62.0, "2400"));
        routes.save(route("R4", "Neyyattinkara – Campus", "Neyyattinkara", "KTU Campus",
                "Neyyattinkara • Balaramapuram • Kovalam • Karyavattom", 28.4, "1600"));
        routes.save(route("R5", "Attingal – Campus", "Attingal", "KTU Campus",
                "Attingal • Kilimanoor • Venjaramoodu • Karyavattom", 34.8, "1800"));
    }

    private void seedStudents() {
        if (students.count() > 0) {
            return;
        }
        students.save(student("TVE22CS001", "Aravind Menon", "aravind@example.com",
                "Computer Science", "9876543210", "Passw0rd!", Role.STUDENT));
        students.save(student("TVE22EC014", "Diya Nair", "diya@example.com",
                "Electronics", "9876543211", "Passw0rd!", Role.STUDENT));
        students.save(student("TVE21ME007", "Rahul Kumar", "rahul@example.com",
                "Mechanical", "9876543212", "Passw0rd!", Role.STUDENT));
        students.save(student("KTUADMIN", "Bus Pass Cell", "admin@ktu.example",
                "Administration", "04712598700", "Admin@123", Role.ADMIN));
    }

    private void seedPasses() {
        if (passes.count() > 0) {
            return;
        }
        BusRoute city = routes.findByCode("R2").orElse(null);
        BusRoute technopark = routes.findByCode("R1").orElse(null);
        Student aravind = students.findByRegisterNoIgnoreCase("TVE22CS001").orElse(null);
        Student diya = students.findByRegisterNoIgnoreCase("TVE22EC014").orElse(null);
        if (city == null || technopark == null || aravind == null || diya == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        passes.save(pass("KTU-" + today.getYear() + "-DEMO01", aravind, city, PassType.MONTHLY,
                PassStatus.APPROVED, today.minusDays(4), 1, "Verified at issue counter"));
        passes.save(pass("KTU-" + today.getYear() + "-DEMO02", diya, technopark, PassType.SEMESTER,
                PassStatus.PENDING, today.plusDays(2), 6, null));
    }

    private BusRoute route(String code, String name, String from, String to,
                           String stops, double km, String fare) {
        BusRoute route = new BusRoute();
        route.setCode(code);
        route.setName(name);
        route.setFromPlace(from);
        route.setToPlace(to);
        route.setStops(stops);
        route.setDistanceKm(km);
        route.setMonthlyFare(new BigDecimal(fare));
        return route;
    }

    private Student student(String registerNo, String name, String email, String department,
                            String phone, String rawPassword, Role role) {
        Student student = new Student();
        student.setRegisterNo(registerNo);
        student.setName(name);
        student.setEmail(email);
        student.setDepartment(department);
        student.setPhone(phone);
        student.setPasswordHash(passwordEncoder.encode(rawPassword));
        student.setRole(role);
        return student;
    }

    private PassApplication pass(String reference, Student student, BusRoute route, PassType type,
                                 PassStatus status, LocalDate from, int months, String remarks) {
        PassApplication pass = new PassApplication();
        pass.setReference(reference);
        pass.setStudent(student);
        pass.setRoute(route);
        pass.setPassType(type);
        pass.setStatus(status);
        pass.setValidFrom(from);
        pass.setValidTo(from.plusMonths(months).minusDays(1));
        BigDecimal fee = route.getMonthlyFare().multiply(BigDecimal.valueOf(months));
        if (type == PassType.SEMESTER) {
            fee = fee.multiply(new BigDecimal("0.90"));
        }
        pass.setFee(fee.setScale(2, RoundingMode.HALF_UP));
        pass.setRemarks(remarks);
        return pass;
    }
}
