package com.github.danny02.extension;

import com.github.danny02.annotation.TimeLimit;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.unmodifiableMap;
import static java.util.Comparator.comparingLong;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

public class TimeLimitExtension implements Extension, BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private static final String START_TIME = "start time";
    private static final String TIMEOUT_PARAMETER_PREFIX = "com.github.danny02.timeout.";
    private static final String TIMEOUT_LOWER = ".lower";
    private static final String TIMEOUT_UPPER = ".upper";


    private static final Map<String, TimeoutBound> DEFAULTS = unmodifiableMap(new HashMap<String, TimeoutBound>() {{
        put("short", new TimeoutBound(0, 100));
        put("medium", new TimeoutBound(80, 500));
        put("long", new TimeoutBound(400, 1500));
        put("eternal", new TimeoutBound(1500, Long.MAX_VALUE));
    }});

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        getStore(context).put(START_TIME, System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        Method testMethod = context.getRequiredTestMethod();
        long startTime = getStore(context).remove(START_TIME, long.class);
        long duration = System.currentTimeMillis() - startTime;

        Optional<TimeLimit> timeout = first(
                findAnnotation(testMethod, TimeLimit.class),
                findAnnotation(testClass, TimeLimit.class)
        );

        timeout.ifPresent(anno -> {
            String category = anno.value();
            String idealCategory = smallestCategoryForDuration(duration);
            TimeoutBound b = lookUpBounds(context, category);
            if (duration < b.lower || duration > b.upper) {
                String error = String.format("The test run for %dms and was categorized as '%s', " +
                        "but it did not complete in between [%dms, %dms].\n" +
                        "You should probably categorize it as '%s'", duration, category, b.lower, b.upper, idealCategory);
                throw new RuntimeException(error);
            }
        });
    }

    private String smallestCategoryForDuration(long duration) {
        return DEFAULTS.entrySet()
                .stream()
                .filter(e -> e.getValue().lower <= duration && duration <= e.getValue().upper)
                .sorted(comparingLong(a -> a.getValue().lower))
                .findFirst()
                .map(e -> e.getKey())
                .orElse("short");
    }

    private TimeoutBound lookUpBounds(ExtensionContext context, String category) {
        Optional<Long> lower = getTimeout(context, category, TIMEOUT_LOWER);
        Optional<Long> upper = getTimeout(context, category, TIMEOUT_UPPER);
        Optional<TimeoutBound> configured = lower.flatMap(l -> upper.map(u -> new TimeoutBound(l, u)));

        Optional<TimeoutBound> defaultBound = Optional.ofNullable(DEFAULTS.get(category));

        return first(configured, defaultBound).orElseThrow(() -> {
            if (!lower.isPresent()) {
                return new RuntimeException("no configured lower bound or default for timeout category '" + category + "'");
            }
            if (!upper.isPresent()) {
                return new RuntimeException("no upper bound or default for timeout category '" + category + "'");
            }
            throw new UnsupportedOperationException("should be unreachable");
        });
    }

    private Optional<Long> getTimeout(ExtensionContext context, String category, String bound) {
        return context.getConfigurationParameter(TIMEOUT_PARAMETER_PREFIX + category + bound)
                .map(Long::parseLong);
    }

    private <T> Optional<T> first(Optional<T>... options) {
        for (Optional<T> o : options) {
            if (o.isPresent()) {
                return o;
            }
        }
        return Optional.empty();
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
    }

    private static class TimeoutBound {
        final long lower, upper;

        private TimeoutBound(long lower, long upper) {
            this.lower = lower;
            this.upper = upper;
        }
    }
}
