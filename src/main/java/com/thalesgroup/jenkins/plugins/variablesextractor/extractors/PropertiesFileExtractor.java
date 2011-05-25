package com.thalesgroup.jenkins.plugins.variablesextractor.extractors;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.kohsuke.stapler.DataBoundConstructor;

import com.thalesgroup.jenkins.plugins.variablesextractor.Logger;
import com.thalesgroup.jenkins.plugins.variablesextractor.util.ExtractionException;

public class PropertiesFileExtractor extends Extractor {

    /**********
     * FIELDS *
     **********/
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    private final String propertiesFile;
    private final String restrictedNames;

    /***************
     * CONSTRUCTOR *
     ***************/

    @DataBoundConstructor
    public PropertiesFileExtractor(String propertiesFile, String restrictedNames) {
        super();
        this.propertiesFile = propertiesFile;
        this.restrictedNames = restrictedNames;
    }

    /************
     * OVERRIDE *
     ************/

    @Override
    public Map<String, String> extractVariables(AbstractBuild<?, ?> build, TaskListener listener)
            throws ExtractionException
    {
        FilePath workspace = build.getWorkspace();
        EnvVars environment;
        try {
            environment = build.getEnvironment(listener);
        } catch (Exception e1) {
            throw new ExtractionException(e1);
        }

        List<String> names = Arrays.asList(restrictedNames.split("\\s*,\\s*"));
        String resolvedPropertiesFile = environment.expand(this.propertiesFile);

        FilePath filePath;
        if (new File(resolvedPropertiesFile).isAbsolute()) {
            filePath = new FilePath(workspace.getChannel(), resolvedPropertiesFile);
        } else {
            filePath = workspace.child(resolvedPropertiesFile);
        }

        try {
            Logger logger = new Logger(listener.getLogger());
            logger.log("Extracting variables from properties file: " + resolvedPropertiesFile);
            Properties properties = new Properties();
            InputStream is = filePath.read();
            properties.load(is);
            is.close();

            Map<String, String> vars = new LinkedHashMap<String, String>();

            for (String propName : properties.stringPropertyNames()) {
                if (names != null && names.size() != 0) {
                    if (names.contains(propName)) {
                        vars.put(propName, properties.getProperty(propName));
                    }
                } else {
                    vars.put(propName, properties.getProperty(propName));
                }
            }

            return vars;

        } catch (IOException e) {
            throw new ExtractionException("Error reading file: " + resolvedPropertiesFile, e);
        }
    }

    public hudson.model.Descriptor<Extractor> getDescriptor() {
        return DESCRIPTOR;
    }

    /***********
     * GETTERS *
     ***********/

    public String getPropertiesFile() {
        return propertiesFile;
    }

    public String getRestrictedNames() {
        return restrictedNames;
    }

    /**************
     * DESCRIPTOR *
     **************/

    public static class DescriptorImpl extends Extractor.Descriptor {

        @Override
        public String getDisplayName() {
            return "Properties File Extractor";
        }

    }
}