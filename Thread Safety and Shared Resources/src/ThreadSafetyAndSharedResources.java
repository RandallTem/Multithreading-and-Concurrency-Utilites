import org.omg.CORBA.LocalObject;

public class ThreadSafetyAndSharedResources {

    private class IntegerProcesser {

        Integer value;

        public Integer processInteger(Integer value) {
            this.value = value;
            this.value++;
            return this.value;
        }

    }

    private class FieldAdder {

        private long field;

        public FieldAdder(long field) {
            this.field = field;
        }

        public void add(long value) {
            field += value;
        }

        public long getField() {
            return field;
        }

    }

    private void localVariable() {
        long val = 0;
        for (; val < 100; ) {
            val++;
        }
        System.out.println(Thread.currentThread().getName()+": "+val);
    }


    public void testThreadSafetyAndSharedResources() {
        try {
            Thread thread1, thread2;
            Runnable threadCode;

            //Локальная переменная. Всегда потокобезопасна.
            System.out.println("Всегда потокобезопасная локальная переменная");
            threadCode = () -> localVariable();
            thread1 = new Thread(threadCode, "Thread 1");
            thread2 = new Thread(threadCode, "Thread 2");
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();

            //Локальный объект. Не потокобезопасен, потому что передается объекту, доступному для других потоков.
            System.out.println("\nНепотокобезопасный локальный объект\nОжидаемые значения = 1005");
            IntegerProcesser intProcShared = new IntegerProcesser();
            threadCode = () -> {
                Integer val = 5;
                for (int i = 0; i < 1000; i++) {
                    val = intProcShared.processInteger(val);
                }
                System.out.println(Thread.currentThread().getName() + ": " + val);
            };
            thread1 = new Thread(threadCode, "Thread 1");
            thread2 = new Thread(threadCode, "Thread 2");
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();

            //Локальный объект. Потокобезопасный, передается объекту, который не доступен для других потоков.
            System.out.println("\nПотокобезопасный локальный объект\nОжидаемые значения = 1005");
            threadCode = () -> {
                Integer val = 5;
                IntegerProcesser intProcLocal = new IntegerProcesser();
                for (int i = 0; i < 1000; i++) {
                    val = intProcLocal.processInteger(val);
                }
                System.out.println(Thread.currentThread().getName() + ": " + val);
            };
            thread1 = new Thread(threadCode, "Thread 1");
            thread2 = new Thread(threadCode, "Thread 2");
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();

            //Поле объекта. Непотокобезопасное, потоки работают с одним и тем же объектом.
            System.out.println("\nНепотокобезопасное поле объекта\nОжидаемое значение = 2000");
            FieldAdder fieldAddShared = new FieldAdder(0);
            threadCode = () -> {
                for (int i = 0; i < 1000; i++) {
                    fieldAddShared.add(1);
                }
            };
            thread1 = new Thread(threadCode, "Thread 1");
            thread2 = new Thread(threadCode, "Thread 2");
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();
            System.out.println("Результат = " + fieldAddShared.getField());

            //Поле объекта. Потокобезопасное. Каждый поток работает со своим объектом.
            System.out.println("\nПотокобезопасное поле объекта\nОжидаемые значения = 1000");
            threadCode = () -> {
                FieldAdder fieldAdd = new FieldAdder(0);
                for (int i = 0; i < 1000; i++) {
                    fieldAdd.add(1);
                }
                System.out.println(Thread.currentThread().getName() + ": " + fieldAdd.getField());
            };
            thread1 = new Thread(threadCode, "Thread 1");
            thread2 = new Thread(threadCode, "Thread 2");
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();

        } catch (Exception e) {
            System.out.println("Исключение\n" + e);
        }
    }

}
