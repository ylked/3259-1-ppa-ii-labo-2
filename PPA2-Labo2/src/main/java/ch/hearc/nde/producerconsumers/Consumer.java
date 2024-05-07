package ch.hearc.nde.producerconsumers;

import ch.hearc.nde.buffer.CircularBuffer;
import ch.hearc.nde.manager.StatisticsManager;

public class Consumer extends ProducerConsumer {
    public Consumer(CircularBuffer buffer, StatisticsManager statisticsManager, int id, boolean infinite) {
        super(buffer, statisticsManager, id, infinite);
        setName("Consumer-" + id);
    }

    /**
     * Consume a message from the buffer. This method is blocking.
     * It will also wait a certain amount of time to simulate the time it takes to consume the message.
     * @param i A unique identifier for the message (unique in the context of this consumer).
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    @Override
    protected void operate(int i) throws InterruptedException {
        // Get the message from the buffer
        String msg = this.buffer.consume();

        // Simulate the time it takes to consume the message
        long start = System.currentTimeMillis();
        Thread.sleep(operationDuration);
        assert System.currentTimeMillis() - start >= operationDuration;

        // Notify the statistics manager that a message has been consumed
        this.statistics.consume(msg);
    }

    @Override
    protected void log(String msg) {
        System.out.println(getName() + ": " + msg);
    }
}
