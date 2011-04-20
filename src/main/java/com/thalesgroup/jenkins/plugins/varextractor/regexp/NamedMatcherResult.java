package com.thalesgroup.jenkins.plugins.varextractor.regexp;

import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;

public interface NamedMatcherResult extends MatchResult {

    List<String> orderedGroups();

    Map<String, String> namedGroups();

    String group(String groupName);

    int start(String groupName);

    int end(String groupName);
}
