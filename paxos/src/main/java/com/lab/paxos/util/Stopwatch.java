package com.lab.paxos.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class Stopwatch {
    public static String getDuration(LocalDateTime startTime, LocalDateTime endTime, String event) {
        Duration duration = Duration.between(startTime, endTime);
        return event+" time: "+duration.toNanos()/1000000000.0;
    }
}
