/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.1
 */
final class SLF4JLogger extends Logger {
    private final org.slf4j.Logger LOGGER;

    SLF4JLogger(final org.slf4j.Logger logger) {
        LOGGER = logger;
    }

    @Override
    public boolean isDebugEnabled() {
        return LOGGER.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return LOGGER.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return LOGGER.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return LOGGER.isErrorEnabled();
    }

    @Override
    public void debug(final String message) {
        LOGGER.debug(message);
    }

    @Override
    public void debug(final String message, final String message2) {
        LOGGER.debug(message + message2);
    }

    @Override
    public void info(final String message) {
        LOGGER.info(message);
    }

    @Override
    public void info(final String message, final String message2) {
        LOGGER.info(message + message2);
    }

    @Override
    public void warn(final String message) {
        LOGGER.warn(message);
    }

    @Override
    public void warn(final String message, final String message2) {
        LOGGER.warn(message + message2);
    }

    @Override
    public void warn(final String message, final Throwable th) {
        LOGGER.warn(message, th);
    }

    @Override
    public void error(final String message) {
        LOGGER.error(message);
    }

    @Override
    public void error(final String message, final Throwable th) {
        LOGGER.error(message, th);
    }
}
