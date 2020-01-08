package me.codeboy.doc.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.codeboy.doc.base.CommonResult;
import me.codeboy.doc.db.domain.Doc;
import me.codeboy.doc.db.domain.DocInfo;
import me.codeboy.doc.db.helper.DbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * 数据查询
 * Created by yuedong.li on 2019-12-21
 */
@RestController
public class QueryController {
    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);

    @Autowired
    private DbHelper dbHelper;

    @Autowired
    private MaintainController maintainController;

    @RequestMapping(value = "/api/query")
    public CommonResult query(@RequestParam String keyword) {
        try {
            keyword = URLDecoder.decode(keyword, Charsets.UTF_8.toString());
            if (keyword.endsWith("__update")) {
                maintainController.updateDoc();
                return CommonResult.failed("文档已经开始更新");
            }
            logger.info("query es data {}", keyword);
            List<DocInfo> docInfos = dbHelper.queryEsDocs(keyword);
            // 如果发现有和标题一样的文档，优先展示
            for (DocInfo docInfo : docInfos) {
                if (docInfo.getTitle().endsWith(keyword)) {
                    docInfos.remove(docInfo);
                    docInfos.add(0, docInfo);
                    break;
                }
            }
            for (DocInfo docInfo : docInfos) {
                docInfo.setUrl("/redirect?targetUrl=" + URLEncoder.encode(docInfo.getUrl(), Charsets.UTF_8.toString()));
            }
            return CommonResult.success(docInfos);
        } catch (UnsupportedEncodingException e) {
            logger.info("query error", e);
            return CommonResult.failed("请检查输入");
        }
    }

    @RequestMapping(value = "/api/suggest")
    public String suggest(@RequestParam String keyword) {
        try {
            keyword = URLDecoder.decode(keyword, Charsets.UTF_8.toString());
            List<Doc> docList = dbHelper.querySuggestTitle(keyword);
            List<Map<String, Object>> wordResult = Lists.newArrayList();

            List<String> titles = Lists.newArrayList();
            for (Doc doc : docList) {
                if (titles.contains(doc.getTitle())) {
                    continue;
                }
                titles.add(doc.getTitle());
                Map<String, Object> map = Maps.newHashMap();
                map.put("id", doc.getId());
                map.put("label", doc.getTitle());
                map.put("value", doc.getTitle());
                wordResult.add(map);
            }
            return JSON.toJSONString(wordResult);
        } catch (UnsupportedEncodingException e) {
            logger.info("query error", e);
            return "[]";
        }
    }

    @RequestMapping(value = "/api/queryHot")
    public CommonResult queryHot() {
        logger.info("query hot data");
        List<Doc> docs = dbHelper.queryTopic();
        try {
            for (Doc doc : docs) {
                doc.setUrl("/redirect?targetUrl=" + URLEncoder.encode(doc.getUrl(), Charsets.UTF_8.toString()));
            }
        } catch (UnsupportedEncodingException e) {
            logger.info("query hot data error", e);
        }
        return CommonResult.success(docs);
    }

}
