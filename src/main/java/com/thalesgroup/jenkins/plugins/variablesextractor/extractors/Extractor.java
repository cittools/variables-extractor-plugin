package com.thalesgroup.jenkins.plugins.variablesextractor.extractors;

import hudson.model.Describable;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;

import java.io.Serializable;
import java.util.Map;

import com.thalesgroup.jenkins.plugins.variablesextractor.util.ExtractionException;

public abstract class Extractor implements Describable<Extractor>, Serializable {

    private static final long serialVersionUID = 192216864201851260L;

    public abstract Map<String, String> extractVariables(AbstractBuild<?, ?> build,
            TaskListener listener) throws ExtractionException;

    public static abstract class Descriptor extends hudson.model.Descriptor<Extractor> implements
            Serializable
    {

        private static final long serialVersionUID = -5274378027362433086L;

    }
}
