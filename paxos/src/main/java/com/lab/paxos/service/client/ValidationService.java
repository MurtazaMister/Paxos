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
    private ExitService exitService;

    public ValidationService(RestTemplate restTemplate, ApiConfig apiConfig) {
        this.restTemplate = restTemplate;
    }

    public Long identifyServer(String username) {

        Long id = -1L;

        List<Integer> portsArray = portUtil.portPoolGenerator();

        int finalPort = -1;

        for(int port : portsArray) {
            try{
                String url = restServerUrl+":"+Integer.toString(port+Integer.parseInt(offset))+"/server/test";
                boolean up = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(url).toUriString(), Boolean.class);
                if(up) {
                    finalPort = port;
                    break;
                }
            }
            catch (HttpServerErrorException e) {
                continue;
            }
            catch(Exception e){
                continue;
            }
        }

        if(finalPort == -1){
            log.error("No servers available");
            exitService.exitApplication(0);
        }

        String url = restServerUrl+":"+Integer.toString(finalPort+Integer.parseInt(offset))+"/user/getId";

        log.info("Sending req: {}", url);

        try {

            id = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("username", username)
                    .toUriString(), Long.class);

            int respectivePort = portsArray.get(0) + Integer.parseInt(offset) + Math.toIntExact(id) - 1;

            log.info("Connected to server port {}", respectivePort);

            apiConfig.setApiPort(respectivePort);
            apiConfig.setRestServerUrlWithPort(restServerUrl + ":" + apiConfig.getApiPort());

        } catch (HttpServerErrorException e){
            if(e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE){
                log.error("Service unavailable : {}", e.getMessage());
            } else {
                log.error("Server error: {}", e.getMessage());
            }
        }
        catch (HttpClientErrorException e) {

            if(e.getStatusCode().value() == 404){
                return -1L;
            }
            else{
                log.error(e.getMessage());
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return id;

    }

    public boolean validate(Long id, String password){

        if(apiService.validate(id, password) == true)
            return true;
        return false;

    }

}
