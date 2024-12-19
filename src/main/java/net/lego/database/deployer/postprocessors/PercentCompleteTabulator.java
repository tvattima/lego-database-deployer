package net.lego.database.deployer.postprocessors;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class PercentCompleteTabulator {
    private final long totalToComplete;
    private final double percentIncrements;
    private final Consumer<Double> completionConsumer;

    public PercentCompleteTabulator(long totalToComplete, double percentIncrements, Consumer<Double> completionConsumer) {
        this.totalToComplete = totalToComplete;
        this.percentIncrements = percentIncrements;
        this.completionConsumer = completionConsumer;
        currentPercentIncrement = this.percentIncrements;
    }

    private final AtomicLong completedCount = new AtomicLong(0);
    private Double currentPercentIncrement = 0.0d;

    public double percentComplete() {
        return (double)completedCount.get() / (double)totalToComplete;
    }

    public void incrementPercentComplete() {
        completedCount.incrementAndGet();
        if (percentComplete() > currentPercentIncrement) {
            currentPercentIncrement = currentPercentIncrement + percentIncrements;
            completionConsumer.accept(percentComplete());
        }
    }
}
