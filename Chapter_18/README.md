第18章　使用WebSocket和STOMP实现消息功能
-----------
Spring 4.0为WebSocket通信提供了支持，包括：
- 发送和接收消息的低层级API；
- 发送和接收消息的高级API；
- 用来发送消息的模板；
- 支持SockJS，用来解决浏览器端、服务器以及代理不支持WebSocket的问题。

# 1　使用Spring的低层级WebSocket API
按照其最简单的形式，WebSocket只是两个应用之间通信的通道。位于WebSocket一端的应用发送消息，另外一端处理消息。因为它是全双工的，所以每一端都可以发送和处理消息。如图18.1所示。
<br/>![](img/img18-1.jpg)<br/>

WebSocket通信可以应用于任何类型的应用中，但是WebSocket最常见的应用场景是实现服务器和基于浏览器的应用之间的通信。

为了在Spring使用较低层级的API来处理消息，我们必须编写一个实现WebSocketHandler的类.WebSocketHandler需要我们实现五个方法。相比直接实现WebSocketHandler，更为简单的方法是扩展AbstractWebSocketHandler，这是WebSocketHandler的一个抽象实现。

```java
public class MarcoHandler extends AbstractWebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(MarcoHandler.class);
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		logger.info("Received message: " + message.getPayload());
		Thread.sleep(2000);
		session.sendMessage(new TextMessage("Polo!"));
	}
	
}
```
除了重载WebSocketHandler中所定义的五个方法以外，我们还可以重载AbstractWebSocketHandler中所定义的三个方法：
- handleBinaryMessage()
- handlePongMessage()
- handleTextMessage()
这三个方法只是handleMessage()方法的具体化，每个方法对应于某一种特定类型的消息。

另外一种方案，我们可以扩展TextWebSocketHandler或BinaryWebSocketHandler。TextWebSocketHandler是AbstractWebSocketHandler的子类，它会拒绝处理二进制消息。它重载了handleBinaryMessage()方法，如果收到二进制消息的时候，将会关闭WebSocket连接。与之类似，BinaryWebSocketHandler也是AbstractWeb-SocketHandler的子类，它重载了handleTextMessage()方法，如果接收到文本消息的话，将会关闭连接。

现在，已经有了消息处理器类，我们必须要对其进行配置，这样Spring才能将消息转发给它。在Spring的Java配置中，这需要在一个配置类上使用@EnableWebSocket，并实现WebSocketConfigurer接口，如下面的程序清单所示。
程序清单18.2　在Java配置中，启用WebSocket并映射消息处理器
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//		registry.addHandler(marcoHandler(), "/marco").withSockJS();
		registry.addHandler(marcoHandler(), "/marco");
	}
  
	@Bean
	public MarcoHandler marcoHandler() {
		return new MarcoHandler();
	}

}
```
或XML配置：
程序清单18.3　借助websocket命名空间以XML的方式配置WebSocket
<br/>![](img/code18-3.jpg)<br/>

不管使用Java还是使用XML，这就是所需的配置。

现在，我们可以把注意力转向客户端，它会发送“Marco!”文本消息到服务器，并监听来自服务器的文本消息。如下程序清单所展示的JavaScript代码开启了一个原始的WebSocket并使用它来发送消息给服务器。

程序清单18.4　连接到“marco” WebSocket的JavaScript客户端
<br/>![](img/code18-4.jpg)<br/>

通过发送“Marco!”，这个无休止的Marco Polo游戏就开始了，因为服务器端的MarcoHandler作为响应会将“Polo!”发送回来，当客户端收到来自服务器的消息后，onmessage事件会发送另外一个“Marco!”给服务器。这个过程会一直持续下去，直到连接关闭。


# 2　应对不支持WebSocket的场景
WebSocket是一个相对比较新的规范。虽然它早在2011年底就实现了规范化，但即便如此，在Web浏览器和应用服务器上依然没有得到一致的支持。Firefox和Chrome早就已经完整支持WebSocket了，但是其他的一些浏览器刚刚开始支持WebSocket。如下列出了几个流行的浏览器支持WebSocket功能的最低版本：
- Internet Explorer：10.0
- Firefox: 4.0（部分支持），6.0（完整支持）。
- Chrome: 4.0（部分支持），13.0（完整支持）。
- Safari: 5.0（部分支持），6.0（完整支持）。
- Opera: 11.0（部分支持），12.10（完整支持）。
- iOS Safari: 4.2（部分支持），6.0（完整支持）。
- Android Browser: 4.4。

服务器端对WebSocket的支持也好不到哪里去。GlassFish在几年前就开始支持一定形式的WebSocket，但是很多其他的应用服务器在最近的版本中刚刚开始支持WebSocket。例如，我在测试上述例子的时候，所使用的就是Tomcat 8的发布候选构建版本。

即便浏览器和应用服务器的版本都符合要求，两端都支持WebSocket，在这两者之间还有可能出现问题。防火墙代理通常会限制所有除HTTP以外的流量。它们有可能不支持或者（还）没有配置允许进行WebSocket通信。

幸好，提到WebSocket的备用方案，这恰是SockJS所擅长的。SockJS让我们能够使用统一的编程模型，就好像在各个层面都完整支持WebSocket一样，SockJS在底层会提供备用方案。

例如，为了在服务端启用SockJS通信，我们在Spring配置中可以很简单地要求添加该功能。重新回顾一下程序清单18.2中的registerWebSocketHandlers()方法，稍微加一点内容就能启用SockJS：
```java
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(marcoHandler(), "/marco").withSockJS();
	}
