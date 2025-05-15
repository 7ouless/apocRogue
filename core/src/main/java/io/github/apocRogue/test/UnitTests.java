package io.github.apocRogue.test;

import io.github.apocRogue.globals.stats.StatsComponent;

import static org.junit.Assert.assertEquals;


public class UnitTests {
    /**
     * Test {@link StatsComponent}
     */
    @org.junit.Test
    public void test1() {
        StatsComponent stats = new StatsComponent(100, 100, 10, 2, 1200, 2, 2, 10, 0);

        // Test typical expected value for takeDamage
        stats.takeDamage(50);
        assertEquals(50, stats.getHealth());

        // Test health is capped at 0 at the lowest
        stats.takeDamage(1_000_000);
        assertEquals(0, stats.getHealth());

        assert(stats.isDead());

        // Test negative values are allowed & increase health (working as intended?)
        stats.takeDamage(-100);
        assertEquals(100, stats.getHealth());
    }
}
