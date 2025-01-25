package ua.pt.ies.RaceFlow.Services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.pt.ies.RaceFlow.Repositories.RaceRepository;
import ua.pt.ies.RaceFlow.Entities.Car;
import ua.pt.ies.RaceFlow.Entities.Race;
import ua.pt.ies.RaceFlow.Repositories.CarRepository;

@Service
public class RaceService {

    @Autowired
    private RaceRepository raceRepository;
    @Autowired
    private CarRepository CarRepository;

    // Create
    public Race createRace(Race race) {
        if (race.getName() == null) {
            race.setName("Miami Grand Prix 2024");
        }
        if (race.getTotalLaps() == null) {
            race.setTotalLaps(30);
        }
        if (race.getDate() == null) {
            race.setDate(LocalDate.now());
        }
        if (race.getLocation() == null) {
            race.setLocation("miami");
        }
        if (race.getWeather() == null) {
            String[] weathers = { "sunny", "rainy", "foggy", "windy" };
            race.setWeather(weathers[(int) (Math.random() * weathers.length)]);
        }
        if (race.getDuration() == null) {
            race.setDuration(0);
        }
        if (race.getRunning() == null) {
            race.setRunning(false);
        }

        if (race.getTrackTemperature() == null) {
            double temperature;
            switch (race.getWeather()) {
                case "sunny":
                    temperature = 25 + (Math.random() * 10); // 25 a 35
                    break;
                case "rainy":
                    temperature = 18 + (Math.random() * 7); // 18 a 25
                    break;
                case "foggy":
                    temperature = 10 + (Math.random() * 8); // 10 a 18
                    break;
                case "windy":
                    temperature = 15 + (Math.random() * 5); // 15 a 20
                    break;
                default:
                    temperature = 20; // Se não identificar o weather, define 20 por defeito
            }
            temperature = Math.round(temperature * 10.0) / 10.0;
            race.setTrackTemperature(temperature);

        }
        List<Car> allCars = CarRepository.findAll();

        // Definir a lista de carros da corrida
        race.setCars(allCars);

        return raceRepository.save(race);
    }

    public void updateRace(Race race) {
        Race existingRace = raceRepository.findById(race.getId()).orElse(null);

        if (existingRace != null) {
            existingRace.setName(race.getName());
            existingRace.setDate(race.getDate());
            existingRace.setLocation(race.getLocation());
            existingRace.setDuration(race.getDuration());
            existingRace.setTotalLaps(race.getTotalLaps());
            existingRace.setWeather(race.getWeather());
            existingRace.setTrackTemperature(race.getTrackTemperature());
            existingRace.setRunning(race.getRunning());
            existingRace.setCars(race.getCars());

            raceRepository.save(existingRace);
        }
    }

    // Read
    public List<Race> getRaces() {
        return raceRepository.findAll();
    }

    public Race getRaceById(Integer id) {
        return raceRepository.findById(id).orElse(null);
    }

    public void deleteRace(Integer id) {
        raceRepository.deleteById(id);
    }

}
