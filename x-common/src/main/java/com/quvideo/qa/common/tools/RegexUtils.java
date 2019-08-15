package com.quvideo.qa.common.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {
    public static String getMatcher(String source, String regex) {
        String result = "";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

}
