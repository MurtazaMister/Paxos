package com.lab.paxos.service.client;

import com.lab.paxos.config.client.ApiConfig;
import com.lab.paxos.model.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiConfig apiConfig;

    // validating a client with username and password
    public Boolean validate(Long id, String password){
        UserAccount ua = new UserAccount(id, password);
        String url = apiConfig.getRestServerUrlWithPort()+"/user/validate";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserAccount> request = new HttpEntity<>(ua, headers);
        ResponseEntity<Boolean> response = restTemplate.exchange(url, HttpMethod.POST, request, Boolean.class);
        return response.getBody();
    }
}
