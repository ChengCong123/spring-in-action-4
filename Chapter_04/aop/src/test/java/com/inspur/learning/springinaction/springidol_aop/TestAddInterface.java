package com.inspur.learning.springinaction.springidol_aop;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by LTN on 2016/6/26.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("spring-idol-addInterface.xml")
public class TestAddInterface {
    @Autowired
    ApplicationContext context;

    /**
     * 在Bean中引入新方法，代理拦截调用并委托给实现该方法的其他对象
     * 也就是，原本声明的bean eddie是Instrumentalist类型，而在xml配置文件中注入新功能，则eddie变为了新的接口。
     * @throws PerformanceException
     */
    @Test
    public void testAddInterface() throws PerformanceException {
        Contestant eddie = (Contestant) context.getBean("eddie");
        eddie.receiveAward();
    }
}
