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
 * Copyright (c) 2024 Jeremy Long. All Rights Reserved.
 */
package io.github.jeremylong.vulnz.cli.ui;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.layout.TTLLLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import org.jline.terminal.Terminal;

public class JLineAppender extends AppenderBase<ILoggingEvent> {

    private Layout<ILoggingEvent> layout;

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    @Override
    protected void append(ILoggingEvent event) {
        Terminal terminal = ProgressMonitor.getTerminal();
        if (terminal != null) {
            terminal.writer().println(layout.doLayout(event));
            terminal.flush();
        } else {
            if (event.getLevel() == Level.TRACE || event.getLevel() == Level.DEBUG) {
                System.err.println(layout.doLayout(event));
            } else {
                System.out.println(layout.doLayout(event));
            }
        }
    }
}
