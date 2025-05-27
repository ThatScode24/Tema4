import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TaskManager {

    enum Status {    // Enum cu starile posibile
        RUNNING,
        COMPLETED,
        TIMED_OUT
    }

    static class Task implements Runnable {
        private final int id;
        private final long durationMs;
        private final Object monitor;
        private final ConcurrentHashMap<Integer, Status> statusMap;
        private volatile boolean completed = false;   // folisim volatile

        public Task(int id, long durationMs, Object monitor, ConcurrentHashMap<Integer, Status> statusMap) {
            this.id = id;
            this.durationMs = durationMs;
            this.monitor = monitor;
            this.statusMap = statusMap;
        }

        @Override
        public void run() {
            statusMap.put(id, Status.RUNNING);
            System.out.printf("Task %d started%n", id);

            synchronized (monitor) {
                try {
                    Thread.sleep(durationMs);
                    if (!Thread.currentThread().isInterrupted()) {
                        synchronized (statusMap) {
                            statusMap.put(id, Status.COMPLETED);   // schimbam status la completed
                            completed = true;
                            System.out.printf("Task %d completed%n", id);
                        }
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }  // exceptie de intrerupere de la timeout
            }
        }

        public boolean isCompleted() { return completed; }
    }

    public static void main(String[] args) {
        final int NUM_TASKS = 5;
        final long[] durations = {1000, 2000, 3000, 4000, 5000}; // durata taskuri in milisecunde
        final long Tmax = 2500; // timp maxim autorizat pentru un task
        final Object monitor = new Object();

        ConcurrentHashMap<Integer, Status> statusMap = new ConcurrentHashMap<>();
        List<Thread> threads = new ArrayList<>();  // aici stocam threadurile

        // dam launch la taskuri manual
        for (int i = 0; i < NUM_TASKS; i++) {
            Task task = new Task(i, durations[i], monitor, statusMap);
            Thread t = new Thread(task);
            threads.add(t);
            t.start();

            // controller de timeout pentru taskuri
            final int taskId = i;
            new Thread(() -> {
                try {
                    t.join(Tmax); // asteptam Tmax milisecunde
                    if (t.isAlive()) {
                        t.interrupt();
                        statusMap.put(taskId, Status.TIMED_OUT);
                        System.out.printf("Task %d timed out%n", taskId);
                    }
                } catch (InterruptedException ignored) {}
            }).start();
        }

        // printam statusurile taskurilor la fiecare jumatate de secunda, thread watchdog
        new Thread(() -> {
            while (true) {
                System.out.println("\n===Task Status===");
                System.out.printf("%-10s%-15s%n", "Task ID", "Status");
                for (int i = 0; i < NUM_TASKS; i++) { System.out.printf("%-10d%-15s%n", i, statusMap.getOrDefault(i, Status.RUNNING)); }

                try { Thread.sleep(500);} catch (InterruptedException ignored) {}
            }
        }).start();
    }
}
