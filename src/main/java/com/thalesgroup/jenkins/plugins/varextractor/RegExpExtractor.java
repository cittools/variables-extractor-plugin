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


import hudson.Util;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.selectors.SelectorUtils;

import com.thalesgroup.jenkins.plugins.varextractor.regexp.NamedMatcher;
import com.thalesgroup.jenkins.plugins.varextractor.regexp.NamedPattern;

public class RegExpExtractor {

    private String file;
    private String pattern;
    private String baseDir;
    
    public RegExpExtractor(String file, String pattern, String baseDir) {
        super();
        this.file = file;
        this.pattern = pattern;
        this.baseDir = baseDir;
    }
    
    
    public Map<String, String> extractVariables() throws IOException {
        Map<String, String> vars = new LinkedHashMap<String, String>();
        NamedMatcher m = NamedPattern.compile(pattern).matcher(getExpandedFileName());
        if (m.find()) {
            vars = m.namedGroups();
        }
        return vars;
    }
    
    
    
    private String getExpandedFileName() throws IOException {
        DirectoryScanner ds = new DirectoryScanner();
        String baseDir = guessBaseDirFromFile();
        String file = this.file;
        if (baseDir == null) {
            baseDir = this.baseDir;
        }
        if (file.startsWith(baseDir)) {
            file = file.substring(baseDir.length());
            while (file.startsWith(File.separator)) {
                file = file.substring(1);
            }
        }
        ds.setBasedir(baseDir);
        ds.setIncludes(new String[]{ file });
        ds.scan();
        String[] files = ds.getIncludedFiles();
        if (files.length == 1) {
            File f = new File(files[0]);
            return f.getName();
        } else if (files.length == 0) {
            throw new IOException("No file matches with the given pattern: "+file);
        } else {
            throw new IOException("Multiple files match with the given pattern: "+file);
        }
    }
    
    private String guessBaseDirFromFile() {
        String nonWildcardBase = SelectorUtils.rtrimWildcardTokens(this.file);
        return Util.fixEmpty(nonWildcardBase);
    }
    
    public String getFile() {
        return file;
    }
    public String getPattern() {
        return pattern;
    }
    
    
    
    
}
