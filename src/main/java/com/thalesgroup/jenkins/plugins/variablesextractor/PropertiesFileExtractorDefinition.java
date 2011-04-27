package com.thalesgroup.jenkins.plugins.variablesextractor;

import hudson.Extension;
import hudson.model.Descriptor;

import java.util.Arrays;

import org.kohsuke.stapler.DataBoundConstructor;

import com.thalesgroup.variablesextractor.PropertiesFileExtractor;
import com.thalesgroup.variablesextractor.VariableExtractor;

public class PropertiesFileExtractorDefinition extends ExtractorDefinition {

    private final String propertiesFile;
    private final String restrictedNames;

    @DataBoundConstructor  
    public PropertiesFileExtractorDefinition(String propertiesFile, String restrictedNames) {
        super();
        this.propertiesFile = propertiesFile;
        this.restrictedNames = restrictedNames;
    }

    public Descriptor<ExtractorDefinition> getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    public VariableExtractor createExtractor() {
        String[] names = restrictedNames.split("\\s*,\\s*");
        return new PropertiesFileExtractor(propertiesFile, Arrays.asList(names)); 
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends ExtractorDescriptor {

        @Override
        public String getDisplayName() {
            return "Properties File Extractor";
        }

    }

}