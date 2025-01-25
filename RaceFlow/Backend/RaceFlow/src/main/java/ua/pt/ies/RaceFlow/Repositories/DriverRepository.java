package ua.pt.ies.RaceFlow.Repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.pt.ies.RaceFlow.Entities.Driver;

public interface DriverRepository extends JpaRepository<Driver, Integer>{
    Optional<Driver> findByDriverNumber(int driverNumber);
    boolean existsByDriverNumber(int driverNumber);

}
