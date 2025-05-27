import java.util.*;
import java.util.concurrent.locks.*;

public class ProducerConsumer {
    static class DeadlockDemo {
        // resurse shared
        private final List<Integer> bufferA = Collections.synchronizedList(new ArrayList<>());
        private final List<Integer> bufferB = Collections.synchronizedList(new ArrayList<>());

        //lockuri intrinsice, vom folosi cu synchronized
        private final Object lockA = new Object();
        private final Object lockB = new Object();

        public void start() {
            Thread p1 = new Thread(() -> {    // producer 1
                while (true) {
                    synchronized (lockA) {
                        log("P1 locked A");
                        sleep(100);
                        synchronized (lockB) {
                            log("P1 locked B");
                            bufferA.add(1);
                            bufferB.add(1);
                            log("P1 produced to A and B");
                        }
                    }
                }
            }, "P1");

            Thread p2 = new Thread(() -> {    // producer 2
                while (true) {
                    synchronized (lockB) {
                        log("P2 locked B");
                        sleep(100);
                        synchronized (lockA) {
                            log("P2 locked A");
                            bufferA.add(2);
                            bufferB.add(2);
                            log("P2 produced to A and B");
                        }
                    }
                }
            }, "P2");

            Thread c1 = new Thread(() -> {
                while (true) {
                    synchronized (lockA) {
                        log("C1 locked A");
                        sleep(100);
                        synchronized (lockB) {
                            log("C1 locked B");
                            if (!bufferA.isEmpty() && !bufferB.isEmpty()) {
                                bufferA.remove(0);
                                bufferB.remove(0);
                                log("C1 consumed from A and B");
                            }
                        }
                    }
                }
            }, "C1");

            Thread c2 = new Thread(() -> {
                while (true) {
                    synchronized (lockB) {
                        log("C2 locked B");
                        sleep(100);
                        synchronized (lockA) {
                            log("C2 locked A");
                            if (!bufferA.isEmpty() && !bufferB.isEmpty()) {
                                bufferA.removeFirst();
                                bufferB.removeFirst();
                                log("C2 consumed from A and B");
                            }
                        }
                    }
                }
            }, "C2");

            p1.start(); p2.start(); c1.start(); c2.start();   // pornim threadurile
        }

        private void log(String msg) { System.out.printf("|Deadlock|[%s] %s%n", Thread.currentThread().getName(), msg); }   // functie de logare
    }

    // fara deadlock
    static class FixedDeadlockDemo {
        // resurse shared
        private final List<Integer> bufferA = Collections.synchronizedList(new ArrayList<>());
        private final List<Integer> bufferB = Collections.synchronizedList(new ArrayList<>());

        private final ReentrantLock lockA = new ReentrantLock();
        private final ReentrantLock lockB = new ReentrantLock();

        public void start() {  // metoda principiala, definie threaduri etc
            Thread p1 = new Thread(() -> {
                while (true) {
                    if (lockA.tryLock()) {
                        try {
                            if (lockB.tryLock()) {
                                try {
                                    bufferA.add(1);
                                    bufferB.add(1);
                                    log("P1 produced");
                                } finally { lockB.unlock(); }
                            }
                        } finally { lockA.unlock(); }
                    }
                    sleep(50);
                }
            }, "P1");

            Thread p2 = new Thread(() -> {
                while (true) {
                    if (lockA.tryLock()) {
                        try {
                            if (lockB.tryLock()) {   // folosim tryLock
                                try {
                                    bufferA.add(2);
                                    bufferB.add(2);
                                    log("P2 produced");
                                } finally { lockB.unlock(); }
                            }
                        } finally { lockA.unlock(); }
                    }
                    sleep(50);
                }
            }, "P2");

            Thread c1 = new Thread(() -> {
                while (true) {
                    if (lockA.tryLock()) {
                        try {
                            if (lockB.tryLock()) {
                                try {
                                    if (!bufferA.isEmpty() && !bufferB.isEmpty()) {
                                        bufferA.remove(0);
                                        bufferB.remove(0);
                                        log("C1 consumed");
                                    }
                                } finally { lockB.unlock(); }
                            }
                        } finally { lockA.unlock(); }
                    }
                    sleep(50);
                }
            }, "C1");

            Thread c2 = new Thread(() -> {
                while (true) {
                    if (lockA.tryLock()) {
                        try {
                            if (lockB.tryLock()) {
                                try {
                                    if (!bufferA.isEmpty() && !bufferB.isEmpty()) {
                                        bufferA.removeFirst();
                                        bufferB.removeFirst();
                                        log("C2 consumed");
                                    }
                                } finally { lockB.unlock(); }
                            }
                        } finally { lockA.unlock(); }
                    }
                    sleep(50);
                }
            }, "C2");

            p1.start(); p2.start(); c1.start(); c2.start();   // pornim threaduri
        }

        private void log(String msg) {System.out.printf("|Functional|[%s] %s%n", Thread.currentThread().getName(), msg); } // functie de logare

    }

    public static void main(String[] args) {
        new DeadlockDemo().start();  // Deadlock intenționat
        sleep(3000); // asteptam 3 sec intre variante
        new FixedDeadlockDemo().start(); // fara deadlock
    }

    private static void sleep(int ms) {    // functie de sleep
        try { Thread.sleep(ms);} catch (InterruptedException ignored) {}   // eventualele erori
    }
}