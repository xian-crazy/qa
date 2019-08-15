package com.quvideo.qa.api.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.*;
import org.testng.internal.ConstructorOrMethod;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class TestListener implements ITestListener {
    //protected Logger log = LogManager.getLogger(this.getClass());
    protected static Logger log  = LoggerFactory.getLogger(TestListener.class);



    public void onTestStart(ITestResult iTestResult) {

        log.warn("-------------case [" + iTestResult.getName() + "] start -------------");
    }

    private <T extends Annotation> List<T> getAnnotationsOnMethod(ITestResult result, Class<T> clazz) {
        return (List) Stream.of(result).map(ITestResult::getMethod).filter(Objects::nonNull).map(ITestNGMethod::getConstructorOrMethod).map(ConstructorOrMethod::getMethod).flatMap((method) -> {
            return Stream.of(method.getAnnotationsByType(clazz));
        }).collect(Collectors.toList());
    }



    private <T extends Annotation> List<T> getAnnotationsOnClass(ITestResult result, Class<T> clazz) {
        Stream var10000 = Stream.of(result).map(ITestResult::getTestClass).filter(Objects::nonNull).map(IClass::getRealClass).flatMap((aClass) -> {
            return Stream.of(aClass.getAnnotationsByType(clazz));
        });
        clazz.getClass();
        return (List)var10000.map(clazz::cast).collect(Collectors.toList());
    }

    public void onTestSuccess(ITestResult iTestResult) {
        log.warn("-------------case [" + iTestResult.getName() + "] Success -------------");
    }

    public void onTestFailure(ITestResult iTestResult) {
        log.error("-------------case [" + iTestResult.getName() + "] Failure -------------");
        iTestResult.getThrowable().printStackTrace();
    }

    public void onTestSkipped(ITestResult iTestResult) {
        log.warn("-------------case [" + iTestResult.getName() + "] Skipped -------------");
    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
        log.warn("-------------case [" + iTestResult.getName() + "] FailedButWithinSuccessPercentage -------------");
    }

    public void onStart(ITestContext iTestContext) {
        log.warn("-------------test [" + iTestContext.getName() + "] onStart-------------");
    }

    public void onFinish(ITestContext iTestContext) {
        log.warn("------------- test [" + iTestContext.getName() + "] onFinish-------------");
    }



}