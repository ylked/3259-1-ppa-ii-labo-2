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
    private final StatisticsManager statisticsManager;
    private final boolean infinite;

    /**
     * This semaphore is used to lock the end of the program.
     * It is used to prevent the program from ending before all the threads have finished.
     */
    private final Semaphore lock = new Semaphore(1);

    public BufferManager(int buffersize, int nProducers, int nConsumers, boolean infinite) {
        this.buffer = new CircularBuffer(buffersize);
        this.statisticsManager = new StatisticsManager();

        this.producers = new ArrayList<>();
        this.consumers = new ArrayList<>();
        this.infinite = infinite;

        createProducers(nProducers);
        createConsumers(nConsumers);
    }

    private void createProducers(int n){
        for (int i = 0; i < n; i++) {
            this.producers.add(new Producer(this.buffer, this.statisticsManager, i, infinite));
        }
    }

    private void createConsumers(int n){
        for (int i = 0; i < n; i++) {
            this.consumers.add(new Consumer(this.buffer, this.statisticsManager, i, infinite));
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
        // we restart the producers and consumers, but keep the same number of each
        changeNbProducersConsumers(this.consumers.size(), this.producers.size());
    }

    private void stopConsumers(){
        this.consumers.forEach(consumer -> {
            consumer.interrupt();
            try{
                consumer.join();
            } catch (InterruptedException ignored){
            }
        });
    }

    private void stopProducers(){
        this.producers.forEach(producer -> {
            producer.interrupt();
            try{
                producer.join();
            } catch (InterruptedException ignored){
            }
        });
    }

    private void changeNbProducersConsumers(int consumers, int producers){
        stopConsumers();
        stopProducers();

        System.out.println("Producers stopped");
        System.out.println("Consumers stopped");

        this.producers.clear();
        this.consumers.clear();
        this.buffer.clear();
        this.statisticsManager.reset();

        createProducers(producers);
        createConsumers(consumers);
        startProducers();
        startConsumers();

        System.out.println("Producers started");
        System.out.println("Consumers started");
    }


    public void changeNbConsumers(int n) {
        changeNbProducersConsumers(n, this.producers.size());
    }

    public void changeNbProducers(int n) {
        changeNbProducersConsumers(this.consumers.size(), n);
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
        return statisticsManager.getAverageLatency();
    }

    @Override
    public double getAverageDebit() {
        return statisticsManager.getDebit();
    }

    @Override
    public int getSampleSize() {
        return statisticsManager.getSampleSize();
    }

    @Override
    public void setSampleSize(int n) {
        statisticsManager.setSampleSize(n);
    }
}
