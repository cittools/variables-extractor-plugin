package com.thalesgroup.jenkins.plugins.varextractor;

import com.thalesgroup.jenkins.plugins.varextractor.regexp.NamedMatcher;
import com.thalesgroup.jenkins.plugins.varextractor.regexp.NamedPattern;

public class Toto {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String regex = "(?P<NAME>\\w+)-(?P<VERSION>.+)\\.(?P<TIMESTAMP>.+)\\.(?P<FILEEXT>\\w+)";
        NamedPattern p = NamedPattern.compile(regex);
        
        NamedMatcher m = p.matcher("Toto-5.4.2.56115651.zip");
        
        if (m.find()) {
            System.out.println(m.namedGroups());
        }
        
    }

}
