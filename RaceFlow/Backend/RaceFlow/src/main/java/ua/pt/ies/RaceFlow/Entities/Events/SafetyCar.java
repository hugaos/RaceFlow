package ua.pt.ies.RaceFlow.Entities.Events;

import java.time.Duration;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "safety_car")
public class SafetyCar{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private Duration duration;          // Duração do Safety Car na pista
    @Column(nullable = false)
    private Integer averageSpeed;   // Velocidade média do Safety Car durante o evento
    @Column(nullable = false)
    private Integer lap;                    // Volta em que o Safety Car foi acionado
    @Column(nullable = false)
    private List<Float> coordinates; // Coordenadas do local onde o Safety Car foi acionado

}
