package pl.kaszaq.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import static pl.kaszaq.Config.OBJECT_MAPPER;
import pl.kaszaq.agile.jira.JiraSearchRequest;

public class CachingHttpClient {

    private final String cacheDirectory;
    
     private final CloseableHttpClient httpClient;

    public CachingHttpClient(String cacheDirectory, CloseableHttpClient httpclient) {
        this.cacheDirectory = cacheDirectory;
        this.httpClient = httpclient;
    }
     
     

    public String get(String url) throws IOException {
        String fileName = Base64.encodeBase64URLSafeString(url.getBytes());
        String entity;
        File file = new File(cacheDirectory + fileName);
        if (file.exists()) {
            FileInputStream fist = new FileInputStream(file);
            return IOUtils.toString(fist, "UTF-8");
        }

        HttpGet httpGet = new HttpGet(url);

        System.out.println("Executing request " + httpGet.getRequestLine());
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            entity = EntityUtils.toString(response.getEntity(), "UTF-8");
            FileOutputStream fos = new FileOutputStream(file);
            IOUtils.write(entity, fos, "UTF-8");
        }

        return entity;
    }
    
    private String postJson(String url, Object searchRequest)
            throws IOException, JsonProcessingException, UnsupportedCharsetException, ParseException {
        HttpPost post = new HttpPost(url);
        StringEntity requestEntity = new StringEntity(
                OBJECT_MAPPER.writeValueAsString(searchRequest),
                ContentType.APPLICATION_JSON);
        post.setEntity(requestEntity);
        String response;
        try (CloseableHttpResponse httpResponse = httpClient.execute(post)) {
            response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        }
        return response;
    }
}
