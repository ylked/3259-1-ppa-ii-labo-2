package ch.hearc.nde.manager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class StatisticsManager {
    /**
     * This map contains the time at which a hash was produced.
     * The key of the map is the hash and the value is the time in milliseconds.
     * This map only contains the hashes that have been produced but not consumed.
     */
    private final Map<Integer, Long> produced;

    /**
     * This list contains the timestamp at which an element has been consumed.
     * It is limited to the last 1000 hashes. (otherwise it would saturate the memory)
     */
    private final List<Long> processingTimestamps;


    public StatisticsManager() {
        this.produced = new HashMap<>();

        // using linked list for fast insertion
        this.processingTimestamps = new LinkedList<>();
    }

    synchronized public void consume(int hash){
        processingTimestamps.add(System.currentTimeMillis());

        produced.remove(hash);

        if(processingTimestamps.size() > 1000){
            processingTimestamps.removeFirst();
        }
    }

    synchronized public void produce(int hash){
        produced.put(hash, System.currentTimeMillis());
    }

    synchronized public double getAverageLatency(){
        return IntStream.range(1, processingTimestamps.size())
                .mapToDouble(i ->processingTimestamps.get(i) - processingTimestamps.get(i - 1))
                .average()
                .orElse(0);
    }

    synchronized public double getDebit(){
        double totalTime = processingTimestamps.getLast() - processingTimestamps.getFirst();
        double nbElementsProcessed = processingTimestamps.size();
        return nbElementsProcessed / (totalTime / 1000.0);
    }

}
