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
import ua.pt.ies.RaceFlow.Entities.Events.RedFlag;
import ua.pt.ies.RaceFlow.Services.Events.RedFlagService;
import java.util.List;

@RestController
@RequestMapping("/api/events/redflag")
public class RedFlagController {

    private final RedFlagService redFlagService;

    public RedFlagController(RedFlagService redFlagService) {
        this.redFlagService = redFlagService;
    }

    // POST - Create a new red flag event
    @PostMapping
    public ResponseEntity<RedFlag> createRedFlag(@RequestBody RedFlag redFlag) {
        RedFlag newRedFlag = redFlagService.createRedFlag(redFlag);
        return ResponseEntity.ok(newRedFlag);
    }

    // GET - Retrieve all red flag events
    @GetMapping
    public ResponseEntity<List<RedFlag>> getAllRedFlags() {
        List<RedFlag> redFlags = redFlagService.getAllRedFlags();
        return ResponseEntity.ok(redFlags);
    }

    // GET - Retrieve red flag event by ID
    @GetMapping("/{id}")
    public ResponseEntity<RedFlag> getRedFlagById(@PathVariable Integer id) {
        RedFlag redFlag = redFlagService.getRedFlagById(id);
        return redFlag != null ? ResponseEntity.ok(redFlag) : ResponseEntity.notFound().build();
    }

    // DELETE - Delete red flag event by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRedFlag(@PathVariable Integer id) {
        redFlagService.deleteRedFlag(id);
        return ResponseEntity.noContent().build();
    }

    // PUT - Update red flag event by ID
    @PutMapping("/{id}")
    public ResponseEntity<RedFlag> updateRedFlag(@PathVariable Integer id, @RequestBody RedFlag redFlag) {
        RedFlag updatedRedFlag = redFlagService.updateRedFlag(id, redFlag);
        return ResponseEntity.ok(updatedRedFlag);
    }

}
