第19章　使用Spring发送Email
--------------------
# 1　配置Spring发送邮件
Spring Email抽象的核心是MailSender接口。顾名思义，MailSender的实现能够通过连接Email服务器实现邮件发送的功能，如图19.1所示。
<br/>![](img/img19-1.jpg)<br/>
图19.1　Spring的MailSender接口是Spring Email抽象API的核心组件。它把Email发送给邮件服务器，由服务器进行邮件投递

Spring自带了一个MailSender的实现也就是JavaMailSenderImpl，它会使用JavaMail API来发送Email。Spring应用在发送Email之前，我们必须要将JavaMailSenderImpl装配为Spring应用上下文中的一个bean。
# 2　构建丰富内容的Email消息
# 3　使用模板生成Email

# 源码
https://github.com/myitroad/spring-in-action-4/tree/master/Chapter_19