package core.basesyntax;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteList<E> {
    private final List<E> list = new ArrayList<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Condition elementAdded = lock.writeLock().newCondition();

    public void add(E element) {
        lock.writeLock().lock();
        try {
            list.add(element);
            elementAdded.signalAll();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public E get(int index) {
        lock.readLock().lock();
        try {
            if (index >= 0 && index < list.size()) {
                return list.get(index);
            }
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            while (index < 0 || index >= list.size()) {
                try {
                    elementAdded.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            return list.get(index);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return list.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
