package com.liwenwei.pinyintextview;


import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.widget.TextView;

import java.util.Locale;

/**
 * StringUtils
 * Utils related to {@link String} operations.
 */
public class StringUtils {

    /**
     * logic about making link inside a TextView
     */
    public static void makeLinks(TextView textView, String[] links, ClickableSpan[] clickableSpans) {
        SpannableString spannableString = new SpannableString(textView.getText());
        for (int i = 0; i < links.length; i++) {
            ClickableSpan clickableSpan = clickableSpans[i];
            String link = links[i];

            int startIndexOfLink = textView.getText().toString().indexOf(link);
            spannableString.setSpan(clickableSpan, startIndexOfLink,
                    startIndexOfLink + link.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setHighlightColor(
                Color.TRANSPARENT); // prevent TextView change background when highlight
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spannableString, TextView.BufferType.SPANNABLE);
    }

    public static boolean isSymbol(char ch) {
        if (isCnSymbol(ch)) {
            return true;
        }
        if (isEnSymbol(ch)) {
            return true;
        }

        if (0x2010 <= ch && ch <= 0x2017) {
            return true;
        }
        if (0x2020 <= ch && ch <= 0x2027) {
            return true;
        }
        if (0x2B00 <= ch && ch <= 0x2BFF) {
            return true;
        }
        if (0xFF03 <= ch && ch <= 0xFF06) {
            return true;
        }
        if (0xFF08 <= ch && ch <= 0xFF0B) {
            return true;
        }
        if (ch == 0xFF0D || ch == 0xFF0F) {
            return true;
        }
        if (0xFF1C <= ch && ch <= 0xFF1E) {
            return true;
        }
        if (ch == 0xFF20 || ch == 0xFF65) {
            return true;
        }
        if (0xFF3B <= ch && ch <= 0xFF40) {
            return true;
        }
        if (0xFF5B <= ch && ch <= 0xFF60) {
            return true;
        }
        if (ch == 0xFF62 || ch == 0xFF63) {
            return true;
        }
        if (ch == 0x0020 || ch == 0x3000) {
            return true;
        }
        return false;

    }

    public static boolean isCnSymbol(char ch) {
        if (0x3004 <= ch && ch <= 0x301C) {
            return true;
        }
        if (0x3020 <= ch && ch <= 0x303F) {
            return true;
        }
        return false;
    }

    public static boolean isEnSymbol(char ch) {

        if (ch == 0x40) {
            return true;
        }
        if (ch == 0x2D || ch == 0x2F) {
            return true;
        }
        if (0x23 <= ch && ch <= 0x26) {
            return true;
        }
        if (0x28 <= ch && ch <= 0x2B) {
            return true;
        }
        if (0x3C <= ch && ch <= 0x3E) {
            return true;
        }
        if (0x5B <= ch && ch <= 0x60) {
            return true;
        }
        if (0x7B <= ch && ch <= 0x7E) {
            return true;
        }

        return false;
    }

    public static boolean isPunctuation(char ch) {
        if (isCjkPunc(ch)) {
            return true;
        }
        if (isEnPunc(ch)) {
            return true;
        }

        if (0x2018 <= ch && ch <= 0x201F) {
            return true;
        }
        if (ch == 0xFF01 || ch == 0xFF02) {
            return true;
        }
        if (ch == 0xFF07 || ch == 0xFF0C) {
            return true;
        }
        if (ch == 0xFF1A || ch == 0xFF1B) {
            return true;
        }
        if (ch == 0xFF1F || ch == 0xFF61) {
            return true;
        }
        if (ch == 0xFF0E) {
            return true;
        }
        if (ch == 0xFF65) {
            return true;
        }

        return false;
    }

    public static boolean isEnPunc(char ch) {
        if (0x21 <= ch && ch <= 0x22) {
            return true;
        }
        if (ch == 0x27 || ch == 0x2C) {
            return true;
        }
        if (ch == 0x2E || ch == 0x3A) {
            return true;
        }
        if (ch == 0x3B || ch == 0x3F) {
            return true;
        }

        return false;
    }

    public static boolean isCjkPunc(char ch) {
        if (0x3001 <= ch && ch <= 0x3003) {
            return true;
        }
        if (0x301D <= ch && ch <= 0x301F) {
            return true;
        }

        return false;
    }

    public static String toUpperFirstLetter(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        return str.substring(0, 1).toUpperCase(Locale.getDefault()) + str.substring(1);
    }

    public static boolean isInteger(String s) {
        return isInteger(s, 10);
    }

    public static boolean isInteger(String s, int radix) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) {
                    return false;
                } else {
                    continue;
                }
            }
            if (Character.digit(s.charAt(i), radix) < 0) {
                return false;
            }
        }
        return true;
    }
}
