package net.lego.database.deployer.postprocessors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PercentCompleteTabulatorTest {
    @Test
    void testPercentCompleteTabulator() {
        PercentCompleteTabulator percentCompleteTabulator = new PercentCompleteTabulator(1000, .01d, d -> {
            System.out.println(d);
        });
        for (int i = 0; i < 1000; i++) {
            percentCompleteTabulator.incrementPercentComplete();
        }
    }

}