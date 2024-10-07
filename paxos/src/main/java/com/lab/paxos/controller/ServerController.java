package com.lab.paxos.controller;

import com.lab.paxos.model.network.communique.ServerStatusUpdate;
import com.lab.paxos.service.SocketService;
import com.lab.paxos.util.ServerStatusUtil;
import com.lab.paxos.util.SocketMessageUtil;
import com.lab.paxos.wrapper.AckMessageWrapper;
import com.lab.paxos.wrapper.SocketMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/server")
@Slf4j
public class ServerController {

    @Autowired
    private ServerStatusUtil serverStatusUtil;

    @Autowired
    private SocketService socketService;

    @Autowired
    private SocketMessageUtil socketMessageUtil;

    @GetMapping("/fail")
    public ResponseEntity<Boolean> failServer(@RequestParam(required = false) Integer port){

        if(serverStatusUtil.isFailed()) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

        if(port == null){
            serverStatusUtil.setFailed(true);
            log.info("{}: Server down", socketService.getAssignedPort());
            return ResponseEntity.ok(Boolean.valueOf(serverStatusUtil.isFailed()));
        }
        else{
            try{
                ServerStatusUpdate serverStatusUpdate = new ServerStatusUpdate(true, port, socketService.getAssignedPort());
                SocketMessageWrapper socketMessageWrapper = new SocketMessageWrapper(SocketMessageWrapper.MessageType.SERVER_STATUS_UPDATE, serverStatusUpdate);

                AckMessageWrapper ackMessageWrapper = socketMessageUtil.sendMessageToServer(port, socketMessageWrapper);
                log.info("Sending fail message to server {}", port);
                return ResponseEntity.ok(ackMessageWrapper.getAckServerStatusUpdate().isServerFailed());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
    }

    @GetMapping("/resume")
    public ResponseEntity<Boolean> resumeServer(@RequestParam(required = false) Integer port){
        if(port == null){
            serverStatusUtil.setFailed(false);
            log.info("{}: Server up & running", socketService.getAssignedPort());
            return ResponseEntity.ok(Boolean.valueOf(!serverStatusUtil.isFailed()));
        }
        else{

            if(serverStatusUtil.isFailed()) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

            try{
                ServerStatusUpdate serverStatusUpdate = new ServerStatusUpdate(false, port, socketService.getAssignedPort());
                SocketMessageWrapper socketMessageWrapper = new SocketMessageWrapper(SocketMessageWrapper.MessageType.SERVER_STATUS_UPDATE, serverStatusUpdate);

                AckMessageWrapper ackMessageWrapper = socketMessageUtil.sendMessageToServer(port, socketMessageWrapper);
                log.info("Sending resume message to server {}", port);
                return ResponseEntity.ok(!ackMessageWrapper.getAckServerStatusUpdate().isServerFailed());
            }
            catch (IOException e){
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }
        }
    }
}
