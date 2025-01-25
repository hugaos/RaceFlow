package ua.pt.ies.RaceFlow.Services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.List;

@Service
public class ExternalAPIService {

    private final RestTemplate restTemplate;

    public ExternalAPIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

   public Map<String, Object> fetchDriverData(int driverNumber) {
        URI uri = UriComponentsBuilder.fromUriString("https://api.openf1.org/v1/drivers")
            .queryParam("session_key", "9636")
            .queryParam("driver_number", driverNumber)
            .build()
            .toUri();

        Map<String, Object>[] response = restTemplate.getForObject(uri, Map[].class);
        if (response != null && response.length > 0) {
            return response[0];
        }
        return null;    
    }

    // Método para buscar todos os drivers da sessão mais recente
    public List<Map<String, Object>> fetchAllDrivers() {
        URI uri = UriComponentsBuilder.fromUriString("https://api.openf1.org/v1/drivers")
                .queryParam("session_key", "9636")
                .build()
                .toUri();

        Map<String, Object>[] response = restTemplate.getForObject(uri, Map[].class);
        return response != null ? List.of(response) : List.of();
    }

}