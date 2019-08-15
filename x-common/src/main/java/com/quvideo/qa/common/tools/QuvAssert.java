package com.quvideo.qa.common.tools;


import com.quvideo.qa.common.quvideoallure.AllureAssertion;
import io.qameta.allure.Step;
import org.apache.commons.lang3.StringUtils;
import org.testng.SkipException;
import org.testng.asserts.IAssert;
import org.testng.collections.Maps;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

public class QuvAssert extends AllureAssertion {
    private final Map<AssertionError, IAssert<?>> m_errors = Maps.newLinkedHashMap();

    public QuvAssert() {
    }

    public static void  skip(String message){
        throw new SkipException("当前用例因：【"+message+"】，跳过执行！");
    }


    public static void assertDate(int YYYY,int M,int D,String ...message) {
        Calendar cex = Calendar.getInstance();
        cex.set(YYYY, M - 1, D);
        Calendar cnow = Calendar.getInstance();
        if (cnow.getTime().before(cex.getTime())) {
            String mes= StringUtils.join(message,System.lineSeparator());
            throw new SkipException("当前用例计划在" + DateUtils.time(cex, "yyyy-MM-dd") + "开始执行"+System.lineSeparator()+mes);
        }
    }
    @Step("断言验证：{asserter.m_message}")
    protected void doAssert(IAssert<?> asserter) {
        this.onBeforeAssert(asserter);
        try {
            asserter.doAssert();
            this.onAssertSuccess(asserter);
        } catch (AssertionError var6) {
            this.onAssertFailure(asserter, var6);
            this.m_errors.put(var6, asserter);
        } finally {
            this.onAfterAssert(asserter);
        }

    }


    public void assertAll() {
        if (!this.m_errors.isEmpty()) {
            StringBuilder var1 = new StringBuilder("以下断言失败:");
            boolean var2 = true;
            Iterator var3 = this.m_errors.entrySet().iterator();

            while(var3.hasNext()) {
                Map.Entry var4 = (Map.Entry)var3.next();
                if (var2) {
                    var2 = false;
                } else {
                    var1.append(",");
                }

                var1.append("\n\t");
                var1.append(((AssertionError)var4.getKey()).getMessage());
            }

            throw new AssertionError(var1.toString());
        }
    }
}

