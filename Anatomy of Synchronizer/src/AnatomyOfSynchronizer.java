public class AnatomyOfSynchronizer {

    private class BoundedCountingSemaphore {

        //Внутреннее состояние
        private int signals;
        private int bound;

        public BoundedCountingSemaphore(int bound) {
            this.signals = 0;
            this.bound = bound;
        }

        public synchronized void take() throws InterruptedException {
            while (this.signals == this.bound) {
                System.out.println("Поток " + Thread.currentThread().getName() +
                        " выполнил проверку состояния для метода take(). Условие не выполнено. Поток ждет");
                wait();
            }
            System.out.println("Поток " + Thread.currentThread().getName() +
                    " выполнил проверку состояния для метода take(). Условие выполнено. Поток\n" +
                    "изменяет внутреннее состояние, " +
                    "увеличивая переменную signals на 1 (Test-and-Set) и уведомляет один случайный поток");
            this.signals++;
            notify();
        }

        public synchronized void release() throws InterruptedException {
            while (this.signals == 0) {
                System.out.println("Поток " + Thread.currentThread().getName() +
                        " выполнил проверку состояния для метода release(). Условие не выполнено. Поток ждет");
                wait();
            }
            System.out.println("Поток " + Thread.currentThread().getName() +
                    " выполнил проверку состояния для метода release(). Условие выполнено. Поток\n" +
                    "изменяет внутреннее состояние, " +
                    "уменьшая переменную signals на 1 (Test-and-Set) и уведомляет один случайный поток");
            this.signals--;
            notify();
        }
    }

    public class Lock{

        //Внутреннее состояние
        private boolean isLocked;

        public Lock() {
            isLocked = false;
        }

        public synchronized void lock()
                throws InterruptedException{
            while(isLocked){
                System.out.println("Поток " + Thread.currentThread().getName() +
                        " выполнил проверку состояния для метода lock(). Условие не выполнено. Поток ждет");
                wait();
            }
            System.out.println("Поток " + Thread.currentThread().getName() +
                    " выполнил проверку состояния для метода lock(). Условие выполнено. Поток изменяет внутреннее состояние " +
                    " isLocked на true (Test-and-Set)");
            isLocked = true;
        }

        public synchronized void unlock(){
            System.out.println("Поток " + Thread.currentThread().getName() +
                    " изменяет состояние isLocked на false без проверок (Set) и уведомляет один случайный поток");
            isLocked = false;
            notify();
        }
    }

    public void testAnatomyOfSynchronizer() {
        try {

            Thread thread1, thread2;
            Runnable code;

            //Демонстрация анатомии замка
            System.out.println("Замок");
            Lock lock = new Lock();
            code = () -> {
                try {
                    Thread.sleep((int)(Math.random() * 1000));
                    lock.lock();
                    Thread.sleep(700);
                    System.out.println(Thread.currentThread().getName() + " выполнил код");
                    lock.unlock();
                } catch (Exception e) {
                    System.out.println("Исключение " + e);
                }
            };
            thread1 = new Thread(code, "Lock 1");
            thread2 = new Thread(code, "Lock 2");
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();

            //Демонстрация анатомии семафора
            System.out.println("\nОграниченный семафор");
            BoundedCountingSemaphore semaphore = new BoundedCountingSemaphore(1);
            code = () -> {
                try {
                    Thread.sleep((int) (Math.random() * 1000));
                    semaphore.take();
                    Thread.sleep(700);
                    System.out.println(Thread.currentThread().getName() + " выполнил код");
                    semaphore.release();
                } catch (Exception e) {
                    System.out.println("Исключение " + e);
                }
            };
            thread1 = new Thread(code, "Semaphore 1");
            thread2 = new Thread(code, "Semaphore 2");
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();

        } catch (Exception e) {
            System.out.println("Исключение " + e);
        }


    }



}
