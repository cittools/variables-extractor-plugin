package com.thalesgroup.jenkins.plugins.variablesextractor;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;

import java.util.regex.Pattern;

import org.kohsuke.stapler.DataBoundConstructor;

import com.thalesgroup.variablesextractor.FileNameRegExpExtractor;
import com.thalesgroup.variablesextractor.VariableExtractor;

public class FileNameRegExpExtractorDefinition extends ExtractorDefinition {

    private final String file;
    private final String pattern;
    private final String baseDir;
    private final boolean ignoreCase;

    @DataBoundConstructor
    public FileNameRegExpExtractorDefinition(String file, String pattern, String baseDir,
            boolean ignoreCase)
    {
        super();
        this.file = Util.fixEmptyAndTrim(file);
        this.pattern = Util.fixEmptyAndTrim(pattern);
        this.baseDir = Util.fixEmptyAndTrim(baseDir);
        this.ignoreCase = ignoreCase;
    }

    public Descriptor<ExtractorDefinition> getDescriptor() {
        return DESCRIPTOR;
    }

    public String getFile() {
        return file;
    }

    public String getPattern() {
        return pattern;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    @Override
    public VariableExtractor createExtractor(EnvVars environment) {
        String file = environment.expand(this.file);
        String pattern = environment.expand(this.pattern);
        String baseDir = environment.expand(this.baseDir);
        int flags = 0;
        if (ignoreCase) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        return new FileNameRegExpExtractor(file, pattern, baseDir, flags);
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends ExtractorDescriptor {

        public static final String DEFAULT_PATTERN = "(?P<NAME>\\w+)-(?P<VERSION>.+)"
                + "\\.(?P<TIMESTAMP>.+)\\.(?P<FILEEXT>\\w+)";
        public static final String DEFAULT_BASEDIR = ".";
        public static final boolean DEFAULT_IGNORECASE = false;

        @Override
        public String getDisplayName() {
            return "File-Name Regular Expression Extractor";
        }

        public static String getDefaultPattern() {
            return DEFAULT_PATTERN;
        }

        public static String getDefaultBaseDir() {
            return DEFAULT_BASEDIR;
        }

        public static boolean isDefaultIgnoreCase() {
            return DEFAULT_IGNORECASE;
        }

    }

}
