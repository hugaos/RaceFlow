package ua.pt.ies.RaceFlow.Repositories.Events;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.pt.ies.RaceFlow.Entities.Events.SafetyCar;

public interface SafetyCarRepository extends JpaRepository<SafetyCar, Integer> {
    
}
