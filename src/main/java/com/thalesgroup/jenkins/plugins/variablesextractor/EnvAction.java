package com.thalesgroup.jenkins.plugins.variablesextractor;

import java.util.Map;

import org.kohsuke.stapler.StaplerProxy;

import hudson.EnvVars;
import hudson.model.EnvironmentContributingAction;
import hudson.model.AbstractBuild;

public class EnvAction implements EnvironmentContributingAction, StaplerProxy {

    private Map<String, String> vars;
    
    public EnvAction(Map<String, String> vars) {
        super();
        this.vars = vars;
    }

    public String getIconFileName() {
        return "/plugin/variables-extractor-plugin/icons/extractor.png";
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

    public Object getTarget() {
        return new ExtractedVariablesList(vars);
    }
}
