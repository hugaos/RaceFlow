package ua.pt.ies.RaceFlow.Kafka.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class LapTimeData {

    private int driverID;
    private Double lapTime;
    
}
