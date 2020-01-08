package me.codeboy.doc.es;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据源
 * Created by yuedong.li on 2019/3/19
 */
@Configuration
class EsRestClient {
    private static final Logger log = LoggerFactory.getLogger(EsRestClient.class);

    @Bean
    public RestClient restClient() {
        return RestClient.builder(new HttpHost("127.0.0.1", 9200, "http")).build();
    }

}
