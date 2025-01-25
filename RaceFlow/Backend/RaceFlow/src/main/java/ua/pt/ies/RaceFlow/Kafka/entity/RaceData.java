package ua.pt.ies.RaceFlow.Kafka.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaceData {
    private int raceID;
    private int currentLap;
    private int fastestLap;
    private String weather;
    private Double trackTemp;

}
