package com.thalesgroup.jenkins.plugins.variablesextractor.extractors;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.kohsuke.stapler.DataBoundConstructor;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;
import com.thalesgroup.jenkins.plugins.variablesextractor.util.ExtractionException;

public class FileContentRegExpExtractor extends Extractor {

    /**********
     * FIELDS *
     **********/
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    private final String file;
    private final String pattern;
    private final boolean ignoreCase;
    private final boolean comments;
    private final boolean multiline;
    private final boolean dotall;

    /***************
     * CONSTRUCTOR *
     ***************/

    @DataBoundConstructor
    public FileContentRegExpExtractor(String file, String pattern, boolean ignoreCase,
            boolean comments, boolean multiline, boolean dotall)
    {
        super();
        this.file = file;
        this.pattern = pattern;
        this.ignoreCase = ignoreCase;
        this.comments = comments;
        this.multiline = multiline;
        this.dotall = dotall;
    }

    /************
     * OVERRIDE *
     ************/

    @Override
    public Map<String, String> extractVariables(FilePath workspace, EnvVars environment)
            throws ExtractionException
    {
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
        String resolvedFile = environment.expand(this.file);
        String pattern = environment.expand(this.pattern);

        FilePath filePath;

        if (new File(resolvedFile).isAbsolute()) {
            filePath = new FilePath(workspace.getChannel(), resolvedFile);
        } else {
            filePath = workspace.child(resolvedFile);
        }

        try {
            String content = filePath.readToString();
            NamedPattern compiledPattern = NamedPattern.compile(pattern, flags);
            NamedMatcher matcher = compiledPattern.matcher(content);

            if (matcher.find()) {
                return matcher.namedGroups();
            } else {
                return new LinkedHashMap<String, String>();
            }
        } catch (IOException e) {
            throw new ExtractionException("Error reading file: " + resolvedFile, e);
        } catch (PatternSyntaxException e) {
            throw new ExtractionException("Invalid regexp pattern: " + e.getMessage(), e);
        }
    }

    public hudson.model.Descriptor<Extractor> getDescriptor() {
        return DESCRIPTOR;
    }

    /***********
     * GETTERS *
     ***********/

    public String getFile() {
        return file;
    }

    public String getPattern() {
        return pattern;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public boolean isComments() {
        return comments;
    }

    public boolean isMultiline() {
        return multiline;
    }

    public boolean isDotall() {
        return dotall;
    }

    /**************
     * DESCRIPTOR *
     **************/

    public static class DescriptorImpl extends Extractor.Descriptor {

        public static final String DEFAULT_PATTERN = "<version>(?P<VERSION>.+?)</version>";
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