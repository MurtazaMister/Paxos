package com.lab.paxos.service.client;

import com.lab.paxos.config.client.ApiConfig;
import com.lab.paxos.service.ExitService;
import com.lab.paxos.util.PortUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@Slf4j
public class ValidationService {

    @Autowired
    private final RestTemplate restTemplate;

    @Autowired
    private ApiConfig apiConfig;

    @Autowired
    private ApiService apiService;

    @Value("${rest.server.url}")
    private String restServerUrl;

    @Value("${server.port.pool}")
    private String initPort;

    @Value("${rest.server.offset}")
    private String offset;

    @Autowired
    private PortUtil portUtil;

    @Autowired
    private ClientService clientService;

    public ValidationService(RestTemplate restTemplate, ApiConfig apiConfig) {
        this.restTemplate = restTemplate;
    }

    public Long identifyServer(String username) {

        Long id = clientService.getId(username);
        List<Integer> portsArray = portUtil.portPoolGenerator();

        int respectivePort = portsArray.get(0) + Integer.parseInt(offset) + Math.toIntExact(id) - 1;

        log.info("Connected to server port {}", respectivePort);

        apiConfig.setApiPort(respectivePort);
        apiConfig.setRestServerUrlWithPort(restServerUrl + ":" + apiConfig.getApiPort());

        return id;

    }

    public boolean validate(Long id, String password){

        if(apiService.validate(id, password) == true)
            return true;
        return false;

    }

}
