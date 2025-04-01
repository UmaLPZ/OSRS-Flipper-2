package com.flipper2.helpers;

import lombok.extern.slf4j.Slf4j;

/**
 * Prevents plugin from logging unless in DEV_MODE = true
 * ^not accurate but original dev set this so keeping it anyways
 * modified the Log class since changing DEV_MODE here does nothing
 * Now just acts as a way to log when in debug mode
 */
@Slf4j
public class Log {
    public static void info(String message) {
            log.debug("FLIPPER PLUGIN: " + message);
    }
}