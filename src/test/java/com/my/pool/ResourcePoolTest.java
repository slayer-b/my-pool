package com.my.pool;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class ResourcePoolTest {

    private ResourcePool<Integer> pool;

    @Before
    public void before() {
        pool = new ResourcePool<>();
    }

    @Test(expected = RuntimeException.class)
    public void testAcquireNotReady() throws InterruptedException {
        pool.acquire();
    }

    @Test(timeout = 5000)
    public void testAcquireReadyNoResource() throws InterruptedException {
        pool.open();

        Integer acquired = pool.acquire(3, TimeUnit.SECONDS);

        assertEquals(null, acquired);
    }

    @Test(timeout = 5000)
    public void testAcquireReadyResource() throws InterruptedException {
        pool.open();
        Integer resource = 3;
        pool.add(resource);

        Integer acquired = pool.acquire(3, TimeUnit.SECONDS);

        assertEquals(resource, acquired);
    }

    @Test
    public void testAcquireReadyRemove() throws InterruptedException {
        pool.open();
        Integer resource = 3;
        pool.add(resource);
        pool.remove(resource);

        Integer acquired = pool.acquire(3, TimeUnit.SECONDS);

        assertEquals(null, acquired);
    }

    @Test
    public void testMultiThreads() throws InterruptedException {
        pool.open();
        Integer resource1 = 1;
        Integer resource2 = 2;
        pool.add(resource1);
        pool.add(resource2);

        Thread thread1 = acquireResourceFor(500);
        Thread thread2 = acquireResourceFor(600);
        Thread thread3 = acquireResourceFor(700);

        pool.acquire();

        thread1.join();
        thread2.join();
        thread3.join();
    }

    private Thread acquireResourceFor(long waitTime) throws InterruptedException {
        final Object monitor = new Object();
        Thread thread = new Thread(() -> {
            try {
                Integer acquired = pool.acquire();
                synchronized (monitor) {
                    System.out.println(monitor);
                    monitor.wait(waitTime);
                }
                pool.release(acquired);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        return thread;
    }

}
