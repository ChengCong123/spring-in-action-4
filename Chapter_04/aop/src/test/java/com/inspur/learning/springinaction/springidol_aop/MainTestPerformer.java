package com.inspur.learning.springinaction.springidol_aop;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by LTN on 2016/6/26.
 */

public class MainTestPerformer {
    public static void main(String[] args) throws PerformanceException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("com/inspur/learning/springinaction/springidol_aop/spring-idol-after-before.xml");

       Performer performer=(Performer) ctx.getBean("eddie");
        performer.perform();
    }
}
