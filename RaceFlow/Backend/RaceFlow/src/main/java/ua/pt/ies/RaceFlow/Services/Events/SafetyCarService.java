package ua.pt.ies.RaceFlow.Services.Events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.pt.ies.RaceFlow.Repositories.Events.SafetyCarRepository;
import ua.pt.ies.RaceFlow.Entities.Events.SafetyCar;
import java.util.List;

@Service
public class SafetyCarService {

    @Autowired
    private SafetyCarRepository safetyCarRepository;

    // POST
    public SafetyCar createSafetyCar(SafetyCar safetyCar) {
        return safetyCarRepository.save(safetyCar);
    }

    //GET ALL
    public List<SafetyCar> getAllSafetyCars() {
        return safetyCarRepository.findAll();
    }

    // GET
    public SafetyCar getSafetyCarById(Integer id) {
        return safetyCarRepository.findById(id).orElse(null);
    }

    // DELETE
    public void deleteSafetyCar(Integer id) {
        safetyCarRepository.deleteById(id);
    }

    // PUT
    public SafetyCar updateSafetyCar(Integer id, SafetyCar safetyCar) {
        SafetyCar existingSafetyCar = safetyCarRepository.findById(id).orElse(null);
        if (existingSafetyCar == null) {
            return null;
        }

        existingSafetyCar.setLap(safetyCar.getLap());
        existingSafetyCar.setCoordinates(safetyCar.getCoordinates());
        existingSafetyCar.setDuration(safetyCar.getDuration());
        existingSafetyCar.setAverageSpeed(safetyCar.getAverageSpeed());

        return safetyCarRepository.save(safetyCar);
    }
    
}
