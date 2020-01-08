package me.codeboy.doc.data;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import me.codeboy.common.base.net.CBHttps;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * doc sucker
 * Created by yuedong.li on 2019/12/21
 */
public class DocSucker {

    public static Map<String, String> getDocLinks(String entryUrl) throws IOException {
        Map<String, String> docLinks = Maps.newLinkedHashMap();
        String html = CBHttps.getInstance().connect(entryUrl).execute();
        Elements elements = Jsoup.parse(html).select(".book-summary").select(".summary").select("a");
        int splitStart = entryUrl.indexOf('/');
        int splitEnd = entryUrl.lastIndexOf('/');
        String rootDomain = entryUrl.substring(0, splitStart);
        String rootDomainWithPath = entryUrl.substring(0, splitEnd + 1);
        for (Element element : elements) {
            String url = element.attr("href");
            if (url == null || url.contains(".gitbook.com")) {
                continue;
            }
            String title = element.text();
            String order = element.select("b").text();
            title = title.replace(order, "").trim();
            if (url.startsWith("/")) {
                url = rootDomain + url;
            } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = rootDomainWithPath + url;
            }
            if (url.endsWith("/")) {
                url = url + "index.html";
            }
            docLinks.put(url.replace("/./", "/"), title);
        }
        return docLinks;
    }

    public static String getDocContent(String url) throws IOException {
        String html = CBHttps.getInstance().connect(encodeUrlWithUTf8(url)).execute();
        Elements elements = Jsoup.parse(html).select(".book-body").select(".page-inner").select(".search-noresults");
        Elements foot = elements.select(".page-footer");
        return elements.text().replace(foot.text(), "").trim();
    }

    // 针对有中文的进行转义
    public static String encodeUrlWithUTf8(String url) throws UnsupportedEncodingException {
        if (url == null) {
            return "";
        }

        int protocolStart = url.indexOf("://");
        String protocol = url.substring(0, protocolStart + 3);
        String tmp = url.substring(protocolStart + 3);
        int start = tmp.indexOf("/");
        int end = tmp.lastIndexOf(".");
        if (start != -1 && end != -1 && start < end) {
            url = protocol + tmp.substring(0, start)
                    + URLEncoder.encode(tmp.substring(start, end).replaceAll("\\/", "___"), Charsets.UTF_8.toString()).replaceAll("___", "/")
                    + tmp.substring(end);
        }
        return url;
    }
}
