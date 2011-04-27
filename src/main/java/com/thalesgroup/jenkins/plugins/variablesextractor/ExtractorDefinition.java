package com.thalesgroup.jenkins.plugins.variablesextractor;

import hudson.model.Describable;

import com.thalesgroup.variablesextractor.VariableExtractor;

public abstract class ExtractorDefinition implements Describable<ExtractorDefinition> {

    
    public abstract VariableExtractor createExtractor();
    
    
}
