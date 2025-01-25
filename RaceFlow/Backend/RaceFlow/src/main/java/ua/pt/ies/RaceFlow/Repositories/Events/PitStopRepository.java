package ua.pt.ies.RaceFlow.Repositories.Events;

import ua.pt.ies.RaceFlow.Entities.Events.PitStop;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PitStopRepository extends JpaRepository<PitStop, Integer> {
    
}
