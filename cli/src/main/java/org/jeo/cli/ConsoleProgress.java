/* Copyright 2013 The jeo project. All rights reserved.
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
package org.jeo.cli;

import java.io.PrintStream;

import jline.console.ConsoleReader;

import com.google.common.base.Strings;

/**
 * Displays progress on the console.
 */
public class ConsoleProgress {

    ConsoleReader console;
    int total;
    int count;

    public ConsoleProgress(ConsoleReader console, int total) {
        this.console = console;
        init(total);
    }

    public ConsoleProgress init(int total) {
        this.total = total;
        count = 0;
        return this;
    }

    /**
     * Increments progress by one unit.
     */
    public ConsoleProgress inc() {
        return inc(1);
    }

    /**
     * Increments progress by the specified amount.
     */
    public ConsoleProgress inc(int amt) {
        count += amt;
        return redraw();
    }

    /**
     * Redraws the progress bar.
     * <p>
     * This method is called from {@link #inc()} so no need to call it explicitly.
     * </p>
     */
    public ConsoleProgress redraw() {
        if (total < 0) {
            return this;
        }

        PrintStream out = System.out;

        //number of digits in total to padd count
        int n = (int)(Math.log10(total)+1);

        StringBuilder sb = new StringBuilder();

        // first encode count/total
        sb.append("[").append(String.format("%"+n+"d", count))
            .append("/").append(total).append("]");

        // second is percent
        sb.append(" ").append(String.format("%3.0f", (count/(float)total * 100))).append("% ");

        //last is progress bar
        int left = console.getTerminal().getWidth() - sb.length() - 2;
        int progress = count * left / total;

        sb.append("[")
            .append(Strings.repeat("=", progress))
            .append(Strings.repeat(" ", left-progress))
            .append("]");

        out.print(sb.toString() + "\r");
        return this;
    }
}
