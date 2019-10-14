package com.lcaparros.test.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lcaparros
 */

public class Extractor {

    private Extractor(){}

    public static String getValueFromStringWithLimitsIncluded(String string, String patternStart, String patternStop) {
        Pattern pattern = Pattern.compile(Pattern.quote(patternStart) + ".*?" + Pattern.quote(patternStop));
        Matcher matcher = pattern.matcher(string);
        String values = "";

        while (matcher.find()) {
            values = matcher.group();
        }
        return values;
    }

    public static String getValueFromStringWithLimits(String string, String patternStart, String patternStop) {
        String values = getValueFromStringWithLimitsIncluded(string, patternStart, patternStop);
        return values.replace(patternStart, "").replace(patternStop, "");
    }
}
