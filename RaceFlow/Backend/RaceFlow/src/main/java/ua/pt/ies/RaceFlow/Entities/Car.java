package ua.pt.ies.RaceFlow.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;



@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String teamName;

    @Column(nullable = false)
    private String teamColor;

    @ElementCollection
    @Column(name = "tyre_temp")
    private List<Double> tyreTemp; // Changed to List<Double> for compatibility with JPA

    @ElementCollection
    @Column(name = "location")
    private List<Float> location;

    @Column(nullable = false)
    private int rpm;

    @Column(nullable = false)
    private int currentLap;

    @Column(nullable = false)
    private String tyreType;

    @Column(nullable = false)
    private int gear;

    @Column(nullable = false)
    private double currentSpeed;

    //lap times column in seconds
    @ElementCollection
    @Column
    private List<Double> lapTimes = new ArrayList<>();


    // Relationship with Driver entity, 1 car to 1 driver, column name is driverNumber
    @OneToOne(fetch = FetchType.EAGER) // Carrega o Driver automaticamente
    @JoinColumn(name = "driver_number", referencedColumnName = "driverNumber")
    private Driver driver;

    @Column(nullable = true)
    private String image_url;
}
