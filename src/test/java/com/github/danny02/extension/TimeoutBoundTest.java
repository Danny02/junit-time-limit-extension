package com.github.danny02.extension;

import com.github.danny02.extension.TimeoutBound;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeoutBoundTest {
    static final long LOWER = 100;
    static final long UPPER = 200;

    TimeoutBound bound = TimeoutBound.fromMs(LOWER, UPPER);

    @Test
    void shouldCorrectlyUseMilliseconds() {
        Duration lower = Duration.ofSeconds(2);
        Duration upper = Duration.ofSeconds(4);
        TimeoutBound created = TimeoutBound.fromMs(lower.toMillis(), upper.toMillis());
        assertEquals(new TimeoutBound(lower, upper), created);
    }

    @Test
    void smallerDurationShouldBeOutOfBounds() {
        assertFalse(bound.isInBound(ofMillis(LOWER - 1)));
    }

    @Test
    void lowerBoundShouldBeInBounds() {
        assertTrue(bound.isInBound(ofMillis(LOWER)));
    }

    @Test
    void insideBoundShouldBeInBounds() {
        assertTrue(bound.isInBound(ofMillis((UPPER - LOWER) / 2 + LOWER)));
    }

    @Test
    void upperBoundShouldBeInBounds() {
        assertTrue(bound.isInBound(ofMillis(UPPER)));
    }

    @Test
    void biggerDurationShouldBeOutOfBounds() {
        assertFalse(bound.isInBound(ofMillis(UPPER + 1)));
    }
}