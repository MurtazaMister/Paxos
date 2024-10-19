package com.lab.paxos.util.PaxosUtil;

import com.lab.paxos.util.SocketMessageUtil;
import com.lab.paxos.wrapper.AckMessageWrapper;
import com.lab.paxos.wrapper.SocketMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class Sync {

    @Value("${server.population}")
    int serverPopulation;

    @Autowired
    @Lazy
    SocketMessageUtil socketMessageUtil;

    public void sync(int assignedPort, int ballotNumber, Long lastCommittedTransactionBlockId, String lastCommittedTransactionBlockHash, List<Integer> listNodesWithLatestLog, List<Integer> listNodesWithoutLatestLog) throws IOException, ExecutionException, InterruptedException {
        com.lab.paxos.networkObjects.communique.Sync sync = com.lab.paxos.networkObjects.communique.Sync.builder()
                .ballotNumber(ballotNumber)
                .lastCommittedTransactionBlockId(lastCommittedTransactionBlockId)
                .lastCommittedTransactionBlockHash(lastCommittedTransactionBlockHash)
                .listNodesWithLatestLog(listNodesWithLatestLog)
                .build();

        SocketMessageWrapper socketMessageWrapper = SocketMessageWrapper.builder()
                .type(SocketMessageWrapper.MessageType.SYNC)
                .sync(sync)
                .fromPort(assignedPort)
                .build();

        List<AckMessageWrapper> ackMessageWrapperList = socketMessageUtil.broadcast(socketMessageWrapper, listNodesWithoutLatestLog).get();
        log.info("SYNC acknowledged by {}/{} servers", listNodesWithoutLatestLog.size(), listNodesWithoutLatestLog.size());
    }

}
