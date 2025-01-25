package ua.pt.ies.RaceFlow.Kafka.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ua.pt.ies.RaceFlow.Services.RaceService;
import ua.pt.ies.RaceFlow.Entities.Race;
import ua.pt.ies.RaceFlow.Kafka.entity.RaceData;



@Service
public class RaceConsumer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RaceService raceService;

    @KafkaListener(topics = "race", groupId = "group_id", properties = "spring.json.value.default.type=ua.pt.ies.RaceFlow.Kafka.entity.RaceData")
    public void consume(RaceData raceData) {
        Race race = raceService.getRaceById(1);

        if (race != null) {
            // Atualizar a volta atual
            race.setWeather(raceData.getWeather());
            raceService.createRace(race);  // Salvar mudanças na base de dados
        }

        // Broadcast para WebSocket para atualizações em tempo real
        messagingTemplate.convertAndSend("/topic/race", raceData);
    }
}

