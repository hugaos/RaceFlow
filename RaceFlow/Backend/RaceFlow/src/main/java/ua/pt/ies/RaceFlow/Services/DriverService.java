package ua.pt.ies.RaceFlow.Services;

import ua.pt.ies.RaceFlow.Entities.Car;
import ua.pt.ies.RaceFlow.Entities.Driver;
import ua.pt.ies.RaceFlow.Repositories.DriverRepository;
import ua.pt.ies.RaceFlow.Services.ExternalAPIService;
import ua.pt.ies.RaceFlow.Repositories.CarRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DriverService{

    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private ExternalAPIService externalAPIService;
    @Autowired
    private CarRepository carRepository;
    // GET
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }


    // POST (Cria um driver e automaticamente o carro associado)
    public Driver createDriver(Driver driver) {
        // 1. Salva o driver
        Driver savedDriver = driverRepository.save(driver);

        // 2. Cria o Car correspondente (caso não exista)
        if (!carRepository.findByDriver(savedDriver).isPresent()) {
            Car newCar = new Car();

            // Atribuir valores padrão ao carro
            newCar.setDriver(savedDriver);
            newCar.setTeamName(savedDriver.getTeamName());
            newCar.setTeamColor(savedDriver.getTeamColour());
            newCar.setGear(0);
            newCar.setCurrentLap(0);
            newCar.setTyreType("Soft");
            newCar.setRpm(0);
            newCar.setCurrentSpeed(0.0);
            newCar.setTyreTemp(List.of(100.0, 100.0, 100.0, 100.0));
            newCar.setLapTimes(List.of());
            newCar.setLocation(List.of(-80.237228f,25.9590515f)); // Exemplo de coords iniciais
            newCar.setImage_url(getImageUrlForTeam(savedDriver.getTeamName()));

            carRepository.save(newCar);
        }
        return savedDriver;
    }

    public List<Driver> createAllDrivers() {
        // 1. Obter dados dos drivers da API externa
        List<Map<String, Object>> driversData = externalAPIService.fetchAllDrivers();

        // 2. Converter os dados da API externa para objetos Driver
        List<Driver> drivers = driversData.stream().map(data -> {
            Driver driver = new Driver();
            driver.setDriverNumber((Integer) data.get("driver_number"));
            driver.setName((String) data.get("broadcast_name"));
            driver.setTeamName((String) data.get("team_name"));
            driver.setPhoto((String) data.get("headshot_url"));
            driver.setCountryCode((String) data.get("country_code"));
            driver.setAcronym((String) data.get("name_acronym"));
            driver.setTeamColour((String) data.get("team_colour"));
            return driver;
        }).collect(Collectors.toList());

        // 3. Processar cada driver individualmente
        List<Driver> savedDrivers = new ArrayList<>();
        for (Driver driver : drivers) {
            // 3.1 Verificar se o driver já existe no banco de dados
            Driver savedDriver = driverRepository.findByDriverNumber(driver.getDriverNumber())
                    .orElseGet(() -> driverRepository.save(driver));

            // 3.2 Criar o carro associado ao driver, se ainda não existir
            if (!carRepository.findByDriver(savedDriver).isPresent()) {
                Car newCar = new Car();
                newCar.setDriver(savedDriver);
                newCar.setTeamName(savedDriver.getTeamName());
                newCar.setTeamColor(savedDriver.getTeamColour());
                newCar.setGear(0); // Valor padrão
                newCar.setCurrentLap(0); // Volta inicial
                newCar.setTyreType("Soft"); // Tipo de pneu inicial
                newCar.setRpm(0); // Rpm inicial
                newCar.setCurrentSpeed(0.0); // Velocidade inicial
                newCar.setTyreTemp(List.of(100.0, 100.0, 100.0, 100.0)); // Temperaturas padrão
                newCar.setLapTimes(List.of()); // Sem tempos de volta no início
                newCar.setLocation(List.of(-80.237228f,25.9590515f)); // Coordenadas iniciais
                newCar.setImage_url(getImageUrlForTeam(savedDriver.getTeamName()));

                carRepository.save(newCar);
            }

            // 3.3 Adicionar o driver salvo à lista de retorno
            savedDrivers.add(savedDriver);
        }

        // 4. Retornar a lista de drivers criados ou existentes
        return savedDrivers;
    }

    // GET by id
    public Driver getDriverById(Integer id) {
        return driverRepository.findById(id).orElse(null);
    }
    
    // DELETE by id
    public void deleteDriverById(Integer driverId) {
        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver != null) {
            Car car = carRepository.findByDriverNumber(driver.getDriverNumber());
            if (car != null) {
                carRepository.delete(car);
            }
            driverRepository.delete(driver);
        }
    }


    // PUT by id
    public Driver updateDriver(Integer id, Driver driverDetails) {
        Driver driver = driverRepository.findById(id).orElse(null);
        if (driver != null) {
            driver.setName(driverDetails.getName());
            driver.setTeamName(driverDetails.getTeamName());
            driver.setPhoto(driverDetails.getPhoto());
            driver.setCountryCode(driverDetails.getCountryCode());
            driver.setAcronym(driverDetails.getAcronym());
            driver.setDriverNumber(driverDetails.getDriverNumber());
            driver.setHeartRate(driverDetails.getHeartRate());
            return driverRepository.save(driver);
        } else {
            return null;
        }
    }

    public Driver saveDriver(Driver driver) {
        return driverRepository.save(driver);
    }
    
    private String getImageUrlForTeam(String teamName) {
        // Gerar o caminho da imagem com base no nome da equipa
        String normalizedTeamName = teamName.toLowerCase().replace(" ", "_");
        return "/car-images/" + normalizedTeamName + ".png";
    }

}

