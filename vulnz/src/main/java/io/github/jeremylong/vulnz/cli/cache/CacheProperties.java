/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) 2023-2024 Jeremy Long. All Rights Reserved.
 */
package io.github.jeremylong.vulnz.cli.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class CacheProperties {
    private static final String NAME = "cache.properties";
    Properties properties = new Properties();
    private File directory;

    public CacheProperties(File dir) {
        directory = dir;
        if (!directory.isDirectory() && !directory.mkdirs()) {
            throw new CacheException("Unable to create cache directory: " + directory);
        }
        File file = new File(directory, NAME);
        if (file.isFile()) {
            try (InputStream input = new FileInputStream(file)) {
                properties.load(input);
            } catch (IOException exception) {
                throw new CacheException("Unable to create read properties file: " + directory, exception);
            }
        }
    }

    public boolean has(String key) {
        return properties.containsKey(key);
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public ZonedDateTime getTimestamp(String key) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssX");
        if (has(key)) {
            String value = get(key);
            return ZonedDateTime.parse(value, dtf);
        }
        return null;
    }

    public void set(String key, ZonedDateTime timestamp) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssX");
        set(key, dtf.format(timestamp));
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
    }

    public File getDirectory() {
        return directory;
    }

    public void save() throws CacheException {
        File file = new File(directory, NAME);
        try (OutputStream output = new FileOutputStream(file, false)) {
            properties.store(output, null);
        } catch (IOException exception) {
            throw new CacheException("Unable to write properties file: " + directory, exception);
        }
    }
}
