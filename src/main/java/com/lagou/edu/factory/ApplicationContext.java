package com.lagou.edu.factory;

import com.lagou.edu.LagouApplication;
import com.lagou.edu.annotation.*;
import com.lagou.edu.service.TransferService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.StringUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ApplicationContext {


    private static Map<String, Object> map = new HashMap<>();

    static {
        Set<Class> clazzSet = new HashSet();
        try {
            String packageName = LagouApplication.class.getPackage().getName();
            packageName = packageName.replace(".", File.separator);
            PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
            CachingMetadataReaderFactory cachingMetadataReaderFactory = new CachingMetadataReaderFactory();
            Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath*:" + packageName + "/**/*.class");

            //遍历所有的类，
//            ClassLoader loader = ClassLoader.getSystemClassLoader();
            for (Resource resource : resources) {
                MetadataReader reader = cachingMetadataReaderFactory.getMetadataReader(resource);
                String className = reader.getClassMetadata().getClassName();
                try {
                    if (className.indexOf("TransferServlet") > -1) {
                        continue;
                    }
                    clazzSet.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    System.out.println("当前文件不可以实例化，文件名为：" + className);
                }
            }

            //并将响应的注解实例化放入ioc容器
            Iterator<Class> iterator = clazzSet.iterator();
            while (iterator.hasNext()) {
                Class next = iterator.next();
                Annotation[] annotations = next.getAnnotations();
                if (null != annotations && annotations.length > 0) {
                    for (int i = 0; i < annotations.length; i++) {
                        Annotation annotation = annotations[i];
                        if (annotation instanceof Component) {
                            resolveComponent(next);
                        } else if (annotation instanceof Repository) {
                            Repository repositoryAnno = (Repository) annotation;
                            String value = repositoryAnno.value();
                            resolveAnnotationValue(next, value);
                        } else if (annotation instanceof Service) {
                            Service repositoryAnno = (Service) annotation;
                            String value = repositoryAnno.value();
                            resolveAnnotationValue(next, value);
                        }
                    }
                }
            }
            //查找依赖关系并注入
            Iterator<Class> autowiredIterator = clazzSet.iterator();
            while (autowiredIterator.hasNext()) {
                Class next = autowiredIterator.next();
                Field[] fields = next.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    Annotation autowiredAnnotation = field.getAnnotation(Autowired.class);
                    if (Objects.nonNull(autowiredAnnotation)) {
                        String beanName = lowerFirstChar(field.getType().getSimpleName());
                        Object instanceValue = map.get(beanName);
                        if (Objects.nonNull(instanceValue)) {
                            field.setAccessible(true);
                            String keyName = getKeyName(next);
                            if (!StringUtils.isEmpty(keyName)) {
                                Object classInstanceValue = map.get(keyName);
                                field.set(classInstanceValue, instanceValue);
                            }
                        }
                    }
                }
            }

            //代理类的实例化
            Iterator<Class> proxyIterator = clazzSet.iterator();
            while (proxyIterator.hasNext()) {
                Class next = proxyIterator.next();
                //解析代理类上注解
                Annotation annotation = next.getAnnotation(Transactional.class);
                resolveProxyAnnotation(next, annotation);

                //解析方法上注解
                Method[] methods = next.getDeclaredMethods();
                for (int i = 0; i < methods.length; i++) {
                    Method method = methods[i];
                    Transactional methodAnnotation = method.getAnnotation(Transactional.class);
                    resolveProxyAnnotation(next, methodAnnotation);
                    break;
                }
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("异常信息:" + e.getMessage());
        }
    }

    private static String getKeyName(Class next) {
        Annotation[] annotations = next.getAnnotations();
        if (null != annotations && annotations.length > 0) {
            for (int i = 0; i < annotations.length; i++) {
                Annotation annotation = annotations[i];
                if (annotation instanceof Component) {
                    return lowerFirstChar(next.getSimpleName());
                } else if (annotation instanceof Repository) {
                    Repository repositoryAnno = (Repository) annotation;
                    return repositoryAnno.value();
                } else if (annotation instanceof Service) {
                    Service repositoryAnno = (Service) annotation;
                    return repositoryAnno.value();
                }
            }
        }
        return "";

    }

    private static void resolveProxyAnnotation(Class next, Annotation annotation) {
        if (null != annotation) {
            ProxyFactory proxyFactory = (ProxyFactory) map.get("proxyFactory");
            Class[] interfaces = next.getInterfaces();
            String instanceKey = getKeyName(next);
            Object targetInstance = map.get(instanceKey);
            if (Objects.nonNull(targetInstance)) {
                Object proxyValue;
                if (null != interfaces && interfaces.length > 0) {
                    proxyValue = proxyFactory.getJdkProxy(targetInstance);
                } else {
                    proxyValue = proxyFactory.getCglibProxy(targetInstance);
                }
                map.put(instanceKey, proxyValue);
            }
        }
    }

    public static void resolveAnnotationValue(Class aClass, String key) {
        try {
            Object instanceValue = aClass.getDeclaredConstructor().newInstance();
            map.put(key, instanceValue);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void resolveComponent(Class aClass) {
        try {
            String instanceKey = lowerFirstChar(aClass.getSimpleName());
            Object instanceValue = aClass.getDeclaredConstructor().newInstance();
            map.put(instanceKey, instanceValue);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 将类名首字母小写
     *
     * @param str
     * @return
     */
    private static String lowerFirstChar(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }


    public static Object getBean(String transferService) {
        return map.get(transferService);
    }

}
