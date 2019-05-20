package com.avocado.common.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * ThreadUtils class
 *
 * @author xuning
 * @date 2019-05-07 14:54
 */
@Slf4j
@UtilityClass
public class ThreadUtils {

    public void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {

        }
    }
}
