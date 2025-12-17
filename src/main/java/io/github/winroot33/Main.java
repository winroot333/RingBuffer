package io.github.winroot33;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        var ringBuffer = new RingBuffer<String>(5);
        var threadPool = Executors.newFixedThreadPool(4);

        // Производитель
        threadPool.submit(() -> {
            try {
                for (int i = 0; i <= 20; i++) {
                    ringBuffer.put("Message " + i);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Потребители
        for (int i = 0; i < 3; i++) {
            threadPool.submit(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        String message = ringBuffer.take();
                        System.out.printf("Thread: %s taken element with message: %s\n",
                                Thread.currentThread().getName(), message);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        Thread.sleep(10000);

        if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
            threadPool.shutdownNow();
        }
    }
}