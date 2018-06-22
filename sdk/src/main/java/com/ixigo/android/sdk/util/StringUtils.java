package com.ixigo.android.sdk.util;

import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class StringUtils {

    public static String joinNotEmpty(List<String> list, String sep) {
        if (list.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (String str : list) {
            if (isNotEmpty(str)) {
                result.append(sep).append(str);
            }
        }
        return result.deleteCharAt(0).toString();
    }

    public static boolean isEmpty(String string) {
        return (string == null || "".equals(string));
    }

    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(String str) {
        return !StringUtils.isBlank(str);
    }

    /**
     * Compress whitespace.
     *
     * @param s String to compress
     * @return string with leading and trailing whitespace removed, and internal runs of whitespace replaced by a single space character
     */
    public static String compressWhitespace(String s) {
        StringBuffer output = new StringBuffer();
        int p = 0;
        boolean inSpace = true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (Character.isWhitespace(s.charAt(i))) {
                if (!inSpace) {
                    output.append(s.substring(p, i));
                    output.append(' ');
                    inSpace = true;
                }
            } else {
                if (inSpace) {
                    p = i;
                    inSpace = false;
                }
            }
        }
        if (!inSpace)
            output.append(s.substring(p));
        return output.toString();
    }

    /**
     * This method will taken in a string in any capitalization and capitalize all words.
     *
     * @param str
     * @param sep the sepeartor separting the words
     * @return
     */
    public static String capitalizeString(String str, String sep) {

        if (!isEmpty(str.trim())) {
            StringTokenizer st = new StringTokenizer(str, sep, false);
            StringBuilder output = new StringBuilder();
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                output.append(capitalizeWord(token));
                if (st.hasMoreTokens())
                    output.append(sep);
            }
            return output.toString();
        } else {
            // "String can't be capitalized."
            return str;
        }
    }

    /**
     * This method will take in a word and format it with first letter as uppercase and rest all to lowercase.
     *
     * @param str
     * @return
     */
    public static String capitalizeWord(String str) {
        if (!isEmpty(str.trim())) {
            if (str.length() >= 2) {
                StringBuilder output = new StringBuilder();
                output.append(Character.toUpperCase(str.charAt(0)));
                output.append(str.substring(1).toLowerCase(Locale.ENGLISH));
                return output.toString();
            } else {
                return str.toUpperCase(Locale.ENGLISH);
            }
        } else {
            // "Word can't be capitalized.
            return str;
        }
    }

    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        int sz = str.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(str.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static String toString(CharSequence sequence) {
        if (sequence == null) {
            return null;
        }
        return sequence.toString();
    }

    public static String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

    public static String substringAfterLast(String str, String separator) {
        if (isEmpty(str)) {
            return str;
        }
        if (isEmpty(separator)) {
            return "";
        }
        int pos = str.lastIndexOf(separator);
        if (pos == -1 || pos == (str.length() - separator.length())) {
            return "";
        }
        return str.substring(pos + separator.length());
    }

    public static String extractDigits(String str) {
        if (str == null) {
            return null;
        }
        char[] charArray = str.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char c : charArray) {
            if (Character.isDigit(c)) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    /**
     * <p>Deletes all whitespaces from a String as defined by
     * {@link Character#isWhitespace(char)}.</p>
     * <p>
     * <pre>
     * StringUtils.deleteWhitespace(null)         = null
     * StringUtils.deleteWhitespace("")           = ""
     * StringUtils.deleteWhitespace("abc")        = "abc"
     * StringUtils.deleteWhitespace("   ab  c  ") = "abc"
     * </pre>
     *
     * @param str the String to delete whitespace from, may be null
     * @return the String without whitespaces, {@code null} if null String input
     */
    public static String deleteWhitespace(final String str) {
        if (isEmpty(str)) {
            return str;
        }
        final int sz = str.length();
        final char[] chs = new char[sz];
        int count = 0;
        for (int i = 0; i < sz; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                chs[count++] = str.charAt(i);
            }
        }
        if (count == sz) {
            return str;
        }
        return new String(chs, 0, count);
    }

    public static boolean isNumericIgnoringWhitespace(String str) {
        return isNumeric(deleteWhitespace(str));
    }

}
