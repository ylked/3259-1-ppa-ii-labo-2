package ch.hearc.nde.manager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StatisticsManager {
    /**
     * This map contains the time at which a hash was produced.
     * The key of the map is the hash and the value is the timestamp.
     * This map only contains the hashes that have been produced but not consumed.
     * It is used to calculate the latency.
     */
    private final Map<String, Long> produced;

    /**
     * This list contains the latencies of the elements that have been consumed.
     * It is limited to the last SAMPLE_SIZE hashes. (otherwise it would saturate the memory).
     */
    private final List<Long> latencies;

    /**
     * This list contains the timestamp at which an element has been consumed.
     * It is limited to the last SAMPLE_SIZE hashes. (otherwise it would saturate the memory).
     * It is used to calculate the average debit.
     */
    private final List<Long> processingTimestamps;

    /**
     * The number of elements to consider for the statistics.
     */
    int sampleSize = 1000;


    /**
     * Creates a new statistics manager.
     */
    public StatisticsManager() {
        this.produced = new HashMap<>();

        // using linked list for fast insertion
        this.processingTimestamps = new LinkedList<>();
        this.latencies = new LinkedList<>();
    }

    public String computeHash(String msg) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(msg.getBytes());
            return new String(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Signals that a message has been consumed.
     *
     * @param msg The message consumed.
     */
    synchronized public void consume(String msg) {
        String hash = computeHash(msg);

        long now = System.currentTimeMillis();
        processingTimestamps.add(now);

        assert produced.containsKey(hash);

        Long then = produced.remove(hash);
        long latency = now - then;

        latencies.add(latency);

        if (processingTimestamps.size() > sampleSize) {
            processingTimestamps.removeFirst();
        }

        if (latencies.size() > sampleSize) {
            latencies.removeFirst();
        }

        //System.out.println("Consumed message " + msg + " in " + latency + "ms");
    }

    /**
     * Signals that the production of a message has started.
     *
     * @param msg The message produced.
     */
    synchronized public void produce(String msg) {
        String hash = computeHash(msg);
        assert hash != null;
        assert !produced.containsKey(hash);

        long now = System.currentTimeMillis();
        produced.put(hash, now);
    }

    /**
     * Returns the average latency of the last SAMPLE_SIZE elements.
     *
     * @return The average latency in milliseconds.
     */
    synchronized public double getAverageLatency() {
        assert latencies.size() <= sampleSize;
        return latencies.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }

    /**
     * Returns the debit of the last SAMPLE_SIZE elements.
     *
     * @return The debit in elements per second.
     */
    synchronized public double getDebit() {
        assert processingTimestamps.size() <= sampleSize;

        // if there is not enough data to calculate the debit
        if (processingTimestamps.size() < 2) {
            System.err.println("WARNING : currently not enough data to calculate the debit." +
                    "(current size = " + processingTimestamps.size() + ")");
            return 0;
        }

        long first = processingTimestamps.getFirst();
        long last = processingTimestamps.getLast();

        // if the first and last element were processed at the same time
        // this should not happen when the sample size is big enough.
        // Otherwise, it could happen if the system does not have
        // enough precision to measure the time and gets the same timestamp
        // for two very close events.
        if (first == last) {
            System.err.println("WARNING : first and last element were processed at the same time. " +
                    "The current debit value is INVALID. Consider choosing a greater sample size. " +
                    "(current sample size = " + sampleSize + ")");
            return 0;
        }

        double totalTime = processingTimestamps.getLast() - processingTimestamps.getFirst(); // casting to double
        double nbElementsProcessed = processingTimestamps.size(); // casting to double
        return nbElementsProcessed / (totalTime / 1000.0);
    }

    /**
     * Sets the number of elements to consider for the statistics.
     *
     * @param n The number of elements to consider.
     */
    synchronized public void setSampleSize(int n) {
        this.sampleSize = n;

        // the minimum sample size is 2, otherwise, the statistics would not make sense.
        if (sampleSize < 2) {
            sampleSize = 2;
        }

        this.latencies.clear();
        this.processingTimestamps.clear();
    }

    /**
     * Returns the number of elements to consider for the statistics.
     *
     * @return The number of elements to consider.
     */
    synchronized public int getSampleSize() {
        return this.sampleSize;
    }

    synchronized public void reset() {
        this.produced.clear();
        this.latencies.clear();
        this.processingTimestamps.clear();
        System.out.println("Statistics reset");
    }
}
