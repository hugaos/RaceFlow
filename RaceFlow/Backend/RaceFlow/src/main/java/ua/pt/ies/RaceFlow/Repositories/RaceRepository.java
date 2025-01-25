package ua.pt.ies.RaceFlow.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.pt.ies.RaceFlow.Entities.Race;

public interface RaceRepository extends JpaRepository<Race, Integer>{
    
}
