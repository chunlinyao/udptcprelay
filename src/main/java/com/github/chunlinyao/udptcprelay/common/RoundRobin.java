package com.github.chunlinyao.udptcprelay.common;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobin<E> {
    private final AtomicInteger next = new AtomicInteger(0);
    private final List<E> elements;

    public RoundRobin(List<E> list, Class<E> clazz) {
        elements = list;
    }

    public E get() {
        if (elements.size() > 0) {
            try {
                return elements.get(next.getAndIncrement() % elements.size());
            } catch (IndexOutOfBoundsException | ArithmeticException e) {
                return get();
            }
        } else {
            return null;
        }
    }

}
