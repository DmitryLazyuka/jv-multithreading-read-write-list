package core.basesyntax;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ReadWriteList<E> {
    private final List<E> list = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEnoughElements = lock.newCondition();

    public void add(E element) {
        lock.lock();
        try {
            list.add(element);
            notEnoughElements.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public E get(int index) {
        lock.lock();
        try {
            while (index < 0 || index >= list.size()) {
                try {
                    notEnoughElements.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return list.get(index);
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return list.size();
        } finally {
            lock.unlock();
        }
    }
}
