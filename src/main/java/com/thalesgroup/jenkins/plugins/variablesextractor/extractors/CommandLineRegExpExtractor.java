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
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.Executor;
import hudson.util.ArgumentListBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.kohsuke.stapler.DataBoundConstructor;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;
import com.thalesgroup.jenkins.plugins.variablesextractor.Logger;
import com.thalesgroup.jenkins.plugins.variablesextractor.util.ExtractionException;

public class CommandLineRegExpExtractor extends Extractor {

    private static final long serialVersionUID = -3590627915663936671L;

    /**********
     * FIELDS *
     **********/
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    private final String command;
    private final String pattern;
    private final String workdir;
    private final boolean ignoreCase;
    private final boolean comments;
    private final boolean multiline;
    private final boolean dotall;

    /***************
     * CONSTRUCTOR *
     ***************/

    @DataBoundConstructor
    public CommandLineRegExpExtractor(String command, String pattern, String workdir,
            boolean ignoreCase, boolean comments, boolean multiline, boolean dotall)
    {
        super();
        this.command = command;
        this.pattern = pattern;
        this.workdir = Util.fixEmptyAndTrim(workdir);
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
        if (ignoreCase) flags |= Pattern.CASE_INSENSITIVE;
        if (comments) flags |= Pattern.COMMENTS;
        if (multiline) flags |= Pattern.MULTILINE;
        if (dotall) flags |= Pattern.DOTALL;
        
        String resolvedCommand = environment.expand(this.command);
        String resolvedPattern = environment.expand(this.pattern);
        String resolvedWorkdir = environment.expand(this.workdir);

        FilePath filePath;
        if (resolvedWorkdir == null) {
            filePath = workspace;
        } else if (new File(resolvedWorkdir).isAbsolute()) {
            filePath = new FilePath(workspace.getChannel(), resolvedWorkdir);
        } else {
            filePath = workspace.child(resolvedWorkdir);
        }
        
        Launcher launcher = Executor.currentExecutor().getOwner().getNode()
                .createLauncher(TaskListener.NULL);

        ArgumentListBuilder args = new ArgumentListBuilder();
        args.addTokenized(resolvedCommand);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ProcStarter starter = launcher.launch();
        starter.cmds(args);
        starter.envs(environment);
        starter.stdout(outStream);
        starter.pwd(filePath);

        try {
            Logger logger = new Logger(listener.getLogger());
            int code = launcher.launch(starter).join();
            String commandOutput = outStream.toString();
            logger.log("Extracting variables from command output: " + resolvedCommand);
            if (resolvedWorkdir != null) {
                logger.log("Executed from working directory: " + resolvedWorkdir);
            }
            listener.getLogger().println(commandOutput);
            if (code != 0) {
                throw new RuntimeException("Error during command execution");
            }
            NamedPattern compiledPattern = NamedPattern.compile(resolvedPattern, flags);
            NamedMatcher matcher = compiledPattern.matcher(commandOutput);

            if (matcher.find()) {
                return matcher.namedGroups();
            } else {
                logger.log("<WARNING> No match in command output for pattern: " + resolvedPattern);
                return new LinkedHashMap<String, String>();
            }
        } catch (Exception e) {
            throw new ExtractionException(e);
        }
    }

    public hudson.model.Descriptor<Extractor> getDescriptor() {
        return DESCRIPTOR;
    }

    /***********
     * GETTERS *
     ***********/

    public String getCommand() {
        return command;
    }

    public String getPattern() {
        return pattern;
    }

    public String getWorkdir() {
		return workdir;
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

        private static final long serialVersionUID = 7281953615989321037L;
        public static final boolean DEFAULT_IGNORE_CASE = false;
        public static final boolean DEFAULT_COMMENTS = true;
        public static final boolean DEFAULT_MULTILINE = false;
        public static final boolean DEFAULT_DOTALL = true;

        @Override
        public String getDisplayName() {
            return "Command Line Regular Expression Extractor";
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
