package com.jobseeker.source;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/** Third-party descriptions are untrusted HTML: keep basic formatting, drop scripts, styles and images. */
public final class HtmlSanitizer {

    private static final Safelist SAFELIST = Safelist.basic()
            .addTags("h1", "h2", "h3", "h4", "h5", "h6")
            .addEnforcedAttribute("a", "target", "_blank")
            .addEnforcedAttribute("a", "rel", "nofollow noopener noreferrer");

    private HtmlSanitizer() {}

    public static String clean(String html) {
        return html == null ? "" : Jsoup.clean(html, SAFELIST);
    }

    public static String toText(String html) {
        return html == null ? "" : Jsoup.parse(html).text();
    }
}
