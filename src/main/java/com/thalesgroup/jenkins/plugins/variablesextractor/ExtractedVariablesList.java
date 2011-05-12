package com.thalesgroup.jenkins.plugins.variablesextractor;

import hudson.model.Api;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class ExtractedVariablesList implements Serializable {

    private static final long serialVersionUID = 689591903988355965L;

    private Map<String, String> variables;
    
    public Set<Entry<String,String>> getVariables() {
        return variables.entrySet();
    }

    public ExtractedVariablesList(Map<String, String> vars) {
        super();
        this.variables = vars;
    }

    public Object getDynamic(final String link, final StaplerRequest request,
            final StaplerResponse response) throws IOException
    {
        response.sendRedirect2("index");
        return null;
    }

    public Api getApi() {
        return new Api(this);
    }

}
