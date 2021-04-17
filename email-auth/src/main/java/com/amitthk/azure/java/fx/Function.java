package com.amitthk.azure.java.fx;

import com.amitthk.azure.java.common.Util;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.StringWriter;
import java.util.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {

    private final CloseableHttpClient httpClient = HttpClients.createDefault();


    @FunctionName("EmailAuth")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String bookingNo = getKeyValue(request,"bookingNo");
        String bookingConfirm = getKeyValue(request,"bookingConfirm");

        if ((bookingNo == null)|| (bookingConfirm==null)) {
            return  createResponse(request,HttpStatus.BAD_REQUEST,"Insufficient parameters. Both bookingNo, bookingConfirm should be provided in queryString or request body.");
        } else {
            try{
                Map<String,String> paramsMap = new HashMap<>();
                paramsMap.put("bookingNo",bookingNo);
                paramsMap.put("bookingConfirm",bookingConfirm);
                String responseText=  sendPost(paramsMap);
                return request.createResponseBuilder(HttpStatus.OK).body("Response: " + responseText).build();
            }catch (Exception exc){
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(Util.printStackTraceToString(exc)).build();
            }

        }
    }

    private String getKeyValue(HttpRequestMessage<Optional<String>> request, String keyName){
        final String query = request.getQueryParameters().get(keyName);
        final String keyVal = request.getBody().orElse(query);
        return keyVal;
    }

    private  HttpResponseMessage createResponse(HttpRequestMessage<Optional<String>> request, HttpStatus status, String body){
        return request.createResponseBuilder(status).body(body).build();
    }

    private String sendPost(Map<String,String> keyValuePairs) throws Exception {

        HttpPost post = new HttpPost("https://httpbin.org/post");

        // add request parameter, form parameters
        List<NameValuePair> urlParameters = new ArrayList<>();
        for (Map.Entry<String,String> paramKey :
                keyValuePairs.entrySet()) {
            urlParameters.add(new BasicNameValuePair(paramKey.getKey(), paramKey.getValue()));

        }

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {
            StringWriter stringWriter = new StringWriter();
            stringWriter.append(EntityUtils.toString(response.getEntity()));
            String resp = stringWriter.toString();
            stringWriter.close();
            return resp;
        }

    }
}
