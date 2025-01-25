package ua.pt.ies.RaceFlow.Kafka.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarsData {
    private int carID;
    private int driverID;
    private List<Float> location;
    private List<Double> tyreTemp;
    private int currentLap;
    private String tyreType;
    private double currentSpeed;
    private int gear;
    private int rpm;
}