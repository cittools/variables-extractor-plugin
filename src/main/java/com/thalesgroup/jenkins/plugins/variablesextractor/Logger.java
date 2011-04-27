package com.thalesgroup.jenkins.plugins.variablesextractor;

import java.io.PrintStream;

public class Logger {
    
    private PrintStream stream;

    public Logger(PrintStream stream) {
        this.stream = stream;
    }
    
    public void log(Object obj) {
        stream.println("[variables-extractor] " + obj.toString());
    }
    
}
