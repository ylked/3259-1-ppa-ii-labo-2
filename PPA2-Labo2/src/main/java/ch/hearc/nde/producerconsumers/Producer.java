package ch.hearc.nde.producerconsumers;

import ch.hearc.nde.buffer.CircularBuffer;
import ch.hearc.nde.manager.StatisticsManager;

public class Producer extends ProducerConsumer {
    public Producer(CircularBuffer buffer, StatisticsManager statisticsManager, int id, boolean infinite) {
        super(buffer, statisticsManager, id, infinite);
        setName("Producer-" + id);
    }

    @Override
    protected void operate(int i) throws InterruptedException {
        // Create a message
        String msg = createMessage(i);

        // Notify the statistics manager that a message is being produced
        this.statistics.produce(msg);
        long start = System.currentTimeMillis();

        // Simulate the time it takes to produce the message
        Thread.sleep(operationDuration);

        assert System.currentTimeMillis() - start >= operationDuration;

        // Put the message into the buffer
        this.buffer.produce(msg);
    }

    @Override
    protected void log(String msg) {
        System.out.println(getName() + ": " + msg);
    }
}
