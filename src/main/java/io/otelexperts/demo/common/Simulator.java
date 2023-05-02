package io.otelexperts.demo.common;


import jakarta.inject.Singleton;

import java.util.Random;

@Singleton
public class Simulator {
    private final Random random = new Random();

    public void generateLatency(int lower, int upper) {
        //int randomWithNextInt = random.nextInt(350, 500);
        int randomWithNextInt = 300;
        try {
            Thread.sleep(randomWithNextInt);
        } catch (InterruptedException e) {
        }
    }

    public void generateCPULoad() {

    }

    public void captureMemory() {

    }

    public void releaseMemory() {

    }

}
