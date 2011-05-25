package com.thalesgroup.jenkins.plugins.variablesextractor.extractors;

import hudson.model.Describable;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;

import java.util.Map;

import com.thalesgroup.jenkins.plugins.variablesextractor.util.ExtractionException;

public abstract class Extractor implements Describable<Extractor> {

    public abstract Map<String, String> extractVariables(AbstractBuild<?, ?> build,
            TaskListener listener) throws ExtractionException;

    public static abstract class Descriptor extends hudson.model.Descriptor<Extractor> {

    }
}
