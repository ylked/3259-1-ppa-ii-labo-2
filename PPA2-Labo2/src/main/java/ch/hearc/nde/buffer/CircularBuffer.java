package ch.hearc.nde.buffer;

/**
 * Circular buffer implementation.
 * <br>
 * See <a href="https://en.wikipedia.org/wiki/Circular_buffer">Wikipedia</a>.
 * <br>
 * This implementation permits dynamic resizing of the buffer.
 * <br>
 * <strong>Warning : </strong> resizing the buffer will remove all the messages in the buffer.
 */
public class CircularBuffer {
    /**
     * The array representing the buffer.
     */
    private String[] buffer;

    /**
     * The number of elements currently in the buffer.
     */
    private int size;

    /**
     * The index of the first element in the buffer.
     */
    private int front;

    /**
     * The index of the last element in the buffer.
     */
    private int rear;

    /**
     * Creates a new circular buffer with a given size.
     * @param n The size maximum size of the buffer.
     */
    public CircularBuffer(int n) {
        init(n);
    }

    /**
     * Produces a message in the buffer.
     * If the buffer is full, the thread will wait until a message is consumed.
     * @param message The message to publish.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    public synchronized void produce(String message) throws InterruptedException {
        while(isFull()) wait();

        this.buffer[this.rear] = message;
        updateRear();
        this.size++;

        notifyAll();
    }

    /**
     * Consumes a message from the buffer.
     * If the buffer is empty, the thread will wait until a message is produced.
     * @return the message consumed.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    public synchronized String consume() throws InterruptedException {
        while(isEmpty()) wait();

        String msg = this.buffer[this.front];
        updateFront();
        this.size--;

        notifyAll();
        return msg;
    }

    /**
     * Updates the size of the buffer and resets it.
     * @param n The new size of the buffer.
     */
    public synchronized void updateSizeAndReset(int n){
        assert n > 0;
        init(n);
    }

    private synchronized void init(int n){
        this.buffer = new String[n];
        this.size = 0;
        this.front = 0;
        this.rear = 0;
    }

    /**
     * Clears the buffer.
     */
    public synchronized void clear(){
        init(this.buffer.length);
        System.out.println("Buffer cleared");
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

    /**
     * Returns the current number of elements in the buffer.
     * @return The current number of elements in the buffer.
     */
    public int getCurrentSize() {
        return this.size;
    }

    /**
     * Returns the maximum size of the buffer.
     * @return The maximum size of the buffer.
     */
    public int getSize() {
        return this.buffer.length;
    }
}
