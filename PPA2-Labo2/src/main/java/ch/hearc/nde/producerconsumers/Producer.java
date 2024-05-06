package ch.hearc.nde.producerconsumers;

import ch.hearc.nde.buffer.CircularBuffer;

public class Producer extends ProducerConsumer {
    public Producer(CircularBuffer buffer, int id, boolean infinite) {
        super(buffer, id, infinite);
        setName("Producer-" + id);
    }

    @Override
    protected void operate(int i) throws InterruptedException {
        String msg = createMessage(i);
        this.buffer.produce(msg);
        //log("produced " + msg);
    }

    @Override
    protected void log(String msg) {
        System.out.println(getName() + ": " + msg);
    }
}
