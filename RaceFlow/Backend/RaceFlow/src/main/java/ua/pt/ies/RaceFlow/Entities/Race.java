package ua.pt.ies.RaceFlow.Entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "races")
public class Race {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;               // Race name or title
    private LocalDate date;            // Date of the race
    private String location;           // Location of the race
    private Integer duration;          // Total duration of the race in minutes
    private Integer totalLaps;  // Total number of laps
    private String weather;     // Weather conditions
    private Boolean running;
    @Column(nullable = true) 
    private Double trackTemperature;   // Track temperature during the race

    @OneToMany
    private List<Car> cars;            // List of cars participating

}
