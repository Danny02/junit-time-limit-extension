package com.github.danny02.extension;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.github.danny02.extension.TimeoutBound.fromMs;
import static java.util.Comparator.comparingLong;

public class ConfiguredTimeLimits {

    private static final Map<String, TimeoutBound> DEFAULTS = Map.of(
            "short", fromMs(0, 100),
            "medium", fromMs(80, 500),
            "long", fromMs(400, 1500),
            "eternal", fromMs(1500, Long.MAX_VALUE)
                                                                    );

    private final Map<String, TimeoutBound>            defaults;
    private final Function<String, Optional<Duration>> lookupLower, lookupUpper;

    public ConfiguredTimeLimits(Function<String, Optional<Duration>> lookupLower,
                                Function<String, Optional<Duration>> lookupUpper) {
        this(lookupLower, lookupUpper, DEFAULTS);
    }

    public ConfiguredTimeLimits(Function<String, Optional<Duration>> lookupLower,
                                Function<String, Optional<Duration>> lookupUpper,
                                Map<String, TimeoutBound> defaults) {
        this.defaults = defaults;
        this.lookupLower = lookupLower;
        this.lookupUpper = lookupUpper;
    }

    public TimeoutBound lookUpBounds(String category) {
        Optional<TimeoutBound> defaultBound = Optional.ofNullable(defaults.get(category));

        Optional<Duration>     lower      = lookupLower.apply(category).or(() -> defaultBound.map(db -> db.lower));
        Optional<Duration>     upper      = lookupUpper.apply(category).or(() -> defaultBound.map(db -> db.upper));
        Optional<TimeoutBound> configured = lower.flatMap(l -> upper.map(u -> new TimeoutBound(l, u)));

        return configured.orElseThrow(() -> {
            if (!lower.isPresent()) {
                return new RuntimeException("no configured lower bound or default for timeout category '" + category + "'");
            }
            if (!upper.isPresent()) {
                return new RuntimeException("no upper bound or default for timeout category '" + category + "'");
            }
            throw new UnsupportedOperationException("should be unreachable");
        });
    }

    public Optional<String> smallestDefaultCategoryForDuration(Duration duration) {
        return defaults.entrySet()
                       .stream()
                       .filter(e -> e.getValue().isInBound(duration))
                       .sorted(comparingLong(a -> a.getValue().lower.toMillis()))
                       .findFirst()
                       .map(e -> e.getKey());
    }

    public Optional<String> validateRuntime(Duration runtime, String expectedCategory) {
        TimeoutBound expectedBounds = lookUpBounds(expectedCategory);

        if (expectedBounds.isInBound(runtime)) {
            return Optional.empty();
        }

        Optional<String> idealCategory = smallestDefaultCategoryForDuration(runtime);
        String idealCategoryMessage = idealCategory.map(ic -> "You should probably categorize it as '" + ic + "'")
                                                    .orElse("No default category defined for this runtime.");

        return Optional.of(String.format("The test run for %dms and was categorized as '%s', " +
                                         "but it did not complete in between %s.\n" +
                                         idealCategoryMessage,
                                         runtime.toMillis(), expectedCategory, expectedBounds));


    }
}