```
- XML完成相同的配置效果：
<br/>![](img/xmlConfSockjs.jpg)<br/>

要在客户端使用SockJS，需要确保加载了SockJS客户端库。具体的做法在很大程度上依赖于使用JavaScript模块加载器（如require.js或curl.js）还是简单地使用<script>标签加载JavaScript库。加载SockJS客户端库的最简单办法是使用<script>标签从SockJS CDN中进行加载，如下所示：
```xml
<script src="http://cdn.sockjs.org/sockjs-0.3.min.js"></script>
```
除了加载SockJS客户端库以外，在程序清单18.4中，要使用SockJS只需修改两行代码：
```javascript
var url = 'marco';
var sock = new SocktJS(url);
```
所做的第一个修改就是URL。SockJS所处理的URL是“http://”或“https://”模式，而不是“ws://”和“wss://”。即便如此，我们还是可以使用相对URL，避免书写完整的全限定URL。在本例中，如果包含JavaScript的页面位于“http://localhost:8080/websocket”路径下，那么给定的“marco”路径将会形成到“http://localhost:8080/websocket/marco”的连接。

# 3　使用STOMP消息
直接使用WebSocket（或SockJS）就很类似于使用TCP套接字来编写Web应用。因为没有高层级的线路协议（wire protocol），因此就需要我们定义应用之间所发送消息的语义，还需要确保连接的两端都能遵循这些语义。
不过，好消息是我们并非必须要使用原生的WebSocket连接。就像HTTP在TCP套接字之上添加了请求-响应模型层一样，STOMP在WebSocket之上提供了一个基于帧的线路格式（frame-based wire format）层，用来定义消息的语义。

乍看上去，STOMP的消息格式非常类似于HTTP请求的结构。与HTTP请求和响应类似，STOMP帧由命令、一个或多个头信息以及负载所组成。例如，如下就是发送数据的一个STOMP帧：
```
SEND
destination:/app/marco
content-length:20

