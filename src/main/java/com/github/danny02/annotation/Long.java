package com.github.danny02.annotation;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Tag("long")
@TimeLimit("long")
@Retention(RetentionPolicy.RUNTIME)
public @interface Long {
}
