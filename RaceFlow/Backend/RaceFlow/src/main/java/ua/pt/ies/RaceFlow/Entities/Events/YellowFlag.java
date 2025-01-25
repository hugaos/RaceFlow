package ua.pt.ies.RaceFlow.Entities.Events;

import java.security.Timestamp;
import java.time.Duration;

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
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "yellowflags")
public class YellowFlag{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String motive;         // Motivo da bandeira amarela
    @Column(nullable = false)
    private Duration duration;      // Duração da bandeira amarela
    @Column(nullable = false)
    private int lap;                   // Volta em que a bandeira amarela foi acionada
    @Column(nullable = false)
    private List<Float> coordinates; // Coordenadas do local onde a bandeira amarela foi acionada
    @Column(nullable = false)
    private Timestamp timestamp; // Timestamp do momento em que a bandeira amarela foi acionada
}