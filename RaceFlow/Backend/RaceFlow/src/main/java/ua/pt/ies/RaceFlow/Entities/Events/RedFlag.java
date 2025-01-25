package ua.pt.ies.RaceFlow.Entities.Events;


import java.sql.Timestamp;

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


// Evento específico para bandeira vermelha
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "redflags")
public class RedFlag{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private int lap;                   // Volta em que a bandeira vermelha foi acionada
    @Column(nullable = false)
    private Timestamp timestamp; // Timestamp do momento em que a bandeira vermelha foi acionada
}
