package com.lab.paxos.util.PaxosUtil;

import com.lab.paxos.model.Transaction;
import com.lab.paxos.model.TransactionBlock;
import com.lab.paxos.model.UserAccount;
import com.lab.paxos.networkObjects.acknowledgements.AckMessage;
import com.lab.paxos.networkObjects.communique.Decide;
import com.lab.paxos.repository.UserAccountRepository;
import com.lab.paxos.service.PaxosService;
import com.lab.paxos.util.PortUtil;
import com.lab.paxos.util.Stopwatch;
import com.lab.paxos.wrapper.AckMessageWrapper;
import com.lab.paxos.wrapper.SocketMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class Commit {

    @Autowired
    @Lazy
    PaxosService paxosService;
    @Autowired
    private PortUtil portUtil;
    @Autowired
    @Lazy
    private UserAccountRepository userAccountRepository;

    public void commit(int assignedPort, ObjectInputStream in, ObjectOutputStream out, SocketMessageWrapper socketMessageWrapper) throws IOException {
        LocalDateTime startTime = LocalDateTime.now();
        Decide decide = socketMessageWrapper.getDecide();

        TransactionBlock transactionBlock = decide.getTransactionBlock();

        paxosService.setAcceptNum(null);
        paxosService.setPreviousTransactionBlock(null);

        List<Integer> portsArray = portUtil.portPoolGenerator();
        int currentClientId = assignedPort - portsArray.get(0) + 1;

        for(Transaction transaction : transactionBlock.getTransactions()) {
            if((!transaction.getIsMine()) || (transaction.getIsMine() && (transaction.getSenderId() != currentClientId))) {

                // Execute transactions
                //      If isMine = true
                //          Execute the transactions where sender id != current id
                //      If isMine = false
                //          Execute all transactions

                UserAccount senderAccount = userAccountRepository.findById(transaction.getSenderId()).orElse(null);
                UserAccount receiverAccount = userAccountRepository.findById(transaction.getReceiverId()).orElse(null);

                if(senderAccount == null || receiverAccount == null){ continue; }

                long senderBalance = senderAccount.getBalance() - transaction.getAmount();
                senderAccount.setBalance(senderBalance);
                senderAccount.setEffectiveBalance(senderBalance);

                long receiverBalance = receiverAccount.getBalance() + transaction.getAmount();
                receiverAccount.setBalance(receiverBalance);
                receiverAccount.setEffectiveBalance(receiverBalance);

                userAccountRepository.save(senderAccount);
                userAccountRepository.save(receiverAccount);

            }
        }

        AckMessage ackMessage = AckMessage.builder()
                .message("Commit successful")
                .build();

        AckMessageWrapper ackMessageWrapper = AckMessageWrapper.builder()
                .type(AckMessageWrapper.MessageType.ACK_MESSAGE)
                .ackMessage(ackMessage)
                .fromPort(socketMessageWrapper.getToPort())
                .toPort(socketMessageWrapper.getFromPort())
                .build();

        out.writeObject(ackMessageWrapper);

        log.info("Sent accepted to server {}: {}", ackMessageWrapper.getToPort(), ackMessage);

        out.flush();
        LocalDateTime currentTime = LocalDateTime.now();
        log.info("{}", Stopwatch.getDuration(startTime, currentTime, "Commit"));

    }

}