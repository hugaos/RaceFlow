package ua.pt.ies.RaceFlow.Entities;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ua.pt.ies.RaceFlow.Entities.Events.PitStop;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String teamName;

    @Column(nullable = true)  // Change this if you want it to be non-nullable
    private String photo;

    @Column(nullable = false)
    private String countryCode; // Ensure this is a String

    @Column(nullable = false)
    private String acronym;

    @Column(unique = true, nullable = false)
    private Integer driverNumber;

    @Column(nullable = true)
    private Integer racePoints; // Alterado de int para Integer

    @Column(nullable = true)
    private Double gap; // Renomeado de lap_time para gap

    @Column(nullable = true)
    private Double fastest_lap; // Alterado de double para Double

    @Column(nullable = false, columnDefinition = "varchar(255) default '#000000'")
    private String teamColour;

    @Column(nullable = true)  // Change this if you want it to be non-nullable
    private Integer heartRate; // It's common to use Integer for nullable fields

    @OneToMany(mappedBy = "driver", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<PitStop> pitStops;

    @Column(nullable = true)
    private Integer position;
}


