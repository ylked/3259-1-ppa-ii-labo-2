package ch.hearc.nde.producerconsumers;

import ch.hearc.nde.buffer.CircularBuffer;

public class Consumer extends ProducerConsumer {
    public Consumer(CircularBuffer buffer, int id, boolean infinite) {
        super(buffer, id, infinite);
        setName("Consumer-" + id);
    }

    @Override
    protected void operate(int i) throws InterruptedException {
        String msg = this.buffer.consume();
        //log("consumed " + msg);
    }

    @Override
    protected void log(String msg) {
        System.out.println(getName() + ": " + msg);
    }
}
