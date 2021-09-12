public class JavaThreadLocal {
	
	public void testJavaThreadLocal() {
		try {
			
			Thread thread1, thread2, thread3;
			Runnable trLocal;
			
			//Создание, задание и удаление ThreadLocal
			trLocal = () -> {
				ThreadLocal threadLocal = new ThreadLocal();
				System.out.println(Thread.currentThread().getName() + " изначально: " + threadLocal.get());
				threadLocal.set((int)(Math.random() * 100));
				System.out.println(Thread.currentThread().getName() + " после задания значения: " + threadLocal.get());
				threadLocal.remove();
				System.out.println(Thread.currentThread().getName() + " после удаления значения: " + threadLocal.get());
			};
			thread1 = new Thread(trLocal, "Thread 0");
			thread2 = new Thread(trLocal, "Thread 1");
			thread3 = new Thread(trLocal, "Thread 2");
			thread1.start();
			thread2.start();
			thread3.start();
			thread1.join();
			thread2.join();
			thread3.join();
			System.out.println();
			
			//Generic ThreadLocal с начальным значением
			trLocal = () -> {
				ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>() {
					protected Integer initialValue() {
						return 55;
					}						
				};
				System.out.println(Thread.currentThread().getName() + " изначально: " + threadLocal.get());
				threadLocal.set((int)(Math.random() * 100));
				System.out.println(Thread.currentThread().getName() + " после задания значения: " + threadLocal.get());
				threadLocal.remove();
				System.out.println(Thread.currentThread().getName() + " после удаления значения: " + threadLocal.get());
			};
			thread1 = new Thread(trLocal, "Thread 0");
			thread2 = new Thread(trLocal, "Thread 1");
			thread3 = new Thread(trLocal, "Thread 2");
			thread1.start();
			thread2.start();
			thread3.start();
			thread1.join();
			thread2.join();
			thread3.join();
			System.out.println();
			
			//InheritableThreadLocal
			trLocal = () -> {
				ThreadLocal<Integer> threadLocal = ThreadLocal.withInitial(() -> { 
					return 100;
				});			
				InheritableThreadLocal<Integer> inheritableThreadLocal = new InheritableThreadLocal<Integer>() {
					protected Integer initialValue() {
						return 100;
					}
				};
				System.out.println("ThreadLocal изначально: " + threadLocal.get());
				System.out.println("Inheritable ThreadLocal изначально: " + threadLocal.get());
				threadLocal.set((int)(Math.random() * 100));
				inheritableThreadLocal.set((int)(Math.random() * 100));
				System.out.println("ThreadLocal после задания значения: " + threadLocal.get());
				System.out.println("Inheritable ThreadLocal после задания значения: " + inheritableThreadLocal.get());
				Thread childThread = new Thread( () -> {
					System.out.println("ThreadLocal в порожденном потоке: " + threadLocal.get());
					System.out.println("InheritableThreadLocal в порожденном потоке: " + inheritableThreadLocal.get());
				} );
				childThread.start();
			};
			new Thread(trLocal, "Thread").start();
			
		} catch (Exception e) {
			System.out.println("Исключение " + e);
		}
	}
	
}