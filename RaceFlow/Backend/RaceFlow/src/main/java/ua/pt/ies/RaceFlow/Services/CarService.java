package ua.pt.ies.RaceFlow.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import ua.pt.ies.RaceFlow.Entities.Car;
import ua.pt.ies.RaceFlow.Repositories.*;
import ua.pt.ies.RaceFlow.Entities.Driver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class CarService {

    private CarRepository carRepository;
    private DriverRepository driverRepository;  

    @Autowired
    public CarService(CarRepository carRepository, DriverRepository driverRepository) {
        this.carRepository = carRepository;
        this.driverRepository = driverRepository;
    }

    // POST
    public Car createCar(Map<String, Object> carData) {
        Integer driverId = (Integer) carData.get("driverId");

        // Buscar o Driver pelo ID
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        // Criar o carro
        Car car = new Car();
        car.setTyreTemp((List<Double>) carData.get("tyreTemp"));

        List<Double> locationAsDoubles = (List<Double>) carData.get("location");
        List<Float> locationAsFloats = locationAsDoubles.stream()
                .map(Double::floatValue)
                .toList();
        car.setLocation(locationAsFloats);

        car.setRpm((Integer) carData.get("rpm"));
        car.setCurrentLap((Integer) carData.get("currentLap"));
        car.setTyreType((String) carData.get("tyreType"));
        car.setGear((Integer) carData.get("gear"));
        car.setCurrentSpeed((Double) carData.get("currentSpeed"));
        car.setLapTimes(new ArrayList<>());
        car.setImage_url(getImageUrlForTeam(driver.getTeamName()));

        // Configurar os atributos que dependem do Driver
        car.setDriver(driver);
        car.setTeamName(driver.getTeamName());
        car.setTeamColor(driver.getTeamColour());

        // Salvar o carro no repositório
        return carRepository.save(car);
    }

    // POST
    public void createCarsForAllDrivers() {
        List<Driver> drivers = driverRepository.findAll(); // Buscar todos os drivers

        for (Driver driver : drivers) {
            // Verificar se o driver já tem um carro associado
            if (carRepository.findByDriver(driver).isPresent()) {
                continue; // Ignorar drivers que já têm carros
            }

            // Criar o carro
            Car car = new Car();
            car.setDriver(driver);
            car.setTeamName(driver.getTeamName());
            car.setTeamColor(driver.getTeamColour());
            car.setGear(0); // Valor padrão
            car.setCurrentLap(0);
            car.setTyreType("Soft");
            car.setRpm(0); 
            car.setCurrentSpeed(0.0); // Parado inicialmente
            car.setTyreTemp(List.of(100.0, 100.0, 100.0, 100.0)); // Temperatura padrão
            car.setLapTimes(new ArrayList<>()); // Lista vazia
            List<Double> initialLocationAsDoubles = List.of(-80.237228,25.9590515);
            List<Float> initialLocationAsFloats = initialLocationAsDoubles.stream()
                    .map(Double::floatValue)
                    .toList();
            car.setLocation(initialLocationAsFloats); // Localização inicial

            String imageUrl = getImageUrlForTeam(driver.getTeamName());
            car.setImage_url(imageUrl);

            // Salvar o carro
            carRepository.save(car);
        }
    }

    // GET
    public List<Car> getAllCars() {
        return carRepository.findAllWithDrivers();
    }

    // GET by id
    public Car getCarById(Integer id) {
        return carRepository.findByIdWithDriver(id)
                .orElseThrow(() -> new RuntimeException("Car not found for driver with ID: " + id));    }

    // DELETE by car id
    public void deleteCarById(Integer id) {
        carRepository.deleteById(id);
    }

    // PUT
    public Car updateCar(Integer carId, Car carDetails) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Car not found"));

        car.setTeamName(carDetails.getTeamName());
        car.setTeamColor(carDetails.getTeamColor());
        car.setTyreTemp(carDetails.getTyreTemp());
        car.setLocation(carDetails.getLocation());
        car.setRpm(carDetails.getRpm());
        car.setCurrentLap(carDetails.getCurrentLap());
        car.setTyreType(carDetails.getTyreType());
        car.setGear(carDetails.getGear());
        car.setCurrentSpeed(carDetails.getCurrentSpeed());

        Driver driver = driverRepository.findById(carDetails.getDriver().getId()).orElse(null);
        if (driver != null) {
            car.setDriver(driver);
        }

        if (carDetails.getLapTimes() == null) {
            car.setLapTimes(new ArrayList<>());
        }
        return carRepository.save(car);
    }

    // GET car position by id
    public List<Float> findLocationById(Integer id) {
        return carRepository.findLocationById(id);
    }

    public Car getCarByDriverId(Integer driverId) {
        return carRepository.findByDriverId(driverId)
                .orElseThrow(() -> new RuntimeException("Car not found for driver ID: " + driverId));
    }

    public Car getCarByDriverNumber(Integer driverNumber) {
        return carRepository.findByDriverNumber(driverNumber);
    }

    public void saveCar(Car car) {
        carRepository.save(car);
    }
    
    private String getImageUrlForTeam(String teamName) {
        // Gerar o caminho da imagem com base no nome da equipa
        String normalizedTeamName = teamName.toLowerCase().replace(" ", "_");
        return "/car-images/" + normalizedTeamName + ".png";
    }

    @Transactional
    public void addLapTime(Integer driverId, Double lapTime) {
        Car car = carRepository.findByDriverId(driverId)
                .orElseThrow(() -> new RuntimeException("Car not found for driver ID: " + driverId));

        if (car.getLapTimes() == null) {
            car.setLapTimes(new ArrayList<>());
        }
        car.getLapTimes().add(lapTime);
        carRepository.save(car);

        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new RuntimeException("Driver not found for ID: " + driverId));

        // Calculate the fastest lap
        double fastestLap = car.getLapTimes().stream()
                .min(Double::compare)
                .orElse(Double.MAX_VALUE);

        // Update the driver's fastest lap
        driver.setFastest_lap(fastestLap);

        // Save the updated driver
        driverRepository.save(driver);
    }

}