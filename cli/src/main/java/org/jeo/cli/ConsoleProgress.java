package org.jeo.cli;

import java.io.PrintStream;

import jline.console.ConsoleReader;

import com.google.common.base.Strings;

public class ConsoleProgress {

    ConsoleReader console;
    int total;
    int count;

    public ConsoleProgress(ConsoleReader console, int total) {
        this.console = console;
        this.total = total;
        this.count = 0;
    }

    public void progress() {
        progress(1);
    }
    
    public void progress(int amt) {
        count += amt;
        redraw();
    }

    public void redraw() {
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
//        console.getCursorBuffer().clear();
//        console.getCursorBuffer().write("\r");
//        console.getCursorBuffer().write(sb.toString());
//        try {
//            console.setCursorPosition(sb.length());
//            console.redrawLine();
//            console.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
