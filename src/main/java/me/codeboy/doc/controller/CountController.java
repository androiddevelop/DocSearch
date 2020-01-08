package me.codeboy.doc.controller;

import me.codeboy.doc.data.DocSucker;
import me.codeboy.doc.db.helper.DbHelper;
import org.apache.commons.codec.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * 数据统计
 * Created by yuedong.li on 2019-12-21
 */
@Controller
public class CountController {
    private static final Logger logger = LoggerFactory.getLogger(CountController.class);

    @Autowired
    private DbHelper dbHelper;

    @RequestMapping(value = "/redirect")
    public void query(HttpServletResponse response, @RequestParam String targetUrl) {
        try {
            targetUrl = URLDecoder.decode(targetUrl, Charsets.UTF_8.toString());
            dbHelper.addAccessForUrl(targetUrl);
            response.sendRedirect(DocSucker.encodeUrlWithUTf8(targetUrl));
        } catch (IOException e) {
            logger.error("redirect error", e);
        }
    }

}
