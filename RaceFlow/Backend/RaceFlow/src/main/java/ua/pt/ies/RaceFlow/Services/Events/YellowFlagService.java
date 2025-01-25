package ua.pt.ies.RaceFlow.Services.Events;

import ua.pt.ies.RaceFlow.Entities.Events.YellowFlag;
import ua.pt.ies.RaceFlow.Repositories.Events.YellowFlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class YellowFlagService {

    @Autowired
    private YellowFlagRepository YellowFlagRepository;


    // POST
    public YellowFlag createYellowFlag(YellowFlag YellowFlag) {
        return YellowFlagRepository.save(YellowFlag);
    }

    //GET ALL
    public List<YellowFlag> getAllYellowFlags() {
        return YellowFlagRepository.findAll();
    }

    // GET
    public YellowFlag getYellowFlagById(Integer id) {
        return YellowFlagRepository.findById(id).orElse(null);
    }

    // DELETE
    public void deleteYellowFlag(Integer id) {
        YellowFlagRepository.deleteById(id);
    }

    // PUT
    public YellowFlag updateYellowFlag(Integer id, YellowFlag YellowFlag) {
        YellowFlag existingYellowFlag = YellowFlagRepository.findById(id).orElse(null);
        if (existingYellowFlag == null) {
            return null;
        }

        existingYellowFlag.setLap(YellowFlag.getLap());
        existingYellowFlag.setCoordinates(YellowFlag.getCoordinates());
        existingYellowFlag.setDuration(YellowFlag.getDuration());
        existingYellowFlag.setTimestamp(YellowFlag.getTimestamp());

        return YellowFlagRepository.save(YellowFlag);
    }
    
}
