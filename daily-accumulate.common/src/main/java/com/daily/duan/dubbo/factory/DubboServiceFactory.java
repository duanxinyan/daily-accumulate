package com.daily.duan.dubbo.factory;

import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.MonitorConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ProjectName: daily-accumulate
 * @Package: com.daily.duan.dubbo.factory
 * @ClassName: DubboServiceFactory
 * @Author: duanxinxing
 * @Description:
 * @Date: 2019/2/2 12:58
 * @Version: 1.0
 */
public class DubboServiceFactory {

    private static ApplicationConfig app;

    private static Map<Class<?>, ReferenceConfig<?>> cache = new ConcurrentHashMap<Class<?>, ReferenceConfig<?>>();

    private static RegistryConfig registry;

    private static MonitorConfig monitor;

    private static String directUrl;

    private static boolean isInit;

    static {
        Properties prop = ConfigUtils.loadProperties("dubbo.properties");

        String applicationName = prop.getProperty("dubbo.application.name");

        String registryProtocol = prop.getProperty("dubbo.registry.protocol");

        String registryAddress = prop.getProperty("dubbo.registry.address");

        String monitorProtocol = prop.getProperty("dubbo.monitor.protocol");

        String monitorAddress = prop.getProperty("dubbo.monitor.address");

        //注册配置缺失
        if (StringUtils.isEmpty(applicationName) || StringUtils.isEmpty(registryProtocol) || StringUtils.isEmpty(registryAddress)) {
            throw new IllegalStateException("register center: protocol|address is missing!");
        }

        init(applicationName, registryProtocol, registryAddress, monitorProtocol, monitorAddress);
    }

    /**
     * 注意: 如果 monitorProtocol为registry，则monitorAddress不用填。 其他则要填地址和端口
     *
     * @param applicationName
     * @param registryProtocol
     * @param registryAddress
     * @param monitorProtocol
     * @param monitorAddress
     */
    public static void init(String applicationName, String registryProtocol, String registryAddress, String monitorProtocol, String monitorAddress) {
        if (!isInit) {
            //从注册中心取
            if (!registryProtocol.equals("dubbo")) {
                registry = new RegistryConfig();
                registry.setProtocol(registryProtocol);
                registry.setAddress(registryAddress);
            } else {
                directUrl = registryProtocol + "://" + registryAddress;
            }

            app = new ApplicationConfig();
            app.setName(applicationName);

            if (StringUtils.isNotEmpty(monitorProtocol)) {
                monitor = new MonitorConfig();
                monitor.setProtocol(monitorProtocol);
                if (!"registry".equals(monitorProtocol)) {
                    monitor.setAddress(monitorAddress);
                }
            }

            isInit = true;
        }
    }

    public static void init(String applicationName, String registryProtocol, String registryAddress) {
        init(applicationName, registryProtocol, registryAddress, null, null);
    }

    /**
     * 注意: 如果 monitorProtocol为registry，则monitorAddress不用填
     *
     * @param applicationName
     * @param registryProtocol
     * @param registryAddress
     * @param monitorProtocol
     */
    public static void init(String applicationName, String registryProtocol, String registryAddress, String monitorProtocol) {
        if (!"registry".equals(monitorProtocol)) {
            monitorProtocol = null;
        }
        init(applicationName, registryProtocol, registryAddress, monitorProtocol, null);
    }

    /**
     * @param clazz
     * @return
     */
    public static <T> T getService(Class<T> clazz) {
        ReferenceConfig<T> reference = (ReferenceConfig<T>) cache.get(clazz);
        if (reference == null) {
            synchronized (clazz) {
                if (reference == null) {
                    // 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
                    reference = new ReferenceConfig<T>();
                    reference.setApplication(app);
                    // 从注册中心取
                    if (registry != null) {
                        reference.setRegistry(registry);
                    } else {
                        reference.setUrl(directUrl);
                    }
                    if (monitor != null) {
                        reference.setMonitor(monitor);
                    }
                    reference.setInterface(clazz);
                    cache.put(clazz, reference);
                }
            }
        }
        return (T) reference.get();
    }
}
