package pl.kaszaq.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import static pl.kaszaq.Config.OBJECT_MAPPER;

@Slf4j
public class HttpClient {

    private final CloseableHttpClient httpClient;

    public HttpClient(String jsessionId) {
        httpClient = HttpClients.custom()
                .setDefaultHeaders(Lists.newArrayList(
                        new BasicHeader("Cookie", "JSESSIONID=" + jsessionId)))
                .build();
    }

    

    public String get(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        LOG.info("Executing GET request {}", httpGet.getRequestLine());
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        }
    }

    public String postJson(String url, Object object)
            throws IOException, JsonProcessingException, UnsupportedCharsetException, ParseException {
        HttpPost post = new HttpPost(url);
        String entity = OBJECT_MAPPER.writeValueAsString(object);
        
        LOG.info("Executing POST request {} with body {}", post.getRequestLine(), entity);
        StringEntity requestEntity = new StringEntity(
                entity,
                ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);
        String response;
        try (CloseableHttpResponse httpResponse = httpClient.execute(post)) {
            response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        }
        return response;
    }
}
