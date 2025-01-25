package ua.pt.ies.RaceFlow.Kafka.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ua.pt.ies.RaceFlow.Entities.Car;
import ua.pt.ies.RaceFlow.Kafka.entity.LapTimeData;
import ua.pt.ies.RaceFlow.Services.CarService;

@Service
public class LapTimesConsumer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CarService carService;

    @KafkaListener(topics = "lap-times", groupId = "group_id", properties = "spring.json.value.default.type=ua.pt.ies.RaceFlow.Kafka.entity.LapTimeData")
    public void consume(LapTimeData lapTimeData) {

        Integer driverID = lapTimeData.getDriverID();
        Double lapTime = lapTimeData.getLapTime();

        try {
            carService.addLapTime(driverID, lapTime);
            Car car = carService.getCarByDriverId(driverID);
            messagingTemplate.convertAndSend("/topic/lap-times", car);
        } catch (Exception e) {
            System.err.println("Error processing lap time data: " + e.getMessage());
        }
    }
}