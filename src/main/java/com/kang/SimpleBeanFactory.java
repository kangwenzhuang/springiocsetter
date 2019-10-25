package com.kang;

import com.kang.service.UserService;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleBeanFactory {
    //定义beanFactory保存bean,key-value
    private Map<String, Object> beanFactory = new HashMap<String, Object>();
    private String path;

    public SimpleBeanFactory(String path) {
        this.path = path;
        BeansAndI();
    }

    void BeansAndI() {
        //使用dom4j解析xml文件读取bean标签，将对象创建出来用id为键，对象为值保存到容器中
        SAXReader reader = new SAXReader();
        try {
            //通过反射机制获取资源文件的路径
            URL url = SimpleBeanFactory.class.getClassLoader().getResource(this.path);
            //读取xml文件
            Document document = reader.read(url);
            //获取根便签
            Element rootElement = document.getRootElement();
            //读取bean标签，得到对应对象集合
            List<Element> beans = rootElement.elements("bean");

            //获取bean标签中的id作为键，class对象值，并保存到容器中
            for (Element b : beans) {
                //获取id键
                String key = b.attributeValue("id");
                //获取class值
                String value = b.attributeValue("class");
                //存入容器中
                beanFactory.put(key, Class.forName(value).newInstance());
            }
            //根据property标签定义的依赖关系完成依赖注入
            for (Element b : beans) {
                //获取bean下的property标签
                List<Element> properties = b.elements("property");
                for (Element p : properties) {
                    //需要被赋值的属性名称
                    String pName = p.attributeValue("name");
                    //该属性需要赋值的对象在容器里的id
                    String pRef = p.attributeValue("ref");
                    //从容器中拿到被依赖的对象
                    Object refObject = beanFactory.get(pRef);

                    //根据bean标签拿到正在循环的这个bean标签的id
                    String key = b.attributeValue("id");
                    //从容器中取出依赖方对象
                    Object object = beanFactory.get(key);
                    //获取依赖方的字节码信息
                    Class c = object.getClass();
                    //获取需要被赋值的属性对象
                    Field field = c.getDeclaredField(pName);
                    field.setAccessible(true);
                    field.set(object, refObject);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object getBean(String id) {
        return beanFactory.get(id);
    }

    //测试一下能否运行
    public static void main(String[] args) {
        SimpleBeanFactory beans = new SimpleBeanFactory("ApplicationContext.xml");
        UserService userService = (UserService) beans.getBean("userService");
        userService.addUser();
    }
}
