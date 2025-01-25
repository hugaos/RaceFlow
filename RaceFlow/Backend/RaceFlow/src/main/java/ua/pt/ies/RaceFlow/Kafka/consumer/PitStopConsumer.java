package ua.pt.ies.RaceFlow.Kafka.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ua.pt.ies.RaceFlow.Entities.Events.PitStop;
import ua.pt.ies.RaceFlow.Entities.Car;
import ua.pt.ies.RaceFlow.Entities.Driver;
import ua.pt.ies.RaceFlow.Kafka.entity.PitstopData;
import ua.pt.ies.RaceFlow.Services.Events.PitStopService;
import ua.pt.ies.RaceFlow.Services.CarService;
import ua.pt.ies.RaceFlow.Services.DriverService;

import java.util.Date;
import java.sql.Timestamp;
import java.time.Duration;

@Service
public class PitStopConsumer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PitStopService pitStopService;

    @Autowired
    private DriverService driverService;

    @Autowired
    private CarService carService;

    @KafkaListener(
        topics = "pitstop",
        groupId = "group_id",
        properties = "spring.json.value.default.type=ua.pt.ies.RaceFlow.Kafka.entity.PitstopData"
    )
    public void consume(PitstopData pitstopData) {
        System.out.println("Consumed message: " + pitstopData);

        try {
            // Retrieve the Driver object
            Driver driver = driverService.getDriverById(pitstopData.getDriverID());
            if (driver == null) {
                System.err.println("Driver with ID " + pitstopData. getDriverID() + " not found.");
                return;
            }

            // Create a new PitStop event
            PitStop pitstop = new PitStop();
            pitstop.setDriver(driver);
            pitstop.setDuration(Duration.ofSeconds(pitstopData.getDuration()));
            pitstop.setLap(pitstopData.getLap()); // Set the lap from Kafka message
            pitstop.setTimestamp(new Timestamp(new Date().getTime()));
            pitstop.setNew_tyre(pitstopData.getNew_tyre());

            Car car = carService.getCarById(pitstopData.getDriverID());
            if (car == null) {
                System.err.println("Car with ID " + pitstopData.getDriverID() + " not found.");
                return;
            }
            car.setLocation(pitstopData.getLocation());
            car.setTyreType(pitstopData.getNew_tyre());

            // Persist the PitStop event
            PitStop savedPitStop = pitStopService.createPitStop(pitstop);
            System.out.println("Saved PitStop event to DB: " + savedPitStop);

            // Send the event via WebSocket
            messagingTemplate.convertAndSend("/topic/pitstop", savedPitStop);
            System.out.println("Broadcasted PitStop event to WebSocket.");
        } catch (Exception e) {
            System.err.println("Error processing pitstop message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
