package com.thalesgroup.jenkins.plugins.varextractor;

import java.io.PrintStream;

public class VarExtractorLogger {
    
    private PrintStream stream;

    public VarExtractorLogger(PrintStream stream) {
        this.stream = stream;
    }
    
    public void log(Object obj) {
        stream.println("[variableextractor] " + obj);
    }
    
}
