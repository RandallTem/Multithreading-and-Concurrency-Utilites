public class JavaVolatileKeyword {
    private long sharedVariable;
    private volatile long sharedVariableVolatile;

    public JavaVolatileKeyword() {
        sharedVariable = 0;
        sharedVariableVolatile = 0;
    }

    private void increase() {
        sharedVariable = sharedVariable + 1;
    }

    private void increaseVolatile() {
        sharedVariableVolatile = sharedVariableVolatile + 1;
    }

    private long getSharedVariable() {
        return sharedVariable;
    }

    private long getSharedVariableVolatile() {
        return sharedVariableVolatile;
    }



    public void testJavaVolatileKeyword() {
        try {
            Runnable writer, reader;

            //Один поток записывает, другие читает, volatile не используется, видимость не гарантируется
            writer = () -> {
                increase();
            };
            reader = () -> {
                System.out.println(getSharedVariable());
            };
            new Thread(writer, "Thread 0").start();
            new Thread(reader, "Thread 1").start();

            //Один поток записывает, другой читает, volatile используется, видимость гарантируется
            writer = () -> {
                increaseVolatile();
            };
            reader = () -> {
                System.out.println(getSharedVariableVolatile());
            };
            new Thread(writer, "Thread 0").start();
            new Thread(reader, "Thread 1").start();

            //Оба потока записывают, volatile используется, а синхронизация нет, поэтому
            //видимость не гарантируется, возникает состояние гонки
            writer = () -> {
                for (int i = 0 ; i < 1000; i++) {
                    increaseVolatile();
                }
            };
            Thread writer1 = new Thread(writer, "Thread 0");
            Thread writer2 = new Thread(writer, "Thread 1");
            writer1.start();
            writer2.start();
            writer1.join();
            writer2.join();
            System.out.println("Ожидаемое значение = 2001");
            System.out.println("Полученное значение = " + getSharedVariableVolatile());

        } catch (Exception e) {
            System.out.println("Исключение\n"+e);
        }
    }
}
