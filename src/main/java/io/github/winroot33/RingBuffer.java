package io.github.winroot33;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Реализация класса кругового буффера
 *
 * @param <T> Тим элементов
 */
@RequiredArgsConstructor
public class RingBuffer<T> {
    private final Object[] buffer;
    @Getter
    private final int capacity;
    private int size;
    private int tail;
    private int head;
    private Lock locker;

    /**
     * Создает RingBuffer
     *
     * @param capacity Размер буффера, размер внутреннего массива. Нельзя изменить в дальнейшем.
     */
    public RingBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }

        this.capacity = capacity;
        size = 0;
        tail = 0;
        head = 0;
        buffer = new Object[capacity];
        locker = new ReentrantLock();
    }

    /**
     * Добавляет элемент в буффер. При переполнении буффера перезаписывает старые элементы.
     *
     * @param value Элемент для записи
     */
    public void put(T value) {
        locker.lock();
        try {
            if (isFull()) {
                buffer[tail] = value;
                head = getNextHeadIndex();
            } else {
                buffer[tail] = value;
                size++;
            }
            tail = getNextTailIndex();
        } finally {
            locker.unlock();
        }

    }

    private int getNextHeadIndex() {
        return (head + 1) % capacity;
    }

    private int getNextTailIndex() {
        return (tail + 1) % capacity;
    }

    /**
     * Добавляет список значений в буффер
     *
     * @param values Список значений для добавления
     */
    public void putAll(List<T> values) {
        locker.lock();
        try {
            values.forEach(this::put);
        } finally {
            locker.unlock();
        }
    }

    /**
     * Получает элемент из буффера. Индексы с 0 до capacity -1.
     * Элемент по индексу 0 - самый старый. По индексу capacity -1 последний записанный, если существует.
     *
     * @param index Индекс элемента
     * @return Элемент по указанному индексу
     */
    @SuppressWarnings("unchecked")
    public T get(int index) {
        locker.lock();
        try {
            if (isEmpty()) {
                throw new NoSuchElementException("Buffer is empty");
            }
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException();
            }

            var calculatedIndex = (head + index) % capacity;
            return (T) buffer[calculatedIndex];
        } finally {
            locker.unlock();
        }
    }

    /**
     * Удаляет элемент из буффера. Удаляет самый старый элемент по принципу FIFO
     */
    public void remove() {
        locker.lock();
        try {
            if (isEmpty()) {
                throw new NoSuchElementException("Buffer is empty");
            }
            buffer[head] = null;
            size--;
            head = getNextHeadIndex();
        } finally {
            locker.unlock();
        }
    }

    /**
     * Полностью очищает буффер
     */
    public void clear() {
        locker.lock();
        try {
            for (int i = 0; i < capacity; i++) {
                buffer[i] = null;
            }
            size = 0;
            head = 0;
            tail = 0;
        } finally {
            locker.unlock();
        }
    }

    /**
     * Возвращает пуст ли буффер
     *
     * @return буффер пуст или нет
     */
    public boolean isEmpty() {
        locker.lock();
        try {

            return size == 0;
        } finally {
            locker.unlock();
        }
    }

    /**
     * Возвращает полон ли буффер
     *
     * @return буффер полон или нет
     */
    public boolean isFull() {
        locker.lock();
        try {
            return size == capacity;
        } finally {
            locker.unlock();
        }
    }
}
