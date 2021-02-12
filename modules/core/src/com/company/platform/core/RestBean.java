package com.company.platform.core;

import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component(RestBean.NAME)
public class RestBean {
    public static final String NAME = "platform_RestBean";

    private MediaType contentType;

    private final MultiValueMap<String, Object> map;

    private String url;

    private final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

    private HttpMethod method;

    public RestBean(){
        map = new LinkedMultiValueMap<>();
        factory.setBufferRequestBody(false);
    }

    public ResponseEntity<String> process() throws IllegalStateException{
        if(method == null){
            throw new IllegalStateException("Enter a method");
        }

        RestTemplate template = new RestTemplate(factory);
        HttpEntity<MultiValueMap<String, Object>> entity = getEntity();
        return template.exchange(url, method, entity, String.class);
    }

    private HttpEntity<MultiValueMap<String, Object>> getEntity(){
        HttpHeaders headers = new HttpHeaders();
        if(contentType != null) {
            headers.setContentType(contentType);
        }
        return new HttpEntity<>(map, headers);
    }

    public void setContentType(MediaType contentType) {
        this.contentType = contentType;
    }

    public void addToBody(String key, Object value){
        map.add(key, value);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }
}