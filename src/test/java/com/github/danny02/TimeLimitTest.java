package com.github.danny02;

import com.github.danny02.annotation.Long;
import com.github.danny02.annotation.Short;
import org.junit.jupiter.api.Test;

@Long
public class TimeLimitTest {

    @Test
    @Short
    void run50ms() throws InterruptedException {
        Thread.sleep(50);
    }

    @Test
    void mediumTestWhichShouldFail() throws InterruptedException {
        Thread.sleep(200);
    }

    @Test
    void longTest() throws InterruptedException {
        Thread.sleep(600);
    }
}
