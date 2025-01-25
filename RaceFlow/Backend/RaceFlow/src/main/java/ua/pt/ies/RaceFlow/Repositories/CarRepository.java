package ua.pt.ies.RaceFlow.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ua.pt.ies.RaceFlow.Entities.Car;
import ua.pt.ies.RaceFlow.Entities.Driver;

import java.lang.StackWalker.Option;
import java.util.List;
import java.util.Optional;


public interface CarRepository extends JpaRepository<Car, Integer> {
    @Query("SELECT c FROM Car c LEFT JOIN FETCH c.driver WHERE c.id = :id")
    Optional<Car> findByIdWithDriver(@Param("id") Integer id);

    @Query("SELECT c FROM Car c LEFT JOIN FETCH c.driver")
    List<Car> findAllWithDrivers();

    @Query("SELECT c.location FROM Car c WHERE c.id = :id")
    List<Float> findLocationById(@Param("id") Integer id);

    @Query("SELECT c FROM Car c WHERE c.driver.id = :driverId")
    Optional<Car> findByDriverId(@Param("driverId") Integer driverId);

    @Query("SELECT c FROM Car c WHERE c.driver.driverNumber = :driverNumber")
    Car findByDriverNumber(@Param("driverNumber") Integer driverNumber);

    Optional<Car> findByDriver(Driver driver);

}
