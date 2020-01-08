package me.codeboy.doc.db.helper;

import ch.qos.logback.core.db.DBHelper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import me.codeboy.doc.data.Constants;
import me.codeboy.doc.db.domain.Doc;
import me.codeboy.doc.db.domain.DocInfo;
import me.codeboy.doc.db.mapper.DocMapper;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static me.codeboy.doc.data.Constants.*;

/**
 * db helper
 * Created by yuedong.li on 2019-11-07
 */
@Component
public class DbHelper {
    private final static Logger logger = LoggerFactory.getLogger(DBHelper.class);
    private SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

    @Autowired
    private DocMapper docMapper;

    @Autowired
    private RestClient restClient;

    public boolean saveDoc(Doc doc) {
        if (doc != null) {
            return docMapper.insert(doc) > 0;
        }
        return false;
    }

    /**
     * 获取所有废弃文档
     */
    public List<Doc> queryAllOldDocs(long time) {
        QueryWrapper<Doc> wrapper = new QueryWrapper<>();
        wrapper.lt("updated_time", time);
        return docMapper.selectList(wrapper);
    }

    /**
     * 删除所有废弃文档
     */
    public boolean deleteAllOldDocs(long time) {
        QueryWrapper<Doc> wrapper = new QueryWrapper<>();
        wrapper.lt("updated_time", time);
        return docMapper.delete(wrapper) >= 0;
    }

    /**
     * 删除所有文档
     */
    public boolean deleteAllDocs() {
        QueryWrapper<Doc> wrapper = new QueryWrapper<>();
        wrapper.ge("updated_time", 0);
        return docMapper.delete(wrapper) >= 0;
    }

    public boolean updateDoc(Doc doc) {
        if (doc != null) {
            doc.setUpdatedTime(System.currentTimeMillis());
            return docMapper.updateById(doc) > 0;
        }
        return false;
    }

    /**
     * 查找联想词
     *
     * @return names
     */
    public List<Doc> queryTopic() {
        QueryWrapper<Doc> wrapper = new QueryWrapper<>();
        wrapper.last("order by times desc limit 30");
        return docMapper.selectList(wrapper);
    }

    /**
     * 查找联想词
     *
     * @return names
     */
    public List<Doc> querySuggestTitle(String keyword) {
        QueryWrapper<Doc> wrapper = new QueryWrapper<>();
        wrapper.like("title", keyword);
        wrapper.last("order by times desc limit 10");
        return docMapper.selectList(wrapper);
    }

    /**
     * 查找doc
     */
    public Doc queryDocByUrl(String url) {
        QueryWrapper<Doc> wrapper = new QueryWrapper<>();
        wrapper.eq("url", url);
        return docMapper.selectOne(wrapper);
    }

    /**
     * 查找doc
     */
    public Doc queryDocByEsId(String esId) {
        QueryWrapper<Doc> wrapper = new QueryWrapper<>();
        wrapper.eq("es_id", esId);
        return docMapper.selectOne(wrapper);
    }

    /**
     * 增加times
     */
    public void addAccessForUrl(String url) {
        Doc doc = queryDocByUrl(url);
        if (doc != null) {
            doc.setTimes(doc.getTimes() + 1);
            docMapper.updateById(doc);
        }
    }

    /**
     * 获取es数据
     *
     * @return es数据
     */
    public List<DocInfo> queryEsDocs(String keyword) {
        List<DocInfo> docInfos = Lists.newArrayList();
        try {
            Request request = new Request("GET", INDEX + "/_search");
            request.setJsonEntity(String.format(Locale.CHINA, "{\"query\":{\"multi_match\":{\"query\":\"%s\",\"fields\":[\"title\",\"content^2\"]}},\"sort\":[{\"_score\":{\"order\":\"desc\"}}],\"highlight\":{\"pre_tags\":[\"<span class='highlight'>\"],\"post_tags\":[\"</span>\"],\"fields\":{\"content\":{\"fragment_size\":216}}},\"size\":30}", keyword));
            Response response = restClient.performRequest(request);
            String responseBody;

            if (response.getStatusLine().getStatusCode() == 200) {
                responseBody = EntityUtils.toString(response.getEntity());
                JSONObject docs = JSONObject.parseObject(responseBody);
                JSONArray docArr = docs.getJSONObject("hits").getJSONArray("hits");
                if (docArr != null && docArr.size() > 0) {
                    for (int i = 0; i < docArr.size(); i++) {
                        JSONObject docItem = docArr.getJSONObject(i);
                        JSONObject docSource = docItem.getJSONObject(Constants.SOURCE);
                        String esId = docItem.getString(Constants.ID);
                        String title = docSource.getString(Constants.TITLE);
                        String url = docSource.getString(Constants.URL);
                        String content = docSource.getString(Constants.CONTENT);
                        if (docItem.containsKey(Constants.HIGHLIGHT)) {
                            content = docItem.getJSONObject(Constants.HIGHLIGHT).getJSONArray(Constants.CONTENT).getString(0);
                        }
                        docInfos.add(DocInfo.create(esId, title, content, url));
                    }
                }
                return docInfos;
            }
        } catch (Throwable e) {
            logger.error("queryEsDocs error", e);
        }
        return docInfos;
    }

