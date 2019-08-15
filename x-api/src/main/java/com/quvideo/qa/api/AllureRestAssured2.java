package com.quvideo.qa.api;


import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.internal.NameAndValue;
import io.restassured.internal.support.Prettifier;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.qameta.allure.attachment.DefaultAttachmentProcessor;
import io.qameta.allure.attachment.FreemarkerAttachmentRenderer;
import io.qameta.allure.attachment.http.HttpRequestAttachment;
import io.qameta.allure.attachment.http.HttpResponseAttachment;
import io.qameta.allure.attachment.http.HttpRequestAttachment.Builder;

import java.util.HashMap;
import java.util.Map;


public class AllureRestAssured2 implements OrderedFilter {
    private String requestTemplatePath = "http-request.ftl";
    private String responseTemplatePath = "http-response.ftl";

    public AllureRestAssured2() {
    }

    public AllureRestAssured2 setRequestTemplate(String templatePath) {
        this.requestTemplatePath = templatePath;
        return this;
    }

    public AllureRestAssured2 setResponseTemplate(String templatePath) {
        this.responseTemplatePath = templatePath;
        return this;
    }


    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext filterContext) {
        HttpRequestAttachment httpRequestAttachment=null;
        Prettifier prettifier = new Prettifier();
        Builder requestAttachmentBuilder = Builder.create("请求:"+requestSpec.getURI(), requestSpec.getURI()).setMethod(requestSpec.getMethod()).setHeaders(toMapConverter(requestSpec.getHeaders())).setCookies(toMapConverter(requestSpec.getCookies()));
        if (requestSpec.getBody()!=null) {
            requestAttachmentBuilder.setBody(prettifier.getPrettifiedBodyIfPossible(requestSpec));
        }


        HttpRequestAttachment requestAttachment = requestAttachmentBuilder.build();
        (new DefaultAttachmentProcessor()).addAttachment(requestAttachment, new FreemarkerAttachmentRenderer(this.requestTemplatePath));
        Response response = filterContext.next(requestSpec, responseSpec);
        HttpResponseAttachment responseAttachment = io.qameta.allure.attachment.http.HttpResponseAttachment.Builder.create("响应："+requestSpec.getURI()+":"+response.getStatusLine()).setResponseCode(response.getStatusCode()).setHeaders(toMapConverter(response.getHeaders())).setBody(prettifier.getPrettifiedBodyIfPossible(response, response.getBody())).build();
        (new DefaultAttachmentProcessor()).addAttachment(responseAttachment, new FreemarkerAttachmentRenderer(this.responseTemplatePath));
        return response;
    }

    private static Map<String, String> toMapConverter(Iterable<? extends NameAndValue> items) {
        Map<String, String> result = new HashMap<String, String>();
        items.forEach((h) -> {
            String var10000 = (String)result.put(h.getName(), h.getValue());
        });
        return result;
    }

    public int getOrder() {
        return 2147483647;
    }
}
