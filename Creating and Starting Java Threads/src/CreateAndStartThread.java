public class CreateAndStartThread {

    private class ThreadSubclass extends Thread {

        public ThreadSubclass(String name) {
            super(name);
        }

        public void run() {
            try {
                String threadName = Thread.currentThread().getName();
                System.out.println(threadName + ": Поток запущен");
                for (int i = 0; i < 5; i++) {
                    System.out.println(threadName + ": " + i);
                    Thread.sleep(500);
                }
                System.out.println(threadName + ": Поток завершен\n");
            } catch (Exception e) {
                System.out.println("Исключение " + e + " в потоке " + Thread.currentThread().getName());
            }
        }
    }

    private class RunnableImplementation implements Runnable {
        public void run() {
            try {
                String threadName = Thread.currentThread().getName();
                System.out.println(threadName + ": Поток запущен");
                for (int i = 0; i < 5; i++) {
                    System.out.println(threadName + ": " + i);
                    Thread.sleep(500);
                }
                System.out.println(threadName + ": Поток завершен\n");
            } catch (Exception e) {
                System.out.println("Исключение " + e + " в потоке " + Thread.currentThread().getName());
            }
        }
    }

    public void createAndStartThread() {
        try {
            //Подкласс Thread
            ThreadSubclass threadSubclass = new ThreadSubclass("Подкласс Thread");
            threadSubclass.start();
            threadSubclass.join();

            //Анонимный подкласс Thread
            Thread anonymousThreadSubclass = new Thread("Анонимный подкласс Thread") {
                public void run() {
                    try {
                        String threadName = Thread.currentThread().getName();
                        System.out.println(threadName + ": Поток запущен");
                        for (int i = 0; i < 5; i++) {
                            System.out.println(threadName + ": " + i);
                            Thread.sleep(500);
                        }
                        System.out.println(threadName + ": Поток завершен\n");
                    } catch (Exception e) {
                        System.out.println("Исключение " + e + " в потоке " + Thread.currentThread().getName());
                    }
                }
            };
            anonymousThreadSubclass.start();
            anonymousThreadSubclass.join();

            //Реализация Runnable в классе
            Thread runnableImplementationThread = new Thread(new RunnableImplementation(),
                    "Класс, реализующий интерфейс Runnable");
            runnableImplementationThread.start();
            runnableImplementationThread.join();

            //Реализация Runnable в анонимном классе
            Runnable anonRunnableImplementation = new Runnable() {
                public void run() {
                    try {
                        String threadName = Thread.currentThread().getName();
                        System.out.println(threadName + ": Поток запущен");
                        for (int i = 0; i < 5; i++) {
                            System.out.println(threadName + ": " + i);
                            Thread.sleep(500);
                        }
                        System.out.println(threadName + ": Поток завершен\n");
                    } catch (Exception e) {
                        System.out.println("Исключение " + e + " в потоке " + Thread.currentThread().getName());
                    }
                }
            };
            Thread anonRunnableImplementationThread = new Thread(anonRunnableImplementation,
                    "Анонимный класс, реализующий интерфейс Runnable");
            anonRunnableImplementationThread.start();
            anonRunnableImplementationThread.join();

            //Реализация Runnable через лямбда-выражение
            Runnable lambdaRunnableImplementation = () -> {
                try {
                    String threadName = Thread.currentThread().getName();
                    System.out.println(threadName + ": Поток запущен");
                    for (int i = 0; i < 5; i++) {
                        System.out.println(threadName + ": " + i);
                        Thread.sleep(500);
                    }
                    System.out.println(threadName + ": Поток завершен\n");
                } catch (Exception e) {
                    System.out.println("Исключение " + e + " в потоке " + Thread.currentThread().getName());
                }
            };
            Thread lambdaRunnableImplementationThread = new Thread(lambdaRunnableImplementation,
                    "Реализация интерфейса Runnable через лямбда-выражение");
            lambdaRunnableImplementationThread.start();
            lambdaRunnableImplementationThread.join();

        } catch (Exception e) {
            System.out.println("Исключение\n"+e);
        }
    }

}
