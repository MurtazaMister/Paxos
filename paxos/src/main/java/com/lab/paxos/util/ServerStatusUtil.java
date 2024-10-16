package com.lab.paxos.util;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter
@Setter
public class ServerStatusUtil {

    private boolean failed = false;

    public void setFailed(boolean failed) {
        boolean previousFailed = this.failed;
        this.failed = failed;

        if(previousFailed && !failed){
            log.info("Triggering SYNC for log consistency");

            // clean up the acceptnum and acceptval values
            // as many rounds might already have happened and some transactions from its block
            // might already have committed

            // SYNC code


        }
    }

}
