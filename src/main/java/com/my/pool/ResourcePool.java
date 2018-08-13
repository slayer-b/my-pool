package com.my.pool;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class ResourcePool<R> {

    private final ConcurrentLinkedQueue<R> resources;
    private boolean ready = false;

    public ResourcePool() {
        resources = new ConcurrentLinkedQueue<>();
    }

    public void open() {
        ready = true;
    }

    public boolean isOpen() {
        return ready;
    }

    public void close() {
        checkReady();
        ready = false;
    }

    public R acquire() throws InterruptedException {
        checkReady();
        R poll = resources.poll();
        if (poll == null) {
            poll = getResource();
        }
        return poll;
    }

    public R acquire(long timeout, TimeUnit timeUnit)
            throws InterruptedException {
        checkReady();
        R poll = resources.poll();
        if (poll == null) {
            poll = getResource(timeout, timeUnit);
        }
        return poll;
    }

    public void release(R resource) {
        checkReady();
        synchronized (this) {
            this.notify();
        }
        resources.offer(resource);
    }

    public boolean add(R resource) {
        checkReady();
        synchronized (this) {
            this.notify();
        }
        return resources.offer(resource);
    }

    public boolean remove(R resource) {
        checkReady();
        return resources.remove(resource);
    }

    private void checkReady() {
        if (!ready) {
            throw new RuntimeException("Resource is not ready");
        }
    }

    private R getResource() throws InterruptedException {
        synchronized (this) {
            this.wait();
        }
        R poll = resources.poll();
        if (poll == null) {
            poll = getResource();
        }
        return poll;
    }

    private R getResource(long timeout, TimeUnit timeUnit) throws InterruptedException {
        synchronized (this) {
            timeUnit.timedWait(this, timeout);
        }
        return resources.poll();
    }

}
