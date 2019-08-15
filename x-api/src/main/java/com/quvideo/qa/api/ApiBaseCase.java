package com.quvideo.qa.api;

import io.restassured.RestAssured;
import org.testng.annotations.Listeners;

@Listeners({ com.quvideo.qa.api.listeners.SuiteListener.class,com.quvideo.qa.api.listeners.TestListener.class })
public class ApiBaseCase {
    static {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.filters(new AllureRestAssured2().setRequestTemplate("httprequest.ftl").setResponseTemplate("httpresponse.ftl"));
        //RestAssured.config().decoderConfig(new DecoderConfig("UTF-8"));
    }
}