    /**
     * 添加es数据
     *
     * @param title   doc title
     * @param content doc content
     * @param url     doc url
     */
    public String addEsData(String title, String content, String url) {
        try {
            Request request = new Request("POST", INDEX + "/" + TYPE);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(TITLE, title);
            jsonObject.put(CONTENT, content);
            jsonObject.put(URL, url);
            jsonObject.put(UPDATED_TIME, sdf.format(System.currentTimeMillis()));
            request.setJsonEntity(jsonObject.toJSONString());
            Response response = restClient.performRequest(request);
            String responseBody;

            if (response.getStatusLine().getStatusCode() < 300) {
                responseBody = EntityUtils.toString(response.getEntity());
                return JSON.parseObject(responseBody).getString(Constants.ID);
            }
        } catch (Throwable e) {
            logger.error("add es data error", e);
        }
        return null;
    }

    /**
     * 创建索引
     */
    public boolean createEsIndex() {
        try {
            Request request = new Request("PUT", "/" + INDEX);
            Response response = restClient.performRequest(request);
            if (response.getStatusLine().getStatusCode() < 300) {
                request = new Request("PUT", INDEX + "/_mapping");
                request.setJsonEntity("{\"properties\":{\"content\":{\"type\":\"text\",\"analyzer\":\"ik_max_word\",\"search_analyzer\":\"ik_smart\"}}}");
                response = restClient.performRequest(request);
                return response.getStatusLine().getStatusCode() < 300;
            }
        } catch (Throwable e) {
            logger.error("add es index error", e);
        }
        return false;
    }

    /**
     * 更新es数据
     *
     * @param esId    es id
     * @param title   doc title
     * @param content doc content
     */
    public boolean updateEsData(String esId, String title, String content, String url) {
        try {
            Request request = new Request("POST", INDEX + "/_update/" + esId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(TITLE, title);
            jsonObject.put(CONTENT, content);
            //为了解决问题重建index问题，url也添加上去
            jsonObject.put(URL, url);
            jsonObject.put(UPDATED_TIME, sdf.format(System.currentTimeMillis()));
            JSONObject updateData = new JSONObject();
            updateData.put(Constants.DOC, jsonObject);
            request.setJsonEntity(updateData.toJSONString());
            Response response = restClient.performRequest(request);
            return response.getStatusLine().getStatusCode() < 300;
        } catch (Throwable e) {
            logger.error("update es data error", e);
        }
        return false;
    }

    /**
     * 删除es数据
     *
     * @param esId es is
     */
    public boolean deleteEsData(String esId) {
        try {
            Request request = new Request("POST", INDEX + "/_delete_by_query");
            request.setJsonEntity(String.format(Locale.CHINA, "{\"query\":{\"match\":{\"_id\":\"%s\"}}}", esId));
            Response response = restClient.performRequest(request);
            return response.getStatusLine().getStatusCode() < 300;
        } catch (Throwable e) {
            logger.error("delete es data error", e);
        }
        return false;
    }

    /**
     * 删除es数据
     */
    public boolean resetEsData() {
        try {
            Request request = new Request("POST", INDEX + "/_delete_by_query");
            request.setJsonEntity("{\"query\":{\"match_all\":{}}}");
            Response response = restClient.performRequest(request);
            return response.getStatusLine().getStatusCode() < 300;
        } catch (Throwable e) {
            logger.error("delete es data error", e);
        }
        return false;
    }
}
