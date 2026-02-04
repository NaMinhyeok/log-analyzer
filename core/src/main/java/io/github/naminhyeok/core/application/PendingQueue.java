package io.github.naminhyeok.core.application;

import java.util.List;

public interface PendingQueue<T> {

    boolean offer(T item);

    int offerAll(List<T> items);

    T take() throws InterruptedException;

    void markCompleted(T item);

    int size();

    boolean isPending(T item);
}
