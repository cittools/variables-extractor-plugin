package com.thalesgroup.jenkins.plugins.varextractor;

import java.util.Map;

import hudson.EnvVars;
import hudson.model.EnvironmentContributingAction;
import hudson.model.AbstractBuild;

public class VarExtractorEnvAction implements EnvironmentContributingAction {

    private Map<String, String> vars;
    
    public VarExtractorEnvAction(Map<String, String> vars) {
        super();
        this.vars = vars;
    }

    public String getIconFileName() {
        return "document-properties.gif";
    }

    public String getDisplayName() {
        return "Extracted Variables";
    }

    public String getUrlName() {
        return "extractedvariables";
    }

    public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
        env.putAll(this.vars);
    }
}