{\"message\":\"Marco!\"}
```

## 　3.1 启用STOMP消息功能
在Spring MVC中为控制器方法添加@MessageMapping注解，使其处理STOMP消息，它与带有@RequestMapping注解的方法处理HTTP请求的方式非常类似。但是与@RequestMapping不同的是
- @MessageMapping的功能无法通过@EnableWebMvc启用，而是@EnableWebSocketMessageBroker。
- Spring的Web消息功能基于消息代理（message broker）构建，因此除了告诉Spring我们想要处理消息以外，还有其他的内容需要配置。

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketStompConfig extends AbstractWebSocketMessageBrokerConfigurer {

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/marcopolo").withSockJS();
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
//    registry.enableStompBrokerRelay("/queue", "/topic");
    registry.enableSimpleBroker("/queue", "/topic");
    registry.setApplicationDestinationPrefixes("/app");
  }
  
}
```
上述配置，它重载了registerStompEndpoints()方法，将“/marcopolo”注册为STOMP端点。这个路径与之前发送和接收消息的目的地路径有所不同。这是一个端点，客户端在订阅或发布消息到目的地路径前，要连接该端点。

WebSocketStompConfig还通过重载configureMessageBroker()方法配置了一个简单的消息代理。消息代理将会处理前缀为“/topic”和“/queue”的消息。除此之外，发往应用程序的消息将会带有“/app”前缀。图18.2展现了这个配置中的消息流。
！！！

**启用STOMP代理中继**
对于生产环境下的应用来说，你可能会希望使用真正支持STOMP的代理来支撑WebSocket消息，如RabbitMQ或ActiveMQ。这样的代理提供了可扩展性和健壮性更好的消息功能，当然它们也会完整支持STOMP命令。我们需要根据相关的文档来为STOMP搭建代理。搭建就绪之后，就可以使用STOMP代理来替换内存代理了，只需按照如下方式重载configureMessageBroker()方法即可：
```java
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableStompBrokerRelay("/queue", "/topic");
    registry.setApplicationDestinationPrefixes("/app");
  }
```
- 上述configureMessageBroker()方法的第一行代码启用了STOMP代理中继（broker relay）功能，并将其目的地前缀设置为“/topic”和“/queue”。这样的话，Spring就能知道所有目的地前缀为“/topic”或“/queue”的消息都会发送到STOMP代理中。

- 在第二行的configureMessageBroker()方法中将应用的前缀设置为“/app”。所有目的地以“/app”打头的消息都将会路由到带有@MessageMapping注解的方法中，而不会发布到代理队列或主题中。

默认情况下，STOMP代理中继会假设代理监听localhost的61613端口，并且客户端的username和password均为“guest”。如果你的STOMP代理位于其他的服务器上，或者配置成了不同的客户端凭证，那么我们可以在启用STOMP代理中继的时候，需要配置这些细节信息：
```java
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableStompBrokerRelay("/queue", "/topic")
            .setRelayHost("rabbit.someotherserver")
            .setRelayPort(62623)
            .setClientLogin("marcopolo")
            .setClientPasscode("letmein01")
    registry.setApplicationDestinationPrefixes("/app");
  }
```
## 3.2　处理来自客户端的STOMP消息
Spring 4.0引入了@MessageMapping注解，它用于STOMP消息的处理，类似于Spring MVC的@RequestMapping注解。当消息抵达某个特定的目的地时，带有@MessageMapping注解的方法能够处理这些消息。
```java
@Controller
public class MarcoController {

  private static final Logger logger = LoggerFactory
      .getLogger(MarcoController.class);

  @MessageMapping("/marco")
  public Shout handleShout(Shout incoming) {
    logger.info("Received message: " + incoming.getMessage());

    try { Thread.sleep(2000); } catch (InterruptedException e) {}
    
    Shout outgoing = new Shout();
    outgoing.setMessage("Polo!");
    
    return outgoing;
  }

}
```
示handleShout()方法能够处理指定目的地上到达的消息。在本例中，这个目的地也就是“/app/marco”（“/app”前缀是隐含的，因为我们将其配置为应用的目的地前缀）。
- Shout类是个简单的JavaBean
```java
public class Shout {

  private String message;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
  
}
```

因为我们现在处理的不是HTTP，所以无法使用Spring的HttpMessageConverter实现将负载转换为Shout对象。Spring 4.0提供了几个消息转换器，作为其消息API的一部分。表18.1描述了这些消息转换器，在处理STOMP消息的时候可能会用到它们。

表18.1　Spring能够使用某一个消息转换器将消息负载转换为Java类型


# 4　为目标用户发送消息
# 5　处理消息异常

# 源码
https://github.com/myitroad/spring-in-action-4/tree/master/Chapter_18