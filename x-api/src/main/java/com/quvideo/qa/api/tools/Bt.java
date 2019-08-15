package com.quvideo.qa.api.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.quvideo.qa.common.tools.QuvAssert;
import io.restassured.response.Response;

public class Bt {

    /**
     * 如果响应是jsonArray，且以jsonObject作为数据类型，验证array第index个jsonobject是否包含传入的属性
     */
    public static void softassertJsonObjectKeystr(QuvAssert softAssert, Response response, int index, String keys) {
        String[] keysarr = keys.split(",");
        softassertJsonObjectKeyarr(softAssert, response, index, keysarr);
    }

    /**
     * 如果响应body是jsonObject，验证jsonobject是否包含传入的属性
     */
    public static void softassertJsonObjectKeystr(QuvAssert softAssert, Response response, String keys) {
        String[] keysarr = keys.split(",");
        softassertJsonObjectKeyarr(softAssert, response, keysarr);
    }

    /**
     * 验证响应body是jsonObject，jsonobject是否包含传入的属性
     */
    public static void softassertJsonObjectKeyarr(QuvAssert softAssert, Response response, String... keys) {
        JSONObject jsonObject = JSON.parseObject(response.body().asString());
        softassertJsonObjectKeyarr(softAssert, jsonObject, keys);
    }

    /**
     * 验证响应是jsonArry，且以jsonObject作为数据类型，jsonobject是否包含传入的属性
     */
    public static void softassertJsonObjectKeyarr(QuvAssert softAssert, Response response, int index, String... keys) {
        JSONArray jsonArray = JSON.parseArray(response.body().asString());
        JSONObject jsonObject = jsonArray.getJSONObject(index);
        softassertJsonObjectKeyarr(softAssert, jsonObject, keys);
    }


    /**
     * 验证jsonobject是否包含传入的属性
     */
    public static void softassertJsonObjectKeyarr(QuvAssert softAssert, JSONObject jsonObject, String... keys) {
        for (String k : keys) {
            softAssert.assertTrue(jsonObject.containsKey(k.trim()), "返回对象不包含属性：" + k);
        }
    }


    /**
     * 验证jsonobject是否包含传入的属性
     */
    public static void softassertJsonObjectKeystr(QuvAssert softAssert, JSONObject jsonObject, String keys) {
        String[] keysarr = keys.split(",");
        softassertJsonObjectKeyarr(softAssert, jsonObject, keysarr);
    }
}
