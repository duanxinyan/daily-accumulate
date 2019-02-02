package com.daily.duan.dubbo.support;

/**
 * @ProjectName: daily-accumulate
 * @Package: com.daily.duan.dubbo.support
 * @ClassName: BeanFactory
 * @Author: duanxinxing
 * @Description:
 * @Date: 2019/2/2 13:04
 * @Version: 1.0
 */
public interface BeanFactory {

    <T> T getBean(Class<T> var1);

    <T> boolean isFactoryOf(Class<T> clazz);
}
