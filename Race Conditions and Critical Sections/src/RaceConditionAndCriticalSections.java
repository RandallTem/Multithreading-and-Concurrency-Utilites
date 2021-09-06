public class RaceConditionAndCriticalSections {

    private int value = 0;

    private void addValue(int value) {
        this.value = this.value + value;
    }

    private void addValueIfLessThanFifty(int value) {
        try {
            if (this.value < 50) {
                Thread.sleep(10);
                this.value = this.value + value;
            }
        } catch (Exception e ) {
            System.out.println("Исключение\n"+e);
        }
    }

    private synchronized void addValueIfLessThanFiftySync(int value) {
        try {
            if (this.value < 50) {
                Thread.sleep(10);
                this.value = this.value + value;
            }
        } catch (Exception e ) {
            System.out.println("Исключение\n"+e);
        }
    }

    private class RmwCriticalSection implements Runnable {

        int increase;

        RmwCriticalSection(int increase) {
            this.increase = increase;
        }

        public void run() {
            for (int i = 0; i < 1000; i++) {
                addValue(increase);
            }
        }

    }


    private class RmwCriticalSectionSync implements Runnable {

        int increase;

        RmwCriticalSectionSync(int increase) {
            this.increase = increase;
        }

        public void run() {
            synchronized (this) {
                for (int i = 0; i < 1000; i++) {
                    addValue(increase);
                }
            }
        }

    }

    public void testRaceConditionAndCriticalSections() {
        try {
            Thread thread1, thread2;
            Thread[] threads = new Thread[30];

            //Read-modify-write критическая секция не синхронизирована
            System.out.println("Read-modify-write критическая секция не синхронизирована");
            RmwCriticalSection incrementUnsync = new RmwCriticalSection(2);
            thread1 = new Thread(incrementUnsync);
            thread2 = new Thread(incrementUnsync);
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();
            System.out.println("Ожидаемое значение value = 4000");
            System.out.println("Полученное значение value после работы потоков = " + this.value);
            this.value = 0;

            //Read-modify-write критическая секция синхронизирована с помощью блока synchronized
            System.out.println("\nRead-modify-write критическая секция синхронизирована");
            RmwCriticalSectionSync incrementSync = new RmwCriticalSectionSync(2);
            thread1 = new Thread(incrementSync);
            thread2 = new Thread(incrementSync);
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();
            System.out.println("Ожидаемое значение value = 4000");
            System.out.println("Полученное значение value после работы потоков = " + this.value);
            this.value = 0;

            //Check-then-act критическая секция не синхронизирована
            System.out.println("\nCheck-then-act критическая секция не синхронизирована");
            Runnable incrementIf = () -> {
                for (int i = 0; i < 50; i++) {
                    addValueIfLessThanFifty(1);
                }
            };
            for (int i = 0; i < 30; i++) {
                threads[i] = new Thread(incrementIf);
                threads[i].start();
            }
            for (int i = 0; i < 30; i++) {
                threads[i].join();
            }
            System.out.println("Ожидаемое значение value = 50");
            System.out.println("Полученное значение value после работы потоков = " + this.value);
            this.value = 0;

            //Check-then-act критическая секция синхронизирована с помощью синхронизации метода addValueIfLessThanFifty()
            System.out.println("\nCheck-then-act критическая секция синхронизирована");
            Runnable incrementIfSync = () -> {
                for (int i = 0; i < 50; i++) {
                    addValueIfLessThanFiftySync(1);
                }
            };
            for (int i = 0; i < 30; i++) {
                threads[i] = new Thread(incrementIfSync);
                threads[i].start();
            }
            for (int i = 0; i < 30; i++) {
                threads[i].join();
            }
            System.out.println("Ожидаемое значение value = 50");
            System.out.println("Полученное значение value после работы потоков = " + this.value);

        } catch (Exception e) {
            System.out.println("Исключение\n"+e);
        }
    }


}
