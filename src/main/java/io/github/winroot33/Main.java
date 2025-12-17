package io.github.winroot33;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        var ringBuffer = new RingBuffer<String>(5);
        ringBuffer.putAll(List.of("1", "2", "3", "4"));
        ringBuffer.putAll(List.of("5", "6", "7"));
        System.out.println("Full: " + ringBuffer.isFull());

        ringBuffer.remove();
        ringBuffer.remove();

        System.out.println(ringBuffer.get(0));

        ringBuffer.remove();
        System.out.println("Empty: " + ringBuffer.isEmpty());

        ringBuffer.putAll(List.of("1", "2", "3", "4"));
        ringBuffer.putAll(List.of("5", "6", "7"));
        ringBuffer.clear();
        System.out.println("Empty after clear: " + ringBuffer.isEmpty());
    }
}