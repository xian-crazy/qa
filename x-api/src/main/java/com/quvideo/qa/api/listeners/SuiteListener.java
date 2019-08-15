package com.quvideo.qa.api.listeners;

import com.quvideo.qa.common.tools.BT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;


public class SuiteListener implements ISuiteListener {
    //protected Logger log = LogManager.getLogger(this.getClass());
    protected  Logger log  = LoggerFactory.getLogger(this.getClass());

    public void onStart(ISuite iSuite) {
        log.warn("------------- suite ["+iSuite.getName()+"] onStart -------------");
    }

    public void onFinish(ISuite iSuite) {
        log.warn("------------- suite ["+iSuite.getName()+"] onFinish -------------");
        BT.colseAllMybatisSession();
    }
}
