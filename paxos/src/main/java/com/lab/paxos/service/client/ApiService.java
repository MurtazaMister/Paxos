package com.lab.paxos.service.client;

import com.lab.paxos.config.client.ApiConfig;
import com.lab.paxos.dto.TransactionDTO;
import com.lab.paxos.dto.ValidateUserDTO;
import com.lab.paxos.model.Transaction;
import com.lab.paxos.model.UserAccount;
import com.lab.paxos.service.ExitService;
import com.lab.paxos.util.PortUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ApiService {

    @Value("${rest.server.offset}")
    private String offset;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiConfig apiConfig;

    @Autowired
    private PortUtil portUtil;

    @Value("${rest.server.url}")
    private String restServerUrl;
    @Autowired
    @Lazy
    private ClientService clientService;


    // validating a client with userId and password
    public Boolean validate(Long id, String password){
        ValidateUserDTO validateUserDTO = new ValidateUserDTO(id, password);
        String url = apiConfig.getRestServerUrlWithPort()+"/user/validate";

        log.info("Sending req: {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ValidateUserDTO> request = new HttpEntity<>(validateUserDTO, headers);

        try{
            ResponseEntity<Boolean> response = restTemplate.exchange(url, HttpMethod.POST, request, Boolean.class);

            if(response.getStatusCode() == HttpStatus.OK){
                return response.getBody();
            }
//            else if(response.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE){
//                log.error("{}: Service Unavailable", response.getStatusCode());
//                return false;
//            }
        } catch (HttpServerErrorException e) {
            if(e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE){
                log.error("Service unavailable : {}", e.getMessage());
            } else {
                log.error("Server error: {}", e.getMessage());
            }
        } catch (HttpClientErrorException e) {
            log.error("Client error: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Other error: {}", e.getMessage());
        }
        return false;
    }

    public Long balanceCheck(String username) {
        Long id = clientService.getId(username);

        List<Integer> portsArray = portUtil.portPoolGenerator();

        int respectivePort = portsArray.get(0) + Integer.parseInt(offset) + Math.toIntExact(id) - 1;

        String url = restServerUrl+":"+respectivePort+"/user/balance";

        return balanceCheck(id, url);
    }

    // balance check
    public Long balanceCheck(Long id) {
        String url = apiConfig.getRestServerUrlWithPort()+"/user/balance";
        return balanceCheck(id, url);
    }

    public Long balanceCheck(Long id, String url){
        log.info("Sending req: {}", url);

        Long balance = null;

        try{
            balance = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("userId", id)
                    .toUriString(), Long.class);
            return Long.parseLong(Long.toString(balance));
        }
        catch (HttpServerErrorException e) {
            if(e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE){
                log.error("Service unavailable : {}", e.getMessage());
            } else {
                log.error("Server error: {}", e.getMessage());
            }
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

    public void failServer(Integer port, String url){
        log.info("Sending req: {} {}", url, (port!=null)?" for port "+port:"");
        Boolean failed = false;

        try{
            if(port == null) failed = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(url).toUriString(), Boolean.class);
            else failed = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(url).queryParam("port", port).toUriString(), Boolean.class);

            log.info("Server at port {}'s status = {}", (port!=null)?port:(apiConfig.getApiPort()-Integer.parseInt(offset)), (failed)?"failed":"up & running");
        }
        catch (HttpServerErrorException e) {
            if(e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE){
                log.error("Service unavailable : {}", e.getMessage());
            } else {
                log.error("Server error: {}", e.getMessage());
            }
        }
        catch (HttpClientErrorException e){
            log.error(e.getMessage());
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void failServer(Integer port){
        String url = apiConfig.getRestServerUrlWithPort()+"/server/fail";
        failServer(port, url);
    }

    public void resumeServer(Integer port, String url){
        log.info("Sending req: {} {}", url, (port!=null)?" for port "+port:"");
        Boolean resumed = false;

        try{
            if(port == null) resumed = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(url).toUriString(), Boolean.class);
            else resumed = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(url).queryParam("port", port).toUriString(), Boolean.class);

            log.info("Server at port {}'s status = {}", (port!=null)?port:(apiConfig.getApiPort()-Integer.parseInt(offset)), (!resumed)?"failed":"up & running");
        }
        catch (HttpServerErrorException e) {
            if(e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE){
                log.error("Service unavailable : {}", e.getMessage());
            } else {
                log.error("Server error: {}", e.getMessage());
            }
        }
        catch (HttpClientErrorException e){
            log.error(e.getMessage());
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void resumeServer(Integer port){
        String url = apiConfig.getRestServerUrlWithPort()+"/server/resume";
        resumeServer(port, url);
    }

    public Transaction transact(String sName, String rName, Long amount, String url){
        log.info("Sending req: {}", url);

        TransactionDTO transactionDTO = TransactionDTO.builder()
                .unameSender(sName)
                .unameReceiver(rName)
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TransactionDTO> request = new HttpEntity<>(transactionDTO, headers);

        try{
            ResponseEntity<Transaction> response = restTemplate.exchange(url, HttpMethod.POST, request, Transaction.class);

            if(response.getStatusCode() == HttpStatus.OK){
                return response.getBody();
            }
        }
        catch (HttpServerErrorException e) {
            log.error(e.getMessage());
        }
        catch (HttpClientErrorException e){
            log.error(e.getMessage());
        }
        catch(Exception e){
            log.trace(e.getMessage());
        }
        return null;
    }

    public Transaction transact(String sName, String rName, Long amount){
        String url = apiConfig.getRestServerUrlWithPort()+"/transaction";
        return transact(sName, rName, amount, url);
    }
}
