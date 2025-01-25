package ua.pt.ies.RaceFlow.Controllers.Events;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import ua.pt.ies.RaceFlow.Entities.Events.YellowFlag;
import ua.pt.ies.RaceFlow.Services.Events.YellowFlagService;
import java.util.List;

@RestController
@RequestMapping("/api/events/yellowflag")
public class YellowFlagController {

    private YellowFlagService yellowFlagService;

    public YellowFlagController(YellowFlagService yellowFlagService) {
        this.yellowFlagService = yellowFlagService;
    }

    // POST - Create a new yellow flag event
    @PostMapping
    public ResponseEntity<YellowFlag> createYellowFlag(@RequestBody YellowFlag yellowFlag) {
        YellowFlag newYellowFlag = yellowFlagService.createYellowFlag(yellowFlag);
        return ResponseEntity.ok(newYellowFlag);
    }

    // GET - Retrieve all yellow flag events
    @GetMapping
    public ResponseEntity<List<YellowFlag>> getAllYellowFlags() {
        List<YellowFlag> yellowFlags = yellowFlagService.getAllYellowFlags();
        return ResponseEntity.ok(yellowFlags);
    }

    // GET - Retrieve yellow flag event by ID
    @GetMapping("/{id}")
    public ResponseEntity<YellowFlag> getYellowFlagById(@PathVariable Integer id) {
        YellowFlag yellowFlag = yellowFlagService.getYellowFlagById(id);
        return yellowFlag != null ? ResponseEntity.ok(yellowFlag) : ResponseEntity.notFound().build();
    }

    // DELETE - Delete yellow flag event by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteYellowFlag(@PathVariable Integer id) {
        yellowFlagService.deleteYellowFlag(id);
        return ResponseEntity.noContent().build();
    }

    // PUT - Update yellow flag event by ID
    @PutMapping("/{id}")
    public ResponseEntity<YellowFlag> updateYellowFlag(@PathVariable Integer id, @RequestBody YellowFlag yellowFlag) {
        YellowFlag updatedYellowFlag = yellowFlagService.updateYellowFlag(id, yellowFlag);
        return ResponseEntity.ok(updatedYellowFlag);
    }
    
}
