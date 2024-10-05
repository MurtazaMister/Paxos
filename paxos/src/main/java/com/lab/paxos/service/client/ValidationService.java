package com.lab.paxos.service.client;

import com.lab.paxos.config.client.ApiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class ValidationService {
    private final RestTemplate restTemplate;
    private ApiConfig apiConfig;
    private ApiService apiService;

    @Value("${rest.server.url}")
    private String restServerUrl;

    @Value("${server.port.pool}")
    private String initPort;

    @Value("${rest.server.offset}")
    private String offset;

    public ValidationService(RestTemplate restTemplate, ApiConfig apiConfig) {
        this.restTemplate = restTemplate;
    }

    public Long identifyServer(String username) {
        int firstPort = Integer.parseInt(initPort.split(",")[0]);
        String url = restServerUrl+":"+Integer.toString(firstPort+Integer.parseInt(offset))+"/user/getId";
        log.info("Sending req: {}", url);
        try {
            Long id = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("username", username)
                    .toUriString(), Long.class);

            int respectivePort = firstPort + Integer.parseInt(offset) + Math.toIntExact(id) - 1;
            log.info("Connected to server port {}", respectivePort);
            apiConfig.setApiPort(respectivePort);
            apiConfig.setRestServerUrlWithPort(restServerUrl + ":" + apiConfig.getApiPort());

            return id;
        } catch (HttpClientErrorException e) {
            if(e.getStatusCode().value() == 404) return null;
            else log.trace(e.getMessage());
        } catch (Exception e) {
            log.trace(e.getMessage());
        }
        return -1L;
    }

    public boolean validate(Long id, String password){
        return apiService.validate(id, password);
    }

}
