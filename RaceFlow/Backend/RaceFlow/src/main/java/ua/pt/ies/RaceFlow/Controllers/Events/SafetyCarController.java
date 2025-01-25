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
import ua.pt.ies.RaceFlow.Entities.Events.SafetyCar;
import ua.pt.ies.RaceFlow.Services.Events.SafetyCarService;
import java.util.List;

@RestController
@RequestMapping("/api/events/safetycar")
public class SafetyCarController {

    private SafetyCarService safetyCarService;

    public SafetyCarController(SafetyCarService safetyCarService) {
        this.safetyCarService = safetyCarService;
    }

    // POST - Create a new safety car event
    @PostMapping
    public ResponseEntity<SafetyCar> createSafetyCar(@RequestBody SafetyCar safetyCar) {
        SafetyCar newSafetyCar = safetyCarService.createSafetyCar(safetyCar);
        return ResponseEntity.ok(newSafetyCar);
    }

    // GET - Retrieve all safety car events
    @GetMapping
    public ResponseEntity<List<SafetyCar>> getAllSafetyCars() {
        List<SafetyCar> safetyCars = safetyCarService.getAllSafetyCars();
        return ResponseEntity.ok(safetyCars);
    }

    // GET - Retrieve safety car event by ID
    @GetMapping("/{id}")
    public ResponseEntity<SafetyCar> getSafetyCarById(@PathVariable Integer id) {
        SafetyCar safetyCar = safetyCarService.getSafetyCarById(id);
        return safetyCar != null ? ResponseEntity.ok(safetyCar) : ResponseEntity.notFound().build();
    }

    // DELETE - Delete safety car event by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSafetyCar(@PathVariable Integer id) {
        safetyCarService.deleteSafetyCar(id);
        return ResponseEntity.noContent().build();
    }

    // PUT - Update safety car event by ID
    @PutMapping("/{id}")
    public ResponseEntity<SafetyCar> updateSafetyCar(@PathVariable Integer id, @RequestBody SafetyCar safetyCar) {
        SafetyCar updatedSafetyCar = safetyCarService.updateSafetyCar(id, safetyCar);
        return ResponseEntity.ok(updatedSafetyCar);
    }
    
}
