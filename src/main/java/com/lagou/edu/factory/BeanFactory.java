package com.lagou.edu.factory;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.sql.rowset.spi.XmlReader;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 工厂类，生产对象（使用反射技术）
 */
public class BeanFactory {

    /**
     * 任务1：解析xml,通过反射实例话对象并且存储待用
     * 任务2：对外提供获取实例对象的接口（根据id获取）
     */
    private static Map<String, Object> map = new HashMap<>();

//    static {
//        try {
//            InputStream stream = BeanFactory.class.getClassLoader().getResourceAsStream("beans.xml");
//            SAXReader saxReader = new SAXReader();
//            Document document = saxReader.read(stream);
//            Element rootElement = document.getRootElement();
//            List<Element> list = rootElement.selectNodes("//bean");
//            for (int i = 0; i < list.size(); i++) {
//                Element element = list.get(i);
//                String id = element.attributeValue("id");
//                String clazz = element.attributeValue("class");
////                通过反射技术实例化对象，
//                Class<?> aClass = Class.forName(clazz);
//                map.put(id, aClass.newInstance());
//            }
////            实例化完成后维护对象的依赖关系
//
//            List<Element> properties = rootElement.selectNodes("//property");
//
//            for (int i = 0; i < properties.size(); i++) {
//                Element element = properties.get(i);
//                //<property name="AccountDao" ref="accountDao"></property>
//                String name = element.attributeValue("name");
//                String ref = element.attributeValue("ref");
//                String excuteMethod = "set" + name;
//                Object excuteObj = map.get(ref);
//
//                Element parent = element.getParent();
//                String parentId = parent.attributeValue("id");
//                Object parentObj = map.get(parentId);
//                Method[] declaredMethods = parentObj.getClass().getDeclaredMethods();
////执行parentObj的set方法
//                for (int j = 0; j < declaredMethods.length; j++) {
//                    Method declaredMethod = declaredMethods[j];
//                    if (declaredMethod.getName().equals(excuteMethod)) {
//                        declaredMethod.invoke(parentObj, excuteObj);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static Object getBean(String id) {
        return map.get(id);
    }

    public static Map<String, Object> getMap() {
        return map;
    }

    public static void setMap(Map<String, Object> map) {
        BeanFactory.map = map;
    }
}
