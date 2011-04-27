package com.thalesgroup.jenkins.plugins.variablesextractor;

import java.util.Map;

import hudson.EnvVars;
import hudson.model.EnvironmentContributingAction;
import hudson.model.AbstractBuild;

public class EnvAction implements EnvironmentContributingAction {

    private Map<String, String> vars;
    
    public EnvAction(Map<String, String> vars) {
        super();
        this.vars = vars;
    }

    public String getIconFileName() {
        return "/plugin/variables-extractor-plugin/icons/exctractor.png";
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
