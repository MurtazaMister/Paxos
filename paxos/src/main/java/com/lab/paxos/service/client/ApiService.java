package com.lab.paxos.service.client;

import com.lab.paxos.config.client.ApiConfig;
import com.lab.paxos.model.UserAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class ApiService {

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
        ResponseEntity<Boolean> response = restTemplate.exchange(url, HttpMethod.POST, request, Boolean.class);
        return response.getBody();
    }

    // balance check
    public Long balanceCheck(Long id){
        String url = apiConfig.getRestServerUrlWithPort()+"/user/balance";
        log.info("Sending req: {}", url);

        Long balance = 0L;

        try{
            balance = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("userId", id)
                    .toUriString(), Long.class);
        } catch (HttpClientErrorException e) {
            log.trace(e.getMessage());
        } catch (Exception e) {
            log.trace(e.getMessage());
        }

        return Long.parseLong(Long.toString(balance));
    }

    // fail current server
    public Boolean failServer(){
        String url = apiConfig.getRestServerUrlWithPort()+"/server/fail";
        log.info("Sending req: {}", url);

        Boolean failed = false;

        try{
            failed = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(url).toUriString(), Boolean.class);
        }
        catch (HttpClientErrorException e){
            log.trace(e.getMessage());
        }
        catch (Exception e) {
            log.trace(e.getMessage());
        }

        return failed;
    }
}
