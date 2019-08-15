package com.quvideo.qa.common.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

/**
 * 合并两个java bean对象非空属性（泛型）
 */
public class BeanUtils {

    private static Logger LOG = LoggerFactory.getLogger(BeanUtils.class);

    //merge two bean by discovering differences
    public static <M> void merge(M target, M destination) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass());
            // Iterate over all the attributes
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {

                // Only copy writable attributes
                if (descriptor.getWriteMethod() != null) {
                    //  Object originalValue = descriptor.getReadMethod().invoke(target);
                    Object defaultValue = descriptor.getReadMethod().invoke(
                            destination);
                    // Only copy values values where the destination values is null
                    if (defaultValue != null) {
                        descriptor.getWriteMethod().invoke(target, defaultValue);
                    }

                }
            }
        } catch (Exception e) {
            LOG.error(" 合并两个java bean对象非空属性错误！", e);
        }
    }
}
