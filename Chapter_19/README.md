第19章　使用Spring发送Email
--------------------
# 1　配置Spring发送邮件
Spring Email抽象的核心是MailSender接口。顾名思义，MailSender的实现能够通过连接Email服务器实现邮件发送的功能，如图19.1所示。
<br/>![](img/img19-1.jpg)<br/>
图19.1　Spring的MailSender接口是Spring Email抽象API的核心组件。它把Email发送给邮件服务器，由服务器进行邮件投递

Spring自带了一个MailSender的实现也就是JavaMailSenderImpl，它会使用JavaMail API来发送Email。Spring应用在发送Email之前，我们必须要将JavaMailSenderImpl装配为Spring应用上下文中的一个bean。

## 1.1　配置邮件发送器
```java
  @Bean
  public MailSender mailSender(Environment env) {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(env.getProperty("mailserver.host"));
    mailSender.setPort(Integer.parseInt(env.getProperty("mailserver.port")));
    mailSender.setUsername(env.getProperty("mailserver.username"));
    mailSender.setPassword(env.getProperty("mailserver.password"));
    return mailSender;
  }
```

## 1.2　装配和使用邮件发送器
我们想要给Spitter用户发送Email提示他的朋友写了新的Spittle，所以我们需要一个方法来发送Email，这个方法要接受Email地址和Spittle对象信息。
```java
  @Override
  public void sendSimpleSpittleEmail(String to, Spittle spittle) {
    SimpleMailMessage message = new SimpleMailMessage();
    String spitterName = spittle.getSpitter().getFullName();
    message.setFrom("noreply@spitter.com");
    message.setTo(to);
    message.setSubject("New spittle from " + spitterName);
    message.setText(spitterName + " says: " + spittle.getText());
    mailSender.send(message);
  }
```

# 2　构建丰富内容的Email消息
Spring的Email功能并不局限于纯文本的Email。我们可以添加附件，甚至可以使用HTML来美化消息体的内容。让我们首先从基本的添加附件开始，然后更进一步，借助HTML使我们的Email消息更加美观。

## 2.1　添加附件
如果发送带有附件的Email，关键技巧是创建multipart类型的消息——Email由多个部分组成，其中一部分是Email体，其他部分是附件。

对于发送附件这样的需求来说，SimpleMailMessage过于简单了。为了发送multipart类型的Email，你需要创建一个MIME（Multipurpose Internet Mail Extensions）的消息，我们可以从邮件发送器的createMimeMessage()方法开始：
`MimeMessage message = mailSender.createMimeMessage();`

javax.mail.internet.MimeMessage本身的API有些笨重。好消息是，Spring提供的MimeMessageHelper可以帮助我们。为了使用MimeMessageHelper，我们需要实例化它并将MimeMessage传给其构造器：
`MimeMessageHelper helper = new MimeMessageHelper(message, true);`
构造方法的第二个参数，在这里是个布尔值true，表明这个消息是multipart类型的。

得到了MimeMessageHelper实例后，我们就可以组装Email消息了。这里最主要区别在于使用helper的方法来指定Email细节，而不再是设置消息对象.
```java
  @Override
  public void sendSpittleEmailWithAttachment(String to, Spittle spittle) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    String spitterName = spittle.getSpitter().getFullName();
    helper.setFrom("noreply@spitter.com");
    helper.setTo(to);
    helper.setSubject("New spittle from " + spitterName);
    helper.setText(spitterName + " says: " + spittle.getText());
    ClassPathResource couponImage = new ClassPathResource("/collateral/coupon.png");
    helper.addAttachment("Coupon.png", couponImage);
    mailSender.send(message);
  }
```

## 2.2　发送富文本内容的Email
发送富文本的Email与发送简单文本的Email并没有太大区别。关键是将消息的文本设置为HTML。要做到这一点只需将HTML字符串传递给helper的setText()方法，并将第二个参数设置为true：
<br/>![](img/richText.jpg)<br/>

第二个参数表明传递进来的第一个参数是HTML，所以需要对消息的内容类型进行相应的设置。
要注意的是，传递进来的HTML包含了一个<img>标签，用来在Email中展现Spittr应用程序的logo。src属性可以设置为标准的“http:”URL，以便于从Web中获取Spittr的logo。但在这里，我们将logo图片嵌入在了Email之中。值“cid:spitterLogo”表明在消息中会有一部分是图片并以spitterLogo来进行标识。

为消息添加嵌入式的图片与添加附件很类似。不过这次不再使用helper的addAttachment()方法，而是要调用addInline()方法：
```java
ClassPathResource couponImage = new ClassPathResource("coupon.png");
helper.addInline("spitterLogo", couponImage);
```

以下是新的sendRichSpitterEmail()方法:
<br/>![](img/sendRichSpitterEmail.jpg)<br/>

创建Email体时，使用字符串拼接的办法来构建HTML消息依旧让我觉得美中不足。在结束Email话题之前，让我们看看如何用模板来代替字符串拼接消息。

# 3　使用模板生成Email



# 源码
https://github.com/myitroad/spring-in-action-4/tree/master/Chapter_19