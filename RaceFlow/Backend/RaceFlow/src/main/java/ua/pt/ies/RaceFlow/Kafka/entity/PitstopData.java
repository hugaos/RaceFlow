package ua.pt.ies.RaceFlow.Kafka.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PitstopData {
    private int driverID;
    private int lap;
    private String motive;  
    private int duration;
    private List<Float> location; 
    private String new_tyre;
}