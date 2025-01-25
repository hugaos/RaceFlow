package ua.pt.ies.RaceFlow.Repositories.Events;

import ua.pt.ies.RaceFlow.Entities.Events.RedFlag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RedFlagRepository extends JpaRepository<RedFlag, Integer> {
    
}
