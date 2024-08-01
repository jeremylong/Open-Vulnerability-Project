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
package io.github.jeremylong.vulnz.cli.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.Status;

public class ProgressMonitor implements IProgressMonitor {

    private static Terminal terminal = null;
    private Status status;
    private boolean enabled;
    private Map<String, Integer> rows = new HashMap<>();

    static Terminal getTerminal() {
        return terminal;
    }

    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    public ProgressMonitor(boolean enabled) throws IOException {
        this(enabled, null);
    }

    @SuppressFBWarnings({"CT_CONSTRUCTOR_THROW", "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"})
    public ProgressMonitor(boolean enabled, String name) throws IOException {
        this.enabled = enabled;
        if (enabled) {
            if (name != null) {
                addMonitor(name);
            }
            terminal = TerminalBuilder.terminal();
            status = new Status(terminal);
        }
    }

    @Override
    public void addMonitor(String name) {
        this.rows.put(name, 0);
    }

    private List<AttributedString> determineStatusBar() {
        int maxNameWidth = rows.keySet().stream().mapToInt(String::length).max().orElse(0);
        return rows.entrySet().stream().map(entry -> {
            String name = entry.getKey();
            int percent = entry.getValue();
            int remaining = terminal.getWidth();
            remaining = Math.min(remaining, 100);
            StringBuilder string = new StringBuilder(remaining);
            remaining -= maxNameWidth;
            string.append(name);
            int spaces = maxNameWidth - name.length();
            if (spaces > 0) {
                string.append(String.join("", Collections.nCopies(spaces, " ")));
            }
            if (percent >= 100) {
                string.append(" complete");
            } else {
                String spacer = percent < 10 ? " " : "";
                string.append(spacer).append(String.format(" %d%% [", percent));
                remaining -= 10;
                int completed = remaining * percent / 100;
                int filler = remaining - completed;
                System.out.println("completed: " + completed + " filler: " + filler + " remaining: " + remaining);
                string.append(String.join("", Collections.nCopies(completed, "="))).append('>')
                        .append(String.join("", Collections.nCopies(filler, " "))).append(']');
            }
            String s = string.toString();
            return new AttributedString(s);
        }).sorted().collect(Collectors.toList());
    }

    @Override
    public void updateProgress(String name, int current, int max) {
        int percent = (int) (current * 100 / max);
        rows.put(name, percent);
        if (enabled) {
            status.update(new ArrayList<AttributedString>());
            status.resize();
            List<AttributedString> displayedRows = determineStatusBar();
            status.update(displayedRows, true);
        }
    }

    @Override
    public void close() throws Exception {
        if (enabled) {
            if (status != null) {
                status.close();
            }
            closeTerminal();
            enabled = false;
        }
    }

    static void closeTerminal() throws IOException {
        if (terminal != null) {
            terminal.close();
            terminal = null;
        }
    }
}
