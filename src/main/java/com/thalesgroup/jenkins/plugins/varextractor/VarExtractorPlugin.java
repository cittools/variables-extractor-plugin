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
package com.thalesgroup.jenkins.plugins.varextractor;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.kohsuke.stapler.DataBoundConstructor;

public class VarExtractorPlugin extends BuildWrapper {

	private String file;
	private String pattern;

	@Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
	
	@DataBoundConstructor
	public VarExtractorPlugin(String file, String pattern) {
        super();
        this.file = file;
        this.pattern = pattern;
    }
	
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    public String getPattern() {
        return pattern;
    }

    public String getFile() {
        return file;
    }
    
    
	@Override
    public Environment setUp(@SuppressWarnings("rawtypes") AbstractBuild build, 
            Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException
    {
	    try {
	        VarExtractorLogger logger = new VarExtractorLogger(listener.getLogger());
	        
	        RegExpExtractor ext = new RegExpExtractor(file, pattern, build.getWorkspace().getRemote());
	        Map<String, String> vars = ext.extractVariables();
	        VarExtractorEnvAction action = new VarExtractorEnvAction(vars);
	        build.addAction(action);
	        logger.log("Extracted variables:");
	        for (Entry<String, String> entry : vars.entrySet()) {
	            logger.log(entry.getKey() + " = " + entry.getValue());
	        }
	    } catch (IOException e) {
	        e.printStackTrace(listener.getLogger());
	        build.setResult(Result.FAILURE);
	    }
        return new Environment(){};
    }

    public static class DescriptorImpl extends BuildWrapperDescriptor {

	    public static final String DEFAULT_PATTERN = "(?P<NAME>\\w+)-(?P<VERSION>.+)\\.(?P<TIMESTAMP>.+)\\.(?P<FILEEXT>\\w+)";
	    
        public DescriptorImpl() {
            super(VarExtractorPlugin.class);
            load();
        }
	    
	    @Override
	    public String getDisplayName() {
	        return "Variables Extractor";
	    }

//        @Override
//        public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData)
//                throws FormException
//        {
//            VarExtractorPlugin instance = null;
//            try {
//                JSONObject varextractor = formData.getJSONObject("varextractor");
//                String file = Util.fixEmptyAndTrim(varextractor.getString("file"));
//                String pattern = Util.fixEmptyAndTrim(varextractor.getString("pattern"));
//                if (file != null && pattern != null)
//                    instance = new VarExtractorPlugin(file, pattern);
//            } catch (JSONException e) {
//                throw new FormException(e, "Invalid JSON data");
//            }
//            return instance;
//        }
	    
	    public String getDefaultPattern() {
	        return DEFAULT_PATTERN;
	    }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }
	    
	}

}
