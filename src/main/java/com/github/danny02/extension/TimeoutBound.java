package com.github.danny02.extension;

import java.time.Duration;
import java.util.Objects;

import static java.time.Duration.ofMillis;

public class TimeoutBound {
    final Duration lower, upper;

    public TimeoutBound(Duration lower, Duration upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public static TimeoutBound fromMs(long lowerInMs, long upperInMs) {
        return new TimeoutBound(ofMillis(lowerInMs), ofMillis(upperInMs));
    }

    public boolean isInBound(Duration d) {
        return d.compareTo(lower) >= 0 && d.compareTo(upper) <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeoutBound that = (TimeoutBound) o;
        return Objects.equals(lower, that.lower) &&
                Objects.equals(upper, that.upper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lower, upper);
    }

    public String toString() {
        return String.format("[%s, %s]", lower, upper);
    }
}
