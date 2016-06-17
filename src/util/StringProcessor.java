package util;

import java.io.UnsupportedEncodingException;

public class StringProcessor {

    public static String encodeStringToUtf8(String origanalString) {
        try {
            return new String(origanalString.getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
