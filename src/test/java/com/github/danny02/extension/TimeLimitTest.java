package com.github.danny02.extension;

import com.github.danny02.annotation.Long;
import com.github.danny02.annotation.Short;
import com.github.danny02.extension.TimeLimitExtension;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeLimitTest {

    @Long
    static class TestClassWithAnnotations {

        @Short
        void testWithAnnotation() {

        }

        void testWithoutAnnotation() {

        }
    }

    static class TestClassWithout {
        void testWithoutAnnotation() {

        }
    }

    @Test
    void shouldFindAnnotationOnTestMethod() throws NoSuchMethodException {
        TimeLimitExtension extension  = new TimeLimitExtension();
        Method             testMethod = TestClassWithAnnotations.class.getDeclaredMethod("testWithAnnotation");
        Optional<String>   category   = extension.getCategoryFromAnnotation(testMethod);
        assertEquals(Optional.of("short"), category);
    }

    @Test
    void shouldFindAnnotationOnTestClass() throws NoSuchMethodException {
        TimeLimitExtension extension  = new TimeLimitExtension();
        Method             testMethod = TestClassWithAnnotations.class.getDeclaredMethod("testWithoutAnnotation");
        Optional<String>   category   = extension.getCategoryFromAnnotation(testMethod);
        assertEquals(Optional.of("long"), category);
    }

    @Test
    void shouldntFindAnnotations() throws NoSuchMethodException {
        TimeLimitExtension extension  = new TimeLimitExtension();
        Method             testMethod = TestClassWithout.class.getDeclaredMethod("testWithoutAnnotation");
        Optional<String>   category   = extension.getCategoryFromAnnotation(testMethod);
        assertEquals(Optional.empty(), category);
    }
}
