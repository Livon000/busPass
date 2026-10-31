package in.edu.ktu.buspass.repository;

import in.edu.ktu.buspass.domain.BusRoute;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusRouteRepository extends JpaRepository<BusRoute, Long> {

    List<BusRoute> findAllByOrderByCodeAsc();

    Optional<BusRoute> findByCode(String code);
}
