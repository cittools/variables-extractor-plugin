package com.thalesgroup.jenkins.plugins.variablesextractor.extractors;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Describable;

import java.util.Map;

import com.thalesgroup.jenkins.plugins.variablesextractor.util.ExtractionException;


public abstract class Extractor implements Describable<Extractor> {

    
    public abstract Map<String, String> extractVariables(FilePath workspace, EnvVars environment)  throws ExtractionException;
    
    public static abstract class Descriptor extends hudson.model.Descriptor<Extractor> {

    }
}
