import java.sql.SQLOutput;
import java.util.*;

public class ReadWriteLocks {
	
	private class ReadWriteLock {
		
		private int writeAccesses = 0;
		private int writeRequests = 0;
		
		private Map<Thread, Integer> readingThreads =
			new HashMap<Thread, Integer>();
		private Thread writingThread = null;
		
		private synchronized void readLock() throws InterruptedException {
			Thread currentThread = Thread.currentThread();
			while (!isAllowedToRead(currentThread)) {
				wait();
			}
			readingThreads.put(currentThread, 
				readingThreads.get(currentThread) == null ? 1 : readingThreads.get(currentThread) + 1);
		}
		
		private synchronized void readUnlock() {
			if (readingThreads.get(Thread.currentThread()) != null) {
				int numOfAccesses = readingThreads.get(Thread.currentThread());
				if (numOfAccesses == 1) {
					readingThreads.remove(Thread.currentThread());
					System.out.println(Thread.currentThread().getName() + " больше не обладает правом на чтение");
				} else {
					readingThreads.replace(Thread.currentThread(), numOfAccesses-1);
					System.out.println(Thread.currentThread().getName() + " теряет вложенное право на чтение");
				}
				notifyAll();
			} else {
				throw new IllegalMonitorStateException("Wrong thread calling");
			}
				
		}
		
		private synchronized void writeLock() throws InterruptedException {
			writeRequests++;
			Thread currentThread = Thread.currentThread();
			while (!isAllowedToWrite(currentThread)) {
				wait();
			}
			writeRequests--;
			writeAccesses++;
			writingThread = currentThread;
		}
		
		private synchronized void writeUnlock() {
			if (isWriter(Thread.currentThread())) {
				writeAccesses--;
				if (writeAccesses == 0) {
					writingThread = null;
					System.out.println(Thread.currentThread().getName() + " больше не обладает правом на запись");
				} else {
					System.out.println(Thread.currentThread().getName() + " теряет вложенное право на запись");
				}
				notifyAll();
			} else {
				throw new IllegalMonitorStateException("Wrong thread calling");
			}
		}
		
		private boolean isAllowedToRead(Thread thread) {
			/*
			Условия на получение доступа на чтение:
			- Нет потоков выполняющих запись и нет потоков, ожидающих право на запись (первичный вход)
			- Уже имеет право на чтение (Read Reentrance)
			- Имеет право на запись (Write to Read Reentrance)
			*/
			Thread currentThread = Thread.currentThread();
			if (!hasWriter() && !hasWriterRequests()) {
				System.out.println(currentThread.getName() + " получает право на чтение, потому что нет потоков, " +
						"выполняющих запись или ожидающих право на запись");
				return true;
			}
			if (isReader(currentThread)) {
				System.out.println(currentThread.getName() + " получает право на чтение, потому что уже " +
						"имеет право на чтение");
				return true;
			}
			if (isWriter(currentThread)) {
				System.out.println(currentThread.getName() + " получает право на чтение, потому что " +
						"имеет право на запись");
				return true;
			}
			return false;
		}
		
		private boolean isAllowedToWrite(Thread thread) {
			/*
			Условия для получения доступа на запись
			- Нет потоков, выполняющих чтение или запись (первичный вход)
			- Уже имеет право на запись (Write Reentrance)
			- Поток имеет право на чтение и является единственным читателем (Read to Write Reentrance)
			*/
			Thread currentThread = Thread.currentThread();
			if (!hasReaders() && !hasWriter()) {
				System.out.println(currentThread.getName() + " получает право на запись, потому что нет потоков, " +
						"выполняющих чтение или запись");
				return true;
			}
			if (isWriter(currentThread)) {
				System.out.println(currentThread.getName() + " получает право на запись, потому что он уже " +
						"имеет право на запись");
				return true;
			}
			if (isOnlyReader(currentThread)) {
				System.out.println(currentThread.getName() + " получает право на запись, потому что он " +
						"единственный читатель");
				return true;
			}
			return false;
		}
		
		private boolean hasWriter() {
			return writingThread != null;
		}
		
		private boolean hasReaders() {
			return readingThreads.size() > 0;
		}
		
		private boolean hasWriterRequests() {
			return writeRequests > 0;
		}
		
		private boolean isReader(Thread thread) {
			return readingThreads.get(thread) != null;
		}
		
		private boolean isOnlyReader(Thread thread) {
			return readingThreads.get(thread) != null && readingThreads.size() == 1;
		}
		
		private boolean isWriter(Thread thread) {
			return writingThread == thread;
		}
		
	}
	
	ReadWriteLock lock = new ReadWriteLock();
	
	int data = 0;
	
	private class Writer extends Thread{
		
		public Writer(String name){
			super(name);
		}
		
		//write
		private void createData() {
			try {
				lock.writeLock();
				System.out.println(Thread.currentThread().getName() + " генерирует данные");
				int temp = (int)(Math.random() * 100);
				System.out.println(Thread.currentThread().getName() + " сгенерировал значение: " + temp);
				data = temp;
				checkCreatedData();
			} catch (Exception e) {
				System.out.println("Исключение " + e);
			} finally {
				lock.writeUnlock();
			}
		}
		
		//write -> read
		private void checkCreatedData() {
			try {
				lock.readLock();
				System.out.println(Thread.currentThread().getName() + " проверяет значение: " + data);
				if (data < 100) {
					fixData(data);
				}
				
			} catch (Exception e) {
				System.out.println("Исключение " + e);
			} finally {
				lock.readUnlock();
			}
		}
		
		
		//write -> read -> write
		private void fixData(int val) {
			try {
				lock.writeLock();
				System.out.println(Thread.currentThread().getName() + " Исправляет значение: " + val);
				data = val + 50;
				System.out.println(Thread.currentThread().getName() + " Обновил значение. Новое значение:  " + (val+50));
			} catch (Exception e) {
				System.out.println("Исключение " + e);
			} finally {
				lock.writeUnlock();
			}
		}
		
		public void run() {
			createData();
		}
		
	}

	private class Reader extends Thread{

		public Reader(String name){
			super(name);
		}

		//read
		private void readData() {
			try {
				lock.readLock();
				int temp = data;
				System.out.println(Thread.currentThread().getName() + " прочитал значение: " + temp);
				updateData(temp);
			} catch (Exception e) {
				System.out.println("Исключение " + e);
			} finally {
				lock.readUnlock();
			}
		}

		//read -> write
		private void updateData(int temp) {
			try {
				lock.writeLock();
				temp += 200;
				data = temp;
				System.out.println(Thread.currentThread().getName() + " обновил значение. Новое значение: " + temp);
				checkData();
			} catch (Exception e) {
				System.out.println("Исключение " + e);
			} finally {
				lock.writeUnlock();
			}
		}

		//read -> write -> read
		private void checkData() {
			try {
				lock.readLock();
				int temp = data;
				if (temp >= 200) {
					System.out.println(Thread.currentThread().getName() + " проверил значение. Оно >= 200");
				}
			} catch (Exception e) {
				System.out.println("Исключение " + e);
			} finally {
				lock.readUnlock();
			}
		}

		public void run() {
			readData();
		}

	}


	
	public void testReadWriteLocks() {
		try {
			//Демонстрация работы повторной входимости
			Writer writer = new Writer("Writer");
			Reader reader = new Reader("Reader");
			writer.start();
			writer.join();
			System.out.println();
			reader.start();
			reader.join();
			System.out.println();

		} catch (Exception e) {
			System.out.println("Исключение " + e);
		}

	}
	
}