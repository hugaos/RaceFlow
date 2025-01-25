package ua.pt.ies.RaceFlow.Kafka.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import ua.pt.ies.RaceFlow.Kafka.entity.DriverData;
import ua.pt.ies.RaceFlow.Services.DriverService;
import ua.pt.ies.RaceFlow.Entities.Driver;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
public class DriverConsumer {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private DriverService driverService;


    @KafkaListener(topics = "drivers", groupId = "group_id", properties = "spring.json.value.default.type=ua.pt.ies.RaceFlow.Kafka.entity.DriverData")

    public void consume(DriverData raceData) {
        // Retrieve the driver based on the ID in the received RaceData
        Driver driver = driverService.getDriverById(raceData.getDriverID());
        
        if (driver != null) {
           
            driver.setGap(raceData.getGap());
            driver.setHeartRate(raceData.getHeartRate());
            
            driverService.updateDriver(driver.getId(), driver);
            driverService.saveDriver(driver);
            updateDriverPositions();
            Map<String, Object> averageVelocityData = new HashMap<>();
            averageVelocityData.put("driverID", raceData.getDriverID());
            averageVelocityData.put("averageVelocity", raceData.getAverageVelocity());
            averageVelocityData.put("maxVelocity", raceData.getMaxVelocity());

            // Enviar dados no WebSocket
            Map<String, Object> combinedData = new HashMap<>();
            combinedData.put("driver", driver); // Driver completo
            combinedData.put("velocityInfo", averageVelocityData); // JSON com ID e velocidade média

            messagingTemplate.convertAndSend("/topic/drivers", combinedData);
        }
    }
    
    private void updateDriverPositions() {
        // Retrieve all drivers sorted by their gap
        List<Driver> allDrivers = driverService.getAllDrivers();

        allDrivers.sort(Comparator.comparingDouble(d -> d.getGap() != null ? d.getGap() : Double.MAX_VALUE));

        // Update position attribute based on sorted order
        for (int i = 0; i < allDrivers.size(); i++) {
            Driver driver = allDrivers.get(i);
            driver.setPosition(i + 1); // Position starts at 1
            driverService.saveDriver(driver); // Save the updated position
        }
    }     
}
