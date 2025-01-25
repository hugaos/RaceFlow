package ua.pt.ies.RaceFlow.Controllers.Events;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.pt.ies.RaceFlow.Services.Events.PitStopService;
import ua.pt.ies.RaceFlow.Entities.Events.PitStop;
import ua.pt.ies.RaceFlow.Entities.Driver;
import ua.pt.ies.RaceFlow.Repositories.DriverRepository;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/events/pitstop")
@Tag(name = "Pit Stop Controller", description = "Operations related to pit stop events in the race")
public class PitStopController {

    private final PitStopService pitStopService;
    private DriverRepository driverRepository;

    public PitStopController(PitStopService pitStopService) {
        this.pitStopService = pitStopService;
    }

    // POST - Create a new pit stop event
    @PostMapping
    public ResponseEntity<PitStop> createPitStop(@RequestBody PitStop pitStop) {
        Driver driver = driverRepository.findById(pitStop.getDriver().getId())
            .orElseThrow(() -> new RuntimeException("Driver not found"));
    
        pitStop.setDriver(driver);
    
        PitStop newPitStop = pitStopService.createPitStop(pitStop);
    
        return ResponseEntity.ok(newPitStop);
    }

    // GET - Retrieve all pit stop events
    @GetMapping
    public ResponseEntity<List<PitStop>> getAllPitStops() {
        List<PitStop> pitStops = pitStopService.getAllPitStops();
        return ResponseEntity.ok(pitStops);
    }

    // GET - Retrieve pit stop event by ID
    @GetMapping("/{id}")
    public ResponseEntity<PitStop> getPitStopById(@PathVariable Integer id) {
        PitStop pitStop = pitStopService.getPitStopById(id);
        return pitStop != null ? ResponseEntity.ok(pitStop) : ResponseEntity.notFound().build();
    }

    // DELETE - Delete pit stop event by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePitStop(@PathVariable Integer id) {
        pitStopService.deletePitStop(id);
        return ResponseEntity.noContent().build();
    }

    // PUT - Update pit stop event by ID
    @PutMapping("/{id}")
    public ResponseEntity<PitStop> updatePitStop(@PathVariable Integer id, @RequestBody PitStop pitStop) {
        PitStop updatedPitStop = pitStopService.updatePitStop(id, pitStop);
        return updatedPitStop != null ? ResponseEntity.ok(updatedPitStop) : ResponseEntity.notFound().build();
    }
}