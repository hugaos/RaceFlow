package ua.pt.ies.RaceFlow;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class RaceFlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(RaceFlowApplication.class, args);
	}

	@Bean
	 public NewTopic topic() {
		return TopicBuilder.name("drivers").partitions(1).replicas(1).build();
	 }

	@Bean
	 public NewTopic topicCars() {
		return TopicBuilder.name("cars").partitions(1).replicas(1).build();
	}

}
