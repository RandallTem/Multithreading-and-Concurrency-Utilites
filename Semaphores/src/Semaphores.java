import java.util.LinkedList;

public class Semaphores {

    private class BoundedCountingSemaphore {
        private int signals;
        private int bound;

        public BoundedCountingSemaphore(int bound) {
            this.signals = 0;
            this.bound = bound;
        }

        public synchronized void take() throws InterruptedException {
            while (this.signals == this.bound) {
                wait();
            }
            this.signals++;
            notify();
        }

        public synchronized void release() throws InterruptedException {
            while (this.signals == 0) {
                wait();
            }
            this.signals--;
            notify();
        }
    }

    BoundedCountingSemaphore semaphore = new BoundedCountingSemaphore(10);
    BoundedCountingSemaphore semaphoreLock = new BoundedCountingSemaphore(1);

    int counter;

    public void testSemaphores() {
        try {

            //Использование семафора для сигнализирования. Writer добавляет значения быстрей, чем
            //Reader их читает, поэтому сигналы копятся в семафоре
            LinkedList<Integer> queue = new LinkedList<>();
            Runnable writer = () -> {
                int temp = 0;
                for (int i = 0; i < 6; i++) {
                    try {
                        Thread.sleep(500);
                        if (i < 5)
                            temp = (int)(Math.random() * 100);
                        else
                            temp = -1;
                        queue.addLast(temp);
                        System.out.println(Thread.currentThread().getName() + " записал значение " + temp);
                        semaphore.take();
                    } catch (Exception e) {
                        System.out.println("Исключение " + e);
                    }
                }
            };
            Runnable reader = () -> {
                int temp = 0;
                try {
                    while (temp != -1) {
                        semaphore.release();
                        Thread.sleep(1000);
                        temp = queue.pollFirst();
                        System.out.println(Thread.currentThread().getName() + " прочитал значение " + temp);
                    }
                } catch (Exception e) {
                    System.out.println("Исключение " + e);
                }
            };
            Thread write = new Thread(writer, "Writer");
            Thread read = new Thread(reader, "Reader");
            write.start();
            read.start();
            write.join();
            read.join();
            System.out.println();

            //Использование семафора для защиты критических секций
            counter = 0;
            Runnable criticalCodeNoSemaphore = () -> {
                for (int i = 0; i < 1000; i++) {
                    counter++;
                }
            };
            Runnable criticalCodeWithSemaphore = () -> {
                for (int i = 0; i < 1000; i++) {
                    try {
                        try {
                            semaphoreLock.take();
                            counter++;
                        } finally {
                            semaphoreLock.release();
                        }
                    } catch (Exception e) {
                        System.out.println("Исключение" + e);
                    }
                }
            };
            System.out.println("Выполнение без защиты критической секции семафором");
            Thread[] threads = new Thread[30];
            for (int i = 0; i < 30; i++) {
                threads[i] = new Thread(criticalCodeNoSemaphore);
            }
            for (int i = 0; i < 30; i++) {
                threads[i].start();
            }
            for (int i = 0; i < 30; i++) {
                threads[i].join();
            }
            System.out.println("Ожидаемое значение = 30000\nПолученное значение = " + counter + "\n");
            counter = 0;

            System.out.println("Выполнение с защищенной критической секцией");
            for (int i = 0; i < 30; i++) {
                threads[i] = new Thread(criticalCodeWithSemaphore);
            }
            for (int i = 0; i < 30; i++) {
                threads[i].start();
            }
            for (int i = 0; i < 30; i++) {
                threads[i].join();
            }
            System.out.println("Ожидаемое значение = 30000\nПолученное значение = " + counter);

        } catch (Exception e) {
            System.out.println("Исключение " + e);
        }

    }
}
