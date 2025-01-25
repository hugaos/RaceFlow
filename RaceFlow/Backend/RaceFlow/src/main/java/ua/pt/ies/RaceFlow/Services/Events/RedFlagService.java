package ua.pt.ies.RaceFlow.Services.Events;

import ua.pt.ies.RaceFlow.Entities.Events.RedFlag;
import ua.pt.ies.RaceFlow.Repositories.Events.RedFlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class RedFlagService {

    @Autowired
    private RedFlagRepository redFlagRepository;


    // POST
    public RedFlag createRedFlag(RedFlag redFlag) {
        return redFlagRepository.save(redFlag);
    }

    //GET ALL
    public List<RedFlag> getAllRedFlags() {
        return redFlagRepository.findAll();
    }

    // GET
    public RedFlag getRedFlagById(Integer id) {
        return redFlagRepository.findById(id).orElse(null);
    }

    // DELETE
    public void deleteRedFlag(Integer id) {
        redFlagRepository.deleteById(id);
    }

    // PUT
    public RedFlag updateRedFlag(Integer id, RedFlag redFlag) {
        RedFlag existingRedFlag = redFlagRepository.findById(id).orElse(null);
        if (existingRedFlag == null) {
            return null;
        }

        existingRedFlag.setLap(redFlag.getLap());
        existingRedFlag.setTimestamp(redFlag.getTimestamp());

        return redFlagRepository.save(redFlag);
    }
    
}
