package com.github.danny02.extension;

import com.github.danny02.annotation.TimeLimit;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

public class TimeLimitExtension implements Extension, BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private static final String START_TIME               = "start time";
    private static final String TIMEOUT_PARAMETER_PREFIX = "com.github.danny02.timeout.";
    private static final String TIMEOUT_LOWER            = ".lower";
    private static final String TIMEOUT_UPPER            = ".upper";


    private final Clock clock;

    public TimeLimitExtension() {
        this(Clock.systemDefaultZone());
    }

    public TimeLimitExtension(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        setStartTime(context, now());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Optional<String> category = getCategoryFromAnnotation(context.getRequiredTestMethod());
        category.ifPresent(cat -> {
            Duration             runtime      = Duration.between(getStartTime(context), now());
            ConfiguredTimeLimits configuration = fromContext(context);

            configuration.validateRuntime(runtime, cat).ifPresent(error -> {
                throw new RuntimeException(error);
            });
        });
    }

    public Optional<String> getCategoryFromAnnotation(Method testMethod) {
        Class<?> testClass = testMethod.getDeclaringClass();
        return findAnnotation(testMethod, TimeLimit.class).or(() -> findAnnotation(testClass, TimeLimit.class))
                                                          .map(a -> a.value());
    }

    private ConfiguredTimeLimits fromContext(ExtensionContext context) {
        Function<String, Function<String, Optional<Duration>>> lookup = bound -> category ->
                context.getConfigurationParameter(TIMEOUT_PARAMETER_PREFIX + category + bound)
                       .map(Long::parseLong)
                       .map(Duration::ofMillis);

        return new ConfiguredTimeLimits(lookup.apply(TIMEOUT_LOWER), lookup.apply(TIMEOUT_UPPER));
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private void setStartTime(ExtensionContext context, Instant startTime) {
        getStore(context).put(START_TIME, startTime);
    }

    private Instant getStartTime(ExtensionContext context) {
        return getStore(context).remove(START_TIME, Instant.class);
    }

    private Store getStore(ExtensionContext context) {
        return context.getStore(Namespace.create(getClass(), context.getRequiredTestMethod()));
    }

}
