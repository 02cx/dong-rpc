package com.dong;

import java.lang.invoke.VarHandle;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProduceConsumerTest {

    private int count = 0;
    private int maxNum = 3;

    ReentrantLock lock = new ReentrantLock();
    Condition producerCondition = lock.newCondition();
    Condition consumerCondition = lock.newCondition();

    public static void main(String[] args) {
        ProduceConsumerTest test = new ProduceConsumerTest();

        new Thread(test.new Producer()).start();
        //new Thread(test.new Producer()).start();
        new Thread(test.new Consumer()).start();
        //new Thread(test.new Consumer()).start();
    }


    class Producer implements Runnable{

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                lock.lock();


                    try {
                        while(count >= maxNum) {
                            producerCondition.await();
                            System.out.println("仓库满了");
                        }
                        count++;
                        System.out.println("生产者生产" + i);
                        consumerCondition.signalAll();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }finally {
                        lock.unlock();
                }
            }
        }
    }

    class Consumer implements Runnable{

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                lock.lock();

                try {
                    while(count <= 0){
                        consumerCondition.await();
                        System.out.println("仓库空了");
                    }
                    count--;
                    System.out.println("Consumer " + i);
                    producerCondition.signalAll();
                }catch (InterruptedException e){
                    throw new RuntimeException(e);
                }finally {
                    lock.unlock();
                }
            }
        }
    }
}
