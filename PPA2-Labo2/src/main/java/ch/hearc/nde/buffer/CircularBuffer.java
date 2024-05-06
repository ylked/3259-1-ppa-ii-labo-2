package ch.hearc.nde.buffer;

import ch.hearc.nde.manager.StatisticsManager;

public class CircularBuffer {
    private StatisticsManager statisticsManager;
    private String[] buffer;
    private int size;
    private int front;
    private int rear;

    public CircularBuffer(int n) {
        init(n);
    }

    public synchronized void produce(String message) throws InterruptedException {
        while(isFull()) wait();

        this.buffer[this.rear] = message;
        updateRear();
        this.size++;

        statisticsManager.produce(message.hashCode());

        notifyAll();
    }

    public synchronized String consume() throws InterruptedException {
        while(isEmpty()) wait();

        String msg = this.buffer[this.front];
        updateFront();
        this.size--;

        statisticsManager.consume(msg.hashCode());

        notifyAll();
        return msg;
    }

    public synchronized void updateSizeAndReset(int n){
        assert n > 0;
        init(n);
    }

    private synchronized void init(int n){
        this.statisticsManager = new StatisticsManager();
        this.buffer = new String[n];
        this.size = 0;
        this.front = 0;
        this.rear = 0;
    }

    public synchronized void clear(){
        init(this.buffer.length);
        System.out.println("Cleared buffer");
    }

    private synchronized void updateFront() {
        this.front = (this.front + 1) % this.buffer.length;
    }

    private synchronized void updateRear() {
        this.rear = (this.rear + 1) % this.buffer.length;
    }

    private synchronized boolean isFull() {
        return this.size >= this.buffer.length;
    }

    private synchronized boolean isEmpty() {
        return this.size <= 0;
    }

    public int getCurrentSize() {
        return this.size;
    }

    public int getSize() {
        return this.buffer.length;
    }

    public double getAverageLatency() {
        return statisticsManager.getAverageLatency();
    }

    public double getDebit() {
        return statisticsManager.getDebit();
    }

}
