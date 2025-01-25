package ua.pt.ies.RaceFlow.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import ua.pt.ies.RaceFlow.Entities.Race;
import ua.pt.ies.RaceFlow.Entities.Events.RedFlag;
import ua.pt.ies.RaceFlow.Services.RaceService;
import ua.pt.ies.RaceFlow.Services.Events.RedFlagService;
import ua.pt.ies.RaceFlow.Repositories.RaceRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import ua.pt.ies.RaceFlow.Entities.Car;
import static java.lang.Integer.MAX_VALUE;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/races")
@Tag(name = "Race Controller", description = "Operations related to managing races and their fast laps")
public class RaceController { 

    private final RaceService raceService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private RaceRepository raceRepository;

    @Autowired
    private RedFlagService redFlagService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Map<Integer, Double> lastFastestLaps = new HashMap<>();


    public RaceController(RaceService raceService, KafkaTemplate<String, String> kafkaTemplate) {
        this.raceService = raceService;
        this.kafkaTemplate = kafkaTemplate;
    }

    // Get all races
    @GetMapping
    @Operation(summary = "Get all races", description = "Fetches a list of all races in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Races retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    public List<Race> getAllRaces() {
        return raceService.getRaces();
    }

    // Get a specific race by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get race by ID", description = "Fetches a race by its unique ID")
    @Parameter(name = "id", description = "ID of the race to retrieve", required = true)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Race retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Race not found")
    })
    public ResponseEntity<Race> getRaceById(@PathVariable Integer id) {
        Race race = raceService.getRaceById(id);

        return ResponseEntity.ok(race);
    }

    // Create a new race
    @PostMapping
    @Operation(summary = "Create a new race", description = "Adds a new race to the system with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Race created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Race> createRace(@RequestBody Race race) {
        raceService.createRace(race);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Update an existing race
    @PutMapping("/{id}")
    @Operation(summary = "Update race by ID", description = "Updates the details of a race by its unique ID")
    @Parameter(name = "id", description = "ID of the race to update", required = true)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Race updated successfully"),
        @ApiResponse(responseCode = "404", description = "Race not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<Race> updateRace(@PathVariable Integer id, @RequestBody Race raceDetails) {
        Optional<Race> optionalRace = raceRepository.findById(id);
        if (optionalRace.isPresent()) {
            Race raceToUpdate = optionalRace.get();

            raceToUpdate.setName(raceDetails.getName());
            raceToUpdate.setDate(raceDetails.getDate());
            raceToUpdate.setLocation(raceDetails.getLocation());
            raceToUpdate.setDuration(raceDetails.getDuration());
            raceToUpdate.setTotalLaps(raceDetails.getTotalLaps());
            raceToUpdate.setWeather(raceDetails.getWeather());
            raceToUpdate.setTrackTemperature(raceDetails.getTrackTemperature());
            for (Car car : raceDetails.getCars()) {
                if (!raceToUpdate.getCars().contains(car)) {
                    raceToUpdate.getCars().add(car);
                }
            }
            Race updatedRace = raceRepository.save(raceToUpdate);
            return ResponseEntity.ok(updatedRace);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Delete a race by ID    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete race by ID", description = "Removes a race from the system by its unique ID")
    @Parameter(name = "id", description = "ID of the race to delete", required = true)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Race deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Race not found")
    })
    public ResponseEntity<Void> deleteRace(@PathVariable Integer id) {
        raceService.deleteRace(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/fastest-lap")
    @Operation(summary = "Get fastest lap of the race", description = "Fetches the fastest lap by going through all the cars")
    @Parameter(name = "id", description = "ID of the race to retrieve the fastest lap", required = true)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fastest lap retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Race not found/ There are no laps")
    })
    public ResponseEntity<Map<String, Object>> getFastestLap(@PathVariable Integer id) {
        List<Car> cars = raceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Race not found"))
                .getCars();
    
        if (cars.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    
        Car carWithFastestLap = cars.stream()
                .filter(car -> !car.getLapTimes().isEmpty())
                .flatMap(car -> car.getLapTimes().stream().map(lapTime -> Map.entry(car, lapTime)))
                .min(Map.Entry.comparingByValue(Double::compareTo))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No lap times found"));
    
        Double fastestLap = carWithFastestLap.getLapTimes().stream().min(Double::compareTo).orElse(Double.MAX_VALUE);
    
        if (!fastestLap.equals(lastFastestLaps.getOrDefault(id, Double.MAX_VALUE))) {
            lastFastestLaps.put(id, fastestLap);
    
            Map<String, Object> notification = Map.of(
                "fastestLap", fastestLap,
                "carId", carWithFastestLap.getId(),
                "drivername", carWithFastestLap.getDriver().getName()
            );
            messagingTemplate.convertAndSend("/topic/fastest-lap-update", notification);
        }
    
        Map<String, Object> response = new HashMap<>();
        response.put("fastestLap", fastestLap);
        response.put("carId", carWithFastestLap.getId());
        return ResponseEntity.ok(response);
    }
    



    // endpoint to get the total nmber of laps of the race given its id
    @GetMapping("/{id}/total-laps")
    @Operation(summary = "Get total laps of the race", description = "Fetches the total number of laps of the race")
    @Parameter(name = "id", description = "ID of the race to retrieve the total laps", required = true)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Total laps retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Race not found")
    })
    public ResponseEntity<Integer> getTotalLaps(@PathVariable Integer id) {
        Integer totalLaps = raceRepository.findById(id).get().getTotalLaps();
        return ResponseEntity.ok(totalLaps);
    }


    @PostMapping("/{id}/start")
    @Operation(summary = "Start a race", description = "Triggers the start of a race by sending a Kafka message")
    @Parameter(name = "id", description = "ID of the race to start", required = true)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Race started successfully"),
        @ApiResponse(responseCode = "404", description = "Race not found")
    })
    public ResponseEntity<String> startRace(@PathVariable Integer id) {

        // if this endpoint is called send a message to the kafka topic start_race with raceId and start_race: true
        Optional<Race> raceOptional = raceRepository.findById(id);

        if (raceOptional.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Race not found");
        }

        Race race = raceRepository.findById(id).orElseThrow(() -> new EntityNotFoundException());
        race.setRunning(true);
        raceService.updateRace(race);

        Map<String, Object> message = Map.of(
            "raceId", id,
            "start_race", true
        );

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String messageString = objectMapper.writeValueAsString(message);
            kafkaTemplate.send("start-race", messageString);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending message to Kafka");
        }

        return ResponseEntity.ok("Race started successfully");
        
    }

    @PostMapping("/{id}/stop")
    @Operation(summary = "Stop a race", description = "Triggers the stop of a race by sending a Kafka message")
    @Parameter(name = "id", description = "ID of the race to stop", required = true)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Race stopped successfully"),
        @ApiResponse(responseCode = "404", description = "Race not found")
    })
    public ResponseEntity<String> stopRace(@PathVariable Integer id) {
        Optional<Race> raceOptional = raceRepository.findById(id);

        if (raceOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Race not found");
        }

        Map<String, Object> message = Map.of(
            "raceId", id,
            "start_race", false  // Set to false to stop the race
        );

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String messageString = objectMapper.writeValueAsString(message);
            kafkaTemplate.send("start-race", messageString);  // Sending to the same topic as for start

            Race race = raceRepository.findById(id).orElseThrow(() -> new EntityNotFoundException());
            race.setRunning(false);
            raceService.updateRace(race);

            // create a red flag event
            RedFlag redFlag = new RedFlag();

            int currentLap = MAX_VALUE;

            // ITERATE OVER ALL CARS TO GET THE CURRENT LAP
            for (Car car : raceOptional.get().getCars()) {
                if (car.getCurrentLap() < currentLap) {
                    currentLap = car.getCurrentLap();
                }
            }

            redFlag.setLap(currentLap);
            redFlag.setTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
            redFlagService.createRedFlag(redFlag);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending message to Kafka");
        }

        return ResponseEntity.ok("Race stopped successfully");
    }

    @GetMapping("/{id}/running")
    public ResponseEntity<Boolean> isRaceRunning(@PathVariable Integer id) {
        Optional<Race> raceOptional = raceRepository.findById(id);

        if (raceOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }

        return raceOptional.get().getRunning() ? ResponseEntity.ok(true) : ResponseEntity.ok(false);

    }
    

}
