package ua.pt.ies.RaceFlow.Kafka.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverData {
    private int driverID;
    private String timestamp;
    private Double velocity;
    private int heartRate;
    private Double gap;
    private int averageVelocity;
    private Double maxVelocity;
}