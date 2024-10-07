package com.lab.paxos.service.client;

import com.lab.paxos.config.client.ApiConfig;
import com.lab.paxos.model.UserAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class ApiService {

    @Value("${rest.server.offset}")
    private String offset;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiConfig apiConfig;

    // validating a client with userId and password
    public Boolean validate(Long id, String password){
        UserAccount ua = new UserAccount(id, password);
        String url = apiConfig.getRestServerUrlWithPort()+"/user/validate";

        log.info("Sending req: {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserAccount> request = new HttpEntity<>(ua, headers);

        try{
            ResponseEntity<Boolean> response = restTemplate.exchange(url, HttpMethod.POST, request, Boolean.class);

            if(response.getStatusCode() == HttpStatus.OK){
                return response.getBody();
            }
            else if(response.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE){
                log.error("{}: Service Unavailable", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    // balance check
    public Long balanceCheck(Long id){
        String url = apiConfig.getRestServerUrlWithPort()+"/user/balance";
        log.info("Sending req: {}", url);

        Long balance = null;

        try{
            balance = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("userId", id)
                    .toUriString(), Long.class);
            return Long.parseLong(Long.toString(balance));
        }
        catch (HttpClientErrorException e) {
            log.error("Http error while fetching balance: {}", e.getStatusCode());
        }
        catch (ResourceAccessException e) {
            log.error("Could not access server: {}", e.getMessage());
        }
        catch (Exception e) {
            log.error("{}", e.getMessage());
        }

        return null;

    }

    public void failServer(Integer port){
        String url = apiConfig.getRestServerUrlWithPort()+"/server/fail";
        log.info("Sending req: {} {}", url, (port!=null)?" for port "+port:"");
        Boolean failed = false;

        try{
            if(port == null) failed = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(url).toUriString(), Boolean.class);
            else failed = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(url).queryParam("port", port).toUriString(), Boolean.class);

            log.info("Server at port {}'s status = {}", (port!=null)?port:(apiConfig.getApiPort()-Integer.parseInt(offset)), (failed)?"failed":"up & running");
        }
        catch (HttpClientErrorException e){
            log.trace(e.getMessage());
        }
        catch (Exception e) {
            log.trace(e.getMessage());
        }
    }

    public void resumeServer(Integer port){
        String url = apiConfig.getRestServerUrlWithPort()+"/server/resume";
        log.info("Sending req: {} {}", url, (port!=null)?" for port "+port:"");
        Boolean resumed = false;

        try{
            if(port == null) resumed = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(url).toUriString(), Boolean.class);
            else resumed = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(url).queryParam("port", port).toUriString(), Boolean.class);

            log.info("Server at port {}'s status = {}", (port!=null)?port:(apiConfig.getApiPort()-Integer.parseInt(offset)), (!resumed)?"failed":"up & running");
        }
        catch (HttpClientErrorException e){
            log.trace(e.getMessage());
        }
        catch (Exception e) {
            log.trace(e.getMessage());
        }
    }
}
