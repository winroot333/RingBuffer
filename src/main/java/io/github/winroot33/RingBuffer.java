package io.github.winroot33;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;
import java.util.concurrent.locks.Condition;
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
    private final Lock locker;
    private final Condition notEmpty;
    private final Condition notFull;

    private int size;
    private int tail;
    private int head;

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
        notEmpty = locker.newCondition();
        notFull = locker.newCondition();
    }

    /**
     * Добавляет элемент в буффер. Ждет удаления элементов если буффер полон
     *
     * @param value Элемент для записи
     */
    public void put(T value) throws InterruptedException {
        locker.lock();
        try {
            while (isFull()) {
                notFull.await();
            }
            buffer[tail] = value;
            tail = getNextTailIndex();
            size++;
            notEmpty.signal();
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
     * Получает элемент из буффера, не удаляя его. Индексы с 0 до capacity -1.
     * Элемент по индексу 0 - самый старый. По индексу capacity -1 последний записанный, если существует.
     *
     * @param index Индекс элемента
     * @return Элемент по указанному индексу
     */
    @SuppressWarnings("unchecked")
    public T peek(int index) {
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
     * Берет значение из буффера. Получает и удаляет его. Ждет если буффер пуст
     *
     * @return Значение из буффера
     */
    @SuppressWarnings("unchecked")
    public T take() throws InterruptedException {
        locker.lock();
        try {
            while (isEmpty()) {
                notEmpty.await();
            }
            T value = (T) buffer[head];
            buffer[head] = null;
            head = getNextHeadIndex();
            size--;
            notFull.signal();
            return value;
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
            notFull.signalAll();
        } finally {
            locker.unlock();
        }
    }

    /**
     * Возвращает пуст ли буффер
     *
     * @return буффер пуст или нет
     */
    private boolean isEmpty() {
        return size == 0;
    }

    /**
     * Возвращает полон ли буффер
     *
     * @return буффер полон или нет
     */
    private boolean isFull() {
        return size == capacity;
    }
}
