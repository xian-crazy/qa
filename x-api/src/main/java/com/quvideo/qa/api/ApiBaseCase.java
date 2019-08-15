package com.quvideo.qa.api;

import io.restassured.RestAssured;

public class ApiBaseCase {
    static {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.filters(new AllureRestAssured2().setRequestTemplate("httprequest.ftl").setResponseTemplate("httpresponse.ftl"));
        //RestAssured.config().decoderConfig(new DecoderConfig("UTF-8"));
    }
}
