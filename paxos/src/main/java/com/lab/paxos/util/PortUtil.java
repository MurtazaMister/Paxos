package com.lab.paxos.util;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class PortUtil {

    @Value("${server.port.pool}")
    private String portPool;

    public int basePort(){
        return Integer.parseInt(portPool.split(",")[0]);
    }

    public List<Integer> portPoolGenerator(){
        log.info("PortPool: {}", portPool);

        List<Integer> PORT_POOL = new ArrayList<>();

        Arrays.stream(portPool.split(","))
                .map(String::trim)  // Trim whitespace
                .map(Integer::parseInt)  // Convert to Integer
                .forEach(PORT_POOL::add);

        return PORT_POOL;
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true; // Port is available
        } catch (Exception e) {
            return false; // Port unavailable
        }
    }

    public int findAvailablePort(@NotNull List<Integer> PORT_POOL) {
        for (int port : PORT_POOL) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        return -1;
    }
}
