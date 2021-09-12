public class JavaThreadSignaling {
	
	private int data, freeThreads, busyThreads;
	private boolean isNewData;
	
	private class Monitor {
	}
	
	public JavaThreadSignaling() {
		this.data = 0;
		this.freeThreads = 0;
		this.busyThreads = 0;
		this.isNewData = false;
	}
	
	Monitor dataPreparationMonitor = new Monitor();
	Monitor freeThreadsMonitor = new Monitor();
	
	
	private class Provider implements Runnable {
		public void run() {
			try {
				for (int i = 0; i < 10; i++) {
					System.out.println("Thread Поставщик начинает подготовку данных");
					Thread.sleep(500);
					synchronized (freeThreadsMonitor) {
						while (freeThreads == 0) {
							freeThreadsMonitor.wait();
						}
					}
					data = (int)(Math.random() * 1000);
					isNewData = true;
					System.out.println("Thread Поставщик подготовил данные: " + data);
					synchronized (dataPreparationMonitor) {
						dataPreparationMonitor.notify();
					}
				}
				synchronized (freeThreadsMonitor) {
					while (busyThreads > 0) {
						freeThreadsMonitor.wait();
					}
				}
				System.out.println("У Поставщика больше нет новых данных");
				data = -1;
				isNewData = true;
				synchronized (dataPreparationMonitor) {
					dataPreparationMonitor.notifyAll();
				}
			} catch (Exception e) {
				System.out.println("Исключение " + e);
			}
		}
	}
	
	private class Processor implements Runnable {
		public void run() {
			try {
				synchronized (this) {
					freeThreads++;
				}
				synchronized (freeThreadsMonitor) {
					freeThreadsMonitor.notify();
				}
				while (data != -1) {
					synchronized (dataPreparationMonitor) {
						while (!isNewData) {
							dataPreparationMonitor.wait();
						}
					}
					int temp = data;
					if (temp == -1) {
						System.out.println(Thread.currentThread().getName() + " прекращает работу");
						break;
					}
					synchronized (this) {
						isNewData = false;
						freeThreads--;
						busyThreads++;
					}
					System.out.println(Thread.currentThread().getName() + " начинает обработку: " + temp);
					Thread.sleep(1000);
					System.out.println(Thread.currentThread().getName() + " заканчивает обработку: " + temp);
					synchronized (this) {
						freeThreads++;
						busyThreads--;
					}
					synchronized (freeThreadsMonitor) {
						freeThreadsMonitor.notify();
					}
				}
			} catch (Exception e) {
				System.out.println("Исключение " + e);
			}
		}
	}

	public void testJavaThreadSignaling() {
		//Есть один поток, поставляющий данные, и несколько потоков, их обрабатывающие. Поток-поставщик предоставляет
		//новые данные каждые 0.5 секунд, а поток-обработчик обрабатывает их 1 секунду. Если нет свободных потоков,
		//то поставщик переходит в режим ожидания, пока хотя бы один поток не освободится. Все потоки постоянно находятся
		//в режиме ожидания, пока поставщик не сообщает, что новые данные добавлены. Затем за их обработку берется один
		//из потоков. Когда все данные предоставлены, поставщик переходит в режим ожидания, пока все потоки не освободятся,
		//затем всем потокам через notifyAll() сообщает, что пора завершаться с помощью специального значения в общей
		//переменной для данных.
		new Thread(new Provider()).start();
		for (int i = 0; i < 5; i++) {
			new Thread(new Processor(), "Thread Обработчик "+i).start();
		}

	}
}