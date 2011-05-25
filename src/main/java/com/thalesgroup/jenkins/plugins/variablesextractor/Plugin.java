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
package com.thalesgroup.jenkins.plugins.variablesextractor;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.thalesgroup.jenkins.plugins.variablesextractor.extractors.Extractor;

public class Plugin extends BuildWrapper {

    private final List<Extractor> extractors;

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    @DataBoundConstructor
    public Plugin(List<Extractor> extractors) {
        this.extractors = extractors;
    }

    public Plugin(Extractor... extractors) {
        this.extractors = Arrays.asList(extractors);
    }

    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    public Environment setUp(@SuppressWarnings("rawtypes") AbstractBuild build, Launcher launcher,
            BuildListener listener) throws IOException, InterruptedException
    {
        Logger logger = new Logger(listener.getLogger());
        Map<String, String> vars = new HashMap<String, String>();

        for (Extractor extractor : this.extractors) {
            vars.putAll(extractor.extractVariables(build, listener));
        }

        if (vars.size() > 0) {
            EnvAction action = new EnvAction(vars);
            build.addAction(action);
        }
        
        logger.log("Extracted variables:");
        for (Entry<String, String> entry : vars.entrySet()) {
            logger.log(entry.getKey() + " = " + entry.getValue());
        }
        
        return new Environment() {};
    }

    public List<Extractor> getExtractors() {
        return extractors;
    }

    public static class DescriptorImpl extends BuildWrapperDescriptor {

        public DescriptorImpl() {
            super(Plugin.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return "Variables Extractor";
        }

        @Override
        public BuildWrapper newInstance(StaplerRequest req, JSONObject formData)
                throws FormException
        {
            List<Extractor> extractors = new ArrayList<Extractor>();
            if (formData.containsKey("extractorDefinitions")) {
                try {
                    JSONObject jsonObj = formData.getJSONObject("extractorDefinitions");
                    Extractor def = req.bindJSON(Extractor.class, jsonObj);
                    extractors.add(def);
                } catch (JSONException e) {
                    JSONArray array = formData.getJSONArray("extractorDefinitions");
                    extractors.addAll(req.bindJSONToList(Extractor.class, array));
                }
            }
            return new Plugin(extractors);
        }

        public List<Extractor.Descriptor> getExtractorDescriptors() {
            return Hudson.getInstance().getDescriptorList(Extractor.class);
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

    }

}
