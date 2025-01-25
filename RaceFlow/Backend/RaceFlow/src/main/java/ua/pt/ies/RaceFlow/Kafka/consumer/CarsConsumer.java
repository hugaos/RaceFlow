package ua.pt.ies.RaceFlow.Kafka.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import ua.pt.ies.RaceFlow.Kafka.entity.CarsData;
import ua.pt.ies.RaceFlow.Services.CarService;
import ua.pt.ies.RaceFlow.Entities.Car;


@Service 
public class CarsConsumer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CarService carService;

    @KafkaListener(topics = "cars", groupId = "group_id", properties = "spring.json.value.default.type=ua.pt.ies.RaceFlow.Kafka.entity.CarsData")

    public void consume(CarsData carsData) {
        Car car = carService.getCarById(carsData.getDriverID());

            car.setLocation(carsData.getLocation());
            car.setTyreTemp(carsData.getTyreTemp());
            car.setCurrentLap(carsData.getCurrentLap());
            car.setTyreType(carsData.getTyreType());
            car.setCurrentSpeed(carsData.getCurrentSpeed());
            car.setGear(carsData.getGear());
            car.setRpm(carsData.getRpm());
            carService.updateCar(car.getId(), car);
            carService.saveCar(car);

            CarsData new_data = new CarsData( car.getId(),car.getDriver().getId(), car.getLocation(), car.getTyreTemp(), car.getCurrentLap(), car.getTyreType(), car.getCurrentSpeed(), car.getGear(), car.getRpm());
            messagingTemplate.convertAndSend("/topic/cars", new_data);
    }

}
