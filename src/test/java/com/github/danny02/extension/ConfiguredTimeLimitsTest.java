package com.github.danny02.extension;

import com.github.danny02.extension.ConfiguredTimeLimits;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static com.github.danny02.extension.TimeoutBound.fromMs;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfiguredTimeLimitsTest {

    @Test
    void shouldSelectSmallerBound() {
        ConfiguredTimeLimits conf = new ConfiguredTimeLimits(
                s -> empty(), s -> empty(),
                Map.of("small", fromMs(0, 100),
                       "big", fromMs(100, 200)));

        assertEquals(Optional.of("small"), conf.smallestDefaultCategoryForDuration(ofMillis(50)));
    }

    @Test
    void shouldSelectSmallerBoundIfIntersecting() {
        ConfiguredTimeLimits conf = new ConfiguredTimeLimits(
                s -> empty(), s -> empty(),
                Map.of("small", fromMs(0, 100),
                       "big", fromMs(80, 200)));

        assertEquals(Optional.of("small"), conf.smallestDefaultCategoryForDuration(ofMillis(90)));
    }

    @Test
    void shouldSelectSmallestPossibleBound() {
        ConfiguredTimeLimits conf = new ConfiguredTimeLimits(
                s -> empty(), s -> empty(),
                Map.of("small", fromMs(0, 100),
                       "medium", fromMs(100, 150),
                       "big", fromMs(150, 200)));

        assertEquals(Optional.of("medium"), conf.smallestDefaultCategoryForDuration(ofMillis(110)));
    }

    @Test
    void shouldntReturnBoundIfNotDefinedForRuntime() {
        ConfiguredTimeLimits conf = new ConfiguredTimeLimits(
                s -> empty(), s -> empty(),
                Map.of("small", fromMs(0, 100),
                       "big", fromMs(150, 200)));

        assertEquals(Optional.empty(), conf.smallestDefaultCategoryForDuration(ofMillis(110)));
    }

    @Test
    void shouldReturnDefaultBoundIfNotConfigured() {
        ConfiguredTimeLimits conf = new ConfiguredTimeLimits(
                s -> empty(), s -> empty(),
                Map.of("small", fromMs(0, 100)));

        assertEquals(fromMs(0, 100), conf.lookUpBounds("small"));
    }

    @Test
    void shouldOverwriteLowerBoundWithConfiguredValue() {
        ConfiguredTimeLimits conf = new ConfiguredTimeLimits(
                s -> Optional.of(ofMillis(50)),
                s -> empty(),
                Map.of("small", fromMs(0, 100)));

        assertEquals(fromMs(50, 100), conf.lookUpBounds("small"));
    }

    @Test
    void shouldOverwriteUpperBoundWithConfiguredValue() {
        ConfiguredTimeLimits conf = new ConfiguredTimeLimits(
                s -> empty(),
                s -> Optional.of(ofMillis(50)),
                Map.of("small", fromMs(0, 100)));

        assertEquals(fromMs(0, 50), conf.lookUpBounds("small"));
    }

    @Test
    void shouldAllowBoundWithoutDefaultValueIfConfigured() {
        ConfiguredTimeLimits conf = new ConfiguredTimeLimits(
                s -> Optional.of(ofMillis(0)),
                s -> Optional.of(ofMillis(50)),
                Map.of());

        assertEquals(fromMs(0, 50), conf.lookUpBounds("small"));
    }

    @Test
    void shouldThrowIfOnlyLowerIsConfigured() {
        ConfiguredTimeLimits conf = new ConfiguredTimeLimits(
                s -> Optional.of(ofMillis(0)),
                s -> empty(),
                Map.of());

        assertThrows(RuntimeException.class, () -> conf.lookUpBounds("small"));
    }

    @Test
    void shouldThrowIfOnlyUpperIsConfigured() {
        ConfiguredTimeLimits conf = new ConfiguredTimeLimits(
                s -> empty(),
                s -> Optional.of(ofMillis(0)),
                Map.of());

        assertThrows(RuntimeException.class, () -> conf.lookUpBounds("small"));
    }

    @Test
    void shouldThrowIfNothingIsConfigured() {
        ConfiguredTimeLimits conf = new ConfiguredTimeLimits(
                s -> empty(), s -> empty(),
                Map.of());

        assertThrows(RuntimeException.class, () -> conf.lookUpBounds("small"));
    }

    @Test
    void shouldValidateExpectedCategory() {
        ConfiguredTimeLimits conf = new ConfiguredTimeLimits(
                s -> empty(), s -> empty(),
                Map.of("small", fromMs(0, 50)));

        assertEquals(empty(), conf.validateRuntime(ofMillis(25), "small"));
    }

    @Test
    void shouldValidateWithError() {
        ConfiguredTimeLimits conf = new ConfiguredTimeLimits(
                s -> empty(), s -> empty(),
                Map.of("small", fromMs(0, 50)));

        String error = conf.validateRuntime(ofSeconds(1), "small").get();
        assertTrue(error.contains("small"), error);
        assertTrue(error.contains("1000ms"), error);
        assertTrue(error.contains(fromMs(0, 50).toString()), error);
    }

    @Test
    void shouldPrintIdealCategoryOnError() {
        ConfiguredTimeLimits conf = new ConfiguredTimeLimits(
                s -> empty(), s -> empty(),
                Map.of("small", fromMs(0, 500),
                       "big", fromMs(500, 2000)));

        String error = conf.validateRuntime(ofSeconds(1), "small").get();
        assertTrue(error.contains("big"), error);
    }

}