/*
 * Copyright 2012 - 2026 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tinymediamanager.logging;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;

/**
 * Utility class for logging stuff
 * 
 * @author Manuel Laggner
 */
public class TmmLoggingUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(TmmLoggingUtils.class);

  private TmmLoggingUtils() {
    // private constructor for utility class
    throw new IllegalAccessError();
  }

  /**
   * Converts a string representation of a log level to the corresponding {@link Level} object.
   * 
   * @param level
   *          the string representation of the log level (e.g., "DEBUG", "INFO", "WARN", "ERROR", "OFF")
   * @return the corresponding {@link Level} object, or null if the input is blank
   * @throws IllegalArgumentException
   *           if the input string does not match any known log level
   */
  public static Level getLoggingLevel(String level) {
    if (StringUtils.isBlank(level)) {
      return null;
    }

    return switch (level.toUpperCase(Locale.ROOT).strip()) {
      case "OFF", "NONE" -> Level.OFF;
      case "ERROR" -> Level.ERROR;
      case "WARN" -> Level.WARN;
      case "INFO" -> Level.INFO;
      case "DEBUG" -> Level.DEBUG;
      case "TRACE" -> Level.TRACE;
      default -> throw new IllegalArgumentException("Unknown log level: " + level);
    };
  }

  /**
   * Stops the startup appender (if existing)
   */
  public static void stopStartupAppender() {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    // get the console appender
    Appender<ILoggingEvent> appender = lc.getLogger("ROOT").getAppender("STARTUP");
    if (appender != null) {
      appender.stop();
    }
  }

  /**
   * Sets the console log level according to the system property "tmm.consoleloglevel". If the property is not set or invalid, the console appender
   * will be stopped.
   * <p>
   * Valid values are: OFF, ERROR, WARN, INFO, DEBUG, TRACE (case insensitive)
   */
  public static void setConsoleLogLevel() {
    try {
      Level level = getLoggingLevel(System.getProperty("tmm.consoleloglevel", ""));

      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

      // get the console appender
      Appender<ILoggingEvent> consoleAppender = lc.getLogger("ROOT").getAppender("CONSOLE");
      if (consoleAppender instanceof ConsoleAppender) {
        if (level == null) {
          consoleAppender.stop();
        }
        else {
          // and set a filter to drop messages beneath the given level
          ThresholdLoggerFilter filter = new ThresholdLoggerFilter(level);
          filter.start();
          consoleAppender.clearAllFilters();
          consoleAppender.addFilter(filter);
        }
      }
    }
    catch (Exception e) {
      // just log it - we do not want to break anything here
      LOGGER.error("Could not set console log level: {}", e.getMessage());
    }
  }

  /**
   * Sets the log level of a specific logger according to a given system property.
   * <p>
   * If the system property is not set or invalid, the log level of the specified logger will remain unchanged.
   * <p>
   * Valid values for the system property are: OFF, ERROR, WARN, INFO, DEBUG, TRACE (case insensitive)
   * 
   * @param loggerName
   *          the name of the logger to set the log level for
   * @param jvmParam
   *          the name of the system property that contains the desired log level
   */
  public static void setLogLevel(String loggerName, String jvmParam) {
    String levelStr = System.getProperty(jvmParam);
    if (StringUtils.isBlank(levelStr)) {
      return;
    }

    try {
      LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();

      ch.qos.logback.classic.Logger logger = ctx.getLogger(loggerName);

      logger.setLevel(Level.DEBUG);
    }
    catch (Exception e) {
      // just log it - we do not want to break anything here
      LOGGER.error("Could not set log level for {}: {}", loggerName, e.getMessage());
    }
  }
}
