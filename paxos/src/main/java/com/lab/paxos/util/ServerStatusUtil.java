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

}
