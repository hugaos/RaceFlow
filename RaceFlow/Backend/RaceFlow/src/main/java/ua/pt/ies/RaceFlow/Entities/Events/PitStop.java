package ua.pt.ies.RaceFlow.Entities.Events;

import java.sql.Timestamp;
import java.time.Duration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.pt.ies.RaceFlow.Entities.Driver;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pitstops")
public class PitStop{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id", referencedColumnName = "id", unique = false)
    @JsonBackReference
    private Driver driver; // Piloto que realizou o pit stop
    @Column(nullable = false)
    private Duration duration; // Tempo que o piloto permaneceu no pit stop em segundos
    @Column(nullable = false)
    private int lap;                   // Volta em que o pit stop foi realizado
    @Column(nullable = false)
    private Timestamp timestamp; // Timestamp do momento em que o pit stop foi realizado
    @Column(nullable = false)
    private String new_tyre;

    @JsonGetter("driverId")
    public Integer getDriverId() {
        return driver != null ? driver.getId() : null;
    }
}