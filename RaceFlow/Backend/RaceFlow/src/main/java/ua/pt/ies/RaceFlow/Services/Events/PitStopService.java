package ua.pt.ies.RaceFlow.Services.Events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.pt.ies.RaceFlow.Repositories.DriverRepository;
import ua.pt.ies.RaceFlow.Repositories.Events.PitStopRepository;
import ua.pt.ies.RaceFlow.Entities.Events.PitStop;
import ua.pt.ies.RaceFlow.Entities.Driver;

import java.util.List;
import java.util.Optional;

@Service
public class PitStopService {

    @Autowired
    private PitStopRepository pitStopRepository;

    @Autowired
    private DriverRepository driverRepository;

    // POST
    public PitStop createPitStop(PitStop pitStop) {
        System.out.println("Saving pit stop: " + pitStop);
        Integer driverNumber = pitStop.getDriver().getDriverNumber();
        Optional<Driver> driver = driverRepository.findByDriverNumber(driverNumber);

        if (driver.isPresent()) {
            pitStop.setDriver(driver.get());
        } else {
            throw new RuntimeException("Driver com o número " + driverNumber + " não encontrado.");
        }

        return pitStopRepository.save(pitStop);
    }

    //GET ALL
    public List<PitStop> getAllPitStops() {
        return pitStopRepository.findAll();
    }

    // GET
    public PitStop getPitStopById(Integer id) {
        return pitStopRepository.findById(id).orElse(null);
    }

    // DELETE
    public void deletePitStop(Integer id) {
        pitStopRepository.deleteById(id);
    }

    // PUT
    public PitStop updatePitStop(Integer id, PitStop pitStop) {
        PitStop existingPitStop = pitStopRepository.findById(id).orElse(null);
        if (existingPitStop == null) {
            return null;
        }

        existingPitStop.setDriver(pitStop.getDriver());
        existingPitStop.setDuration(pitStop.getDuration());
        existingPitStop.setLap(pitStop.getLap());
        existingPitStop.setTimestamp(pitStop.getTimestamp());

        return pitStopRepository.save(existingPitStop);
    }
    
}
