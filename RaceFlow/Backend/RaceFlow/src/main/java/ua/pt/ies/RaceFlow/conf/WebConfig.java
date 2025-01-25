package ua.pt.ies.RaceFlow.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Permitir todas as rotas que começam com /api
                        .allowedOrigins("http://localhost") // Permitir o frontend rodando no Nginx
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos HTTP permitidos
                        .allowedHeaders("*") // Permitir todos os headers
                        .allowCredentials(true)
                         .exposedHeaders("*") ; // Permitir cookies se necessário
            }
        };
    }
}
