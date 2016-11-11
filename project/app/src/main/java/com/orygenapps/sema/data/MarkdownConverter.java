package com.orygenapps.sema.data;

import android.text.Html;
import android.text.Spanned;

import com.commonsware.cwac.anddown.AndDown;

/**
 * Created by starehe on 18/05/15.
 */
public class MarkdownConverter {

    public static CharSequence trimSpanned(CharSequence text) {

        while (text.charAt(text.length() - 1) == '\n') {
            text = text.subSequence(0, text.length() - 1);
        }
        return text;
    }

    public static Spanned markdownToSpanned(String text) {

        // convert the markdown text to HTML
        AndDown markdown = new AndDown();
        String htmlString = markdown.markdownToHtml(text);

        // generate and return the spanned text TODO: remove double newline at end (caused by fromHtml method)
        Spanned spannedText = Html.fromHtml(htmlString.trim());
        return spannedText;

    }

}
