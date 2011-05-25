package com.thalesgroup.jenkins.plugins.variablesextractor.extractors;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.kohsuke.stapler.DataBoundConstructor;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;
import com.thalesgroup.jenkins.plugins.variablesextractor.Logger;
import com.thalesgroup.jenkins.plugins.variablesextractor.util.ExtractionException;
import com.thalesgroup.jenkins.plugins.variablesextractor.util.MultipleFilesMatchedException;

public class FileNameRegExpExtractor extends Extractor {

    /**********
     * FIELDS *
     **********/
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    private final String file;
    private final String pattern;
    private final String baseDir;
    private final boolean ignoreCase;

    /***************
     * CONSTRUCTOR *
     ***************/

    @DataBoundConstructor
    public FileNameRegExpExtractor(String file, String pattern, String baseDir, boolean ignoreCase)
    {
        super();
        this.file = Util.fixEmptyAndTrim(file);
        this.pattern = Util.fixEmptyAndTrim(pattern);
        this.baseDir = Util.fixEmptyAndTrim(baseDir);
        this.ignoreCase = ignoreCase;
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

        String resolvedFile = environment.expand(this.file);
        String resolvedPattern = environment.expand(this.pattern);
        String resolvedBaseDir = environment.expand(this.baseDir);
        int flags = 0;
        if (ignoreCase) {
            flags |= Pattern.CASE_INSENSITIVE;
        }

        FilePath path;
        if (resolvedBaseDir != null) {
            if (new File(resolvedBaseDir).isAbsolute()) {
                path = new FilePath(workspace.getChannel(), resolvedBaseDir);
            } else {
                path = workspace.child(resolvedBaseDir);
            }
        } else {
            path = workspace;
        }

        try {
            Logger logger = new Logger(listener.getLogger());
            logger.log("Extracting variables from file name: " + resolvedFile);
            String filename = getExpandedFileName(path, resolvedFile);
            NamedPattern compiledPattern = NamedPattern.compile(resolvedPattern, flags);
            NamedMatcher matcher = compiledPattern.matcher(filename);

            if (matcher.find()) {
                return matcher.namedGroups();
            } else {
                return new LinkedHashMap<String, String>();
            }
        } catch (FileNotFoundException e) {
            throw new ExtractionException("File not found: " + e.getMessage());
        } catch (MultipleFilesMatchedException e) {
            throw new ExtractionException("Multiple files match with the given pattern: "
                    + e.getMessage());
        } catch (PatternSyntaxException e) {
            throw new ExtractionException("Invalid regexp pattern: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ExtractionException(e);
        }
    }

    public hudson.model.Descriptor<Extractor> getDescriptor() {
        return DESCRIPTOR;
    }

    /***********
     * PRIVATE *
     ***********/

    private String getExpandedFileName(FilePath dir, String fileName) throws IOException,
            InterruptedException
    {
        FilePath[] matchingFiles = dir.list(fileName);
        if (matchingFiles.length == 1) {
            return matchingFiles[0].getName();
        } else if (matchingFiles.length == 0) {
            throw new FileNotFoundException(dir.getRemote() + "/" + fileName);
        } else {
            throw new MultipleFilesMatchedException(dir.getRemote() + "/" + fileName);
        }
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

    public String getBaseDir() {
        return baseDir;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    /**************
     * DESCRIPTOR *
     **************/

    public static class DescriptorImpl extends Extractor.Descriptor {

        public static final String DEFAULT_PATTERN = "(?P<NAME>\\w+)-(?P<VERSION>.+)"
                + "\\.(?P<TIMESTAMP>.+)\\.(?P<FILEEXT>\\w+)";
        public static final String DEFAULT_BASEDIR = "";
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
