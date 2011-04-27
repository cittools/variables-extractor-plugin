package com.thalesgroup.jenkins.plugins.variablesextractor;

import hudson.Extension;
import hudson.model.Descriptor;

import java.util.regex.Pattern;

import org.kohsuke.stapler.DataBoundConstructor;

import com.thalesgroup.variablesextractor.FileContentRegExpExtractor;
import com.thalesgroup.variablesextractor.VariableExtractor;

public class FileContentRegExpExtractorDefinition extends ExtractorDefinition {

    private final String file;
    private final String pattern;
    private final boolean ignoreCase;
    private final boolean comments;
    private final boolean multiline;
    private final boolean dotall;

    @DataBoundConstructor
    public FileContentRegExpExtractorDefinition(String file, String pattern,
            boolean ignoreCase, boolean comments, boolean multiline, boolean dotall)
    {
        super();
        this.file = file;
        this.pattern = pattern;
        this.ignoreCase = ignoreCase;
        this.comments = comments;
        this.multiline = multiline;
        this.dotall = dotall;
    }



    public Descriptor<ExtractorDefinition> getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    public VariableExtractor createExtractor() {
        int flags = 0;
        if (ignoreCase) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        if (comments) {
            flags |= Pattern.COMMENTS;
        }
        if (multiline) {
            flags |= Pattern.MULTILINE;
        }
        if (dotall) {
            flags |= Pattern.DOTALL;
        }
        return new FileContentRegExpExtractor(file, pattern, flags);
    }

    
    
    
    public String getPattern() {
        if (pattern == null) {
            return DescriptorImpl.DEFAULT_PATTERN;
        } else {
            return pattern;
        }
    }




    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends ExtractorDescriptor {

        public static final String DEFAULT_PATTERN = "<version>(?P<VERSION>.+)</version>";
        public static final boolean DEFAULT_IGNORE_CASE = false;
        public static final boolean DEFAULT_COMMENTS = true;
        public static final boolean DEFAULT_MULTILINE = false;
        public static final boolean DEFAULT_DOTALL = true;
        
        @Override
        public String getDisplayName() {
            return "File Content Regular Expression Extractor";
        }

        public String getDefaultPattern() {
            return DEFAULT_PATTERN;
        }

        public boolean isDefaultIgnoreCase() {
            return DEFAULT_IGNORE_CASE;
        }

        public boolean isDefaultComments() {
            return DEFAULT_COMMENTS;
        }

        public boolean isDefaultMultiline() {
            return DEFAULT_MULTILINE;
        }

        public boolean isDefaultDotall() {
            return DEFAULT_DOTALL;
        }

    }

}