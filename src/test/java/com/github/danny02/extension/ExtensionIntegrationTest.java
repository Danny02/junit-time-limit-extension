package com.github.danny02.extension;

import com.github.danny02.annotation.Medium;
import com.github.danny02.annotation.Short;
import com.github.danny02.annotation.TimeLimit;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class ExtensionIntegrationTest {

    @Medium
    static class DifferentCategories {

        @Test
        @Short
        void shortRunningTest() {

        }

        @Test
        void mediumRunningTest() throws InterruptedException {
            Thread.sleep(120);
        }

        @Test
        @TimeLimit("custom")
        void customCategoryTest() throws InterruptedException {
            Thread.sleep(50);
        }
    }

    @Test
    void shouldRunAllTestSucessfully() {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
                .request()
                .selectors(
                        selectClass(DifferentCategories.class)
                          )
                .filters()
                .configurationParameters(Map.of(
                        "junit.jupiter.extensions.autodetection.enabled", "true",
                        "com.github.danny02.timeout.custom.lower", "30",
                        "com.github.danny02.timeout.custom.upper", "60"
                                               ))
                .build();

        Launcher launcher = LauncherFactory.create();

        // Register a listener of your choice
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        launcher.execute(request);

        assertEquals(3, listener.getSummary().getTestsSucceededCount());
    }

    static class WrongCategories {

        @Test
        @Short
        void shortRunningTest() throws InterruptedException {
            Thread.sleep(120);
        }

        @Test
        @TimeLimit("not-existing")
        void customCategoryTest() {
        }
    }

    @Test
    void shouldFailAllTest() {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
                .request()
                .selectors(
                        selectClass(WrongCategories.class)
                          )
                .filters()
                .configurationParameters(Map.of(
                        "junit.jupiter.extensions.autodetection.enabled", "true"
                                               ))
                .build();

        Launcher launcher = LauncherFactory.create();

        // Register a listener of your choice
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        launcher.execute(request);

        assertEquals(2, listener.getSummary().getTestsFailedCount());
    }
}
