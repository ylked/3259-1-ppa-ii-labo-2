package ch.hearc.nde.producerconsumers;

import ch.hearc.nde.buffer.CircularBuffer;

public abstract class ProducerConsumer extends Thread {
    protected final CircularBuffer buffer;
    protected final int id;
    private final boolean infinite;
    private static final int NB_ITER_IF_NOT_INFINITE = 10;
    private int operationDuration = 200;

    public ProducerConsumer(CircularBuffer buffer, int id, boolean infinite) {
        this.buffer = buffer;
        this.id = id;
        this.infinite = infinite;
    }

    public ProducerConsumer(CircularBuffer buffer, int id) {
        this(buffer, id, false);
    }

    protected void log(String msg) {
        //System.out.println("<ProducerConsumer> " + id + ": " + msg);
    }

    protected String createMessage(int i) {
        return "message " + i + " from " + id;
    }

    protected abstract void operate(int i) throws InterruptedException;

    private void runInfinitely() throws InterruptedException {
        int i = 0;
        while (true) {
            Thread.sleep(operationDuration);
            operate(i++);
        }
    }

    private void runNTimes() throws InterruptedException {
        for (int i = 0; i < NB_ITER_IF_NOT_INFINITE; i++) {
            Thread.sleep(operationDuration);
            operate(i);
        }
    }

    @Override
    public void run() {
        log("started run method");
        try {
            if (infinite) {
                runInfinitely();
            } else {
                runNTimes();
            }
        } catch (InterruptedException ignored) {
            log("interrupted");
        }
    }

    public void setOperationDuration(int operationDuration) {
        this.operationDuration = operationDuration;
    }
}
