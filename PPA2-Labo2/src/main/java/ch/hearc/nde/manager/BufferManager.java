package ch.hearc.nde.manager;

import ch.hearc.nde.buffer.CircularBuffer;
import ch.hearc.nde.producerconsumers.Consumer;
import ch.hearc.nde.producerconsumers.Producer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class BufferManager implements Runnable, BufferManagerMBean {
    private final List<Consumer> consumers;
    private final List<Producer> producers;
    private final CircularBuffer buffer;
    private final boolean infinite;

    /**
     * This semaphore is used to lock the end of the program.
     * It is used to prevent the program from ending before all the threads have finished.
     */
    private final Semaphore lock = new Semaphore(1);

    public BufferManager(int buffersize, int nProducers, int nConsumers, boolean infinite) {
        this.buffer = new CircularBuffer(buffersize);
        this.producers = new ArrayList<>();
        this.consumers = new ArrayList<>();
        this.infinite = infinite;

        createProducers(nProducers);
        createConsumers(nConsumers);
    }

    private void createProducers(int n){
        for (int i = 0; i < n; i++) {
            this.producers.add(new Producer(this.buffer, i, infinite));
        }
    }

    private void createConsumers(int n){
        for (int i = 0; i < n; i++) {
            this.consumers.add(new Consumer(this.buffer, i, infinite));
        }
    }

    private void startConsumers(){
        this.consumers.forEach(Thread::start);
    }

    private void startProducers(){
        this.producers.forEach(Thread::start);
    }

    public void changeBufferSize(int n) {
        this.buffer.updateSizeAndReset(n);
    }

    public void changeNbProducers(int n) {
        this.producers.forEach(producer -> {
            producer.interrupt();
            try{
                producer.join();
            } catch (InterruptedException ignored){
            }
        });

        System.out.println("Producers stopped");

        this.producers.clear();
        this.buffer.clear();

        createProducers(n);
        startProducers();

        System.out.println("Producers started");
    }

    public void changeNbConsumers(int n) {
        this.consumers.forEach(consumer -> {
            consumer.interrupt();
            try{
                consumer.join();
            } catch (InterruptedException ignored){
            }
        });

        System.out.println("Consumers stopped");

        this.consumers.clear();
        this.buffer.clear();

        createConsumers(n);
        startConsumers();

        System.out.println("Consumers started");
    }

    public void stop(){
        this.producers.forEach(Thread::interrupt);
        this.consumers.forEach(Thread::interrupt);
        this.lock.release();
    }

    @Override
    public void run() {
        try {
            lock.acquire();
            startConsumers();
            startProducers();
            lock.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public int getNbProducers() {
        return this.producers.size();
    }

    public int getNbConsumers() {
        return this.consumers.size();
    }

    public int getBufferSize() {
        return this.buffer.getSize();
    }

    @Override
    public int getCurrentBufferSize() {
        return this.buffer.getCurrentSize();
    }

    @Override
    public void setNbProducers(int n) {
        changeNbProducers(n);
    }

    @Override
    public void setNbConsumers(int n) {
        changeNbConsumers(n);
    }

    @Override
    public void setBufferSize(int n) {
        changeBufferSize(n);
    }

    @Override
    public double getAverageLatency() {
        return buffer.getAverageLatency();
    }

    @Override
    public double getAverageDebit() {
        return buffer.getDebit();
    }
}
