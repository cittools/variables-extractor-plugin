/************************************************************************************
 * Copyright (c) 2004-2011,  Thales Corporate Services SAS                          *
 * Author: Robin Jarry                                                              *
 *                                                                                  *
 * The MIT License                                                                  *
 *                                                                                  *
 * Permission is hereby granted, free of charge, to any person obtaining a copy     *
 * of this software and associated documentation files (the "Software"), to deal    *
 * in the Software without restriction, including without limitation the rights     *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell        *
 * copies of the Software, and to permit persons to whom the Software is            *
 * furnished to do so, subject to the following conditions:                         *
 *                                                                                  *
 * The above copyright notice and this permission notice shall be included in       *
 * all copies or substantial portions of the Software.                              *
 *                                                                                  *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR       *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,         *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE      *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER           *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,    *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN        *
 * THE SOFTWARE.                                                                    *
 ************************************************************************************/
package com.thalesgroup.jenkins.plugins.variablesextractor.extractors;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.kohsuke.stapler.DataBoundConstructor;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;
import com.thalesgroup.jenkins.plugins.variablesextractor.Logger;
import com.thalesgroup.jenkins.plugins.variablesextractor.util.ExtractionException;

public class FileContentRegExpExtractor extends Extractor {

    private static final long serialVersionUID = 6923905637559850482L;

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
        String resolvedPattern = environment.expand(this.pattern);

        FilePath filePath;

        if (new File(resolvedFile).isAbsolute()) {
            filePath = new FilePath(workspace.getChannel(), resolvedFile);
        } else {
            filePath = workspace.child(resolvedFile);
        }

        try {
            Logger logger = new Logger(listener.getLogger());
            logger.log("Extracting variables from file content: " + resolvedFile);
            
            String content = filePath.act(new FileCallable<String>() {
                private static final long serialVersionUID = 2354823830107440576L;

                public String invoke(File f, VirtualChannel channel) throws IOException,
                        InterruptedException
                {
                    final char[] buffer = new char[4096];
                    int bufferLength = 0;
                    StringBuffer textBuffer = new StringBuffer();
                    Reader reader = new FileReader(f);
                    while (bufferLength != -1) {
                        bufferLength = reader.read(buffer);
                        if (bufferLength > 0) {
                            textBuffer.append(new String(buffer, 0, bufferLength));
                        }
                    }
                    reader.close();
                    return textBuffer.toString();
                }
            });
            
            NamedPattern compiledPattern = NamedPattern.compile(resolvedPattern, flags);
            NamedMatcher matcher = compiledPattern.matcher(content);

            if (matcher.find()) {
                return matcher.namedGroups();
            } else {
                logger.log("<WARNING> No match in file content for pattern: " + resolvedPattern);
                return new LinkedHashMap<String, String>();
            }
        } catch (InterruptedException e) {
            throw new ExtractionException("Error reading file: " + resolvedFile, e);
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

        private static final long serialVersionUID = -8256041431702583829L;
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