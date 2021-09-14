import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPools {

    private class PoolThread implements Runnable{
        private Thread thread;
        private BlockingQueue queue;
        private AtomicInteger freeThreads;
        private boolean isStopped;

        public PoolThread(BlockingQueue queue, AtomicInteger freeThreads) {
            this.queue = queue;
            this.freeThreads = freeThreads;
            this.thread = null;
            this.isStopped = false;
        }

        private synchronized boolean isStopped() {
            return this.isStopped;
        }

        private synchronized void stopThread() {
            this.isStopped = true;
            this.thread.interrupt();
        }

        public void run() {
            thread = Thread.currentThread();
            while (!isStopped) {
                try {
                    Runnable task = (Runnable) queue.take();
                    freeThreads.decrementAndGet();
                    task.run();
                    freeThreads.incrementAndGet();
                } catch (InterruptedException e) {
                    System.out.println(Thread.currentThread().getName() + " прерван");
                }
            }
        }
    }

    private class ThreadPool {

        private ArrayList<PoolThread> pool;
        private BlockingQueue queue;
        private boolean isStopped;
        private AtomicInteger freeThreads;
        private String name;

        public ThreadPool(int numberOfThreads, int queueSize, String name) {
            this.pool = new ArrayList<>();
            this.name = name;
            this.freeThreads = new AtomicInteger(numberOfThreads);
            this.isStopped = false;
            this.queue = new ArrayBlockingQueue(queueSize);
            for (int i = 0; i < numberOfThreads; i++) {
                pool.add(new PoolThread(queue, freeThreads));
                new Thread(pool.get(i), this.name + ": Thread " + i).start();
            }
            System.out.println("Создан пул " + this.name + " с параметрами:\n" +
                    "Потоков: " + numberOfThreads + "; Размер очереди: " + queueSize + "\n");
        }

        public synchronized void setNewTask(Runnable task) throws Exception {
            if (!isStopped) {
                queue.put(task);
                System.out.println("В пул " + this.name + " добавлено новое задание");
            } else {
                throw new IllegalStateException("ThreadPool is stopped");
            }
        }

        public synchronized void stopPool() {
            this.isStopped = true;
            System.out.println("Пул " + this.name + " завершает выполнение");
            while (pool.size() > 0) {
                pool.remove(0).stopThread();
            }
        }

        public synchronized void waitUntilTasksAreFinished() {
            while (this.queue.size() > 0 || this.freeThreads.get() < this.pool.size()) {
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    System.out.println("Исключение " + e);
                }
            }
        }
    }

    public void testThreadPools() {
        try {

            ThreadPool pool = new ThreadPool(2, 3, "Pool");
            
            for (int i = 0; i < 8; i++) {
                if (i == 7)
                    Thread.sleep(5000);
                pool.setNewTask(
                    () -> {
                        System.out.println(Thread.currentThread().getName() + " взял задание");
                        int temp = (int)(Math.random() * 100);
                        int multiplier = (int)(Math.random() * 100);
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                            System.out.println("Исключение " + e);
                        }
                    }
                );
            }

            pool.waitUntilTasksAreFinished();
            pool.stopPool();

        } catch (Exception e) {
            System.out.println("Исключение " + e);
        }
    }

}
