Chapter 2 Examples
==================
This folder contains example code for chapter 2 of Spring in Action, 4th Edition.
The samples are split across multiple directories:

 * stereo-autoconfig  : Examples illustrating component-scanning and auto-wiring for section 2.2.
 * stereo-javaconfig  : Examples illustrating Java configuration for section 2.3.
 * stereo-xmlconfig   : Examples illustrating XML configuration for section 2.4.
 * stereo-mixedconfig : Examples illustrating mixed XML and Java configuration for section 2.5.

Note that because the examples evolve throughout the chapter and the book's text sometimes
shows multiple ways of accomplishing a goal, not all variations of the code in the book will
be represented in these samples. You are invited to use this source code as a starting point
and experiment using the variations presented in the text.

# 1. Spring配置的可选方案
- 在XML中进行显式配置；
- 在Java中进行显式配置；
- 隐式的bean发现机制和自动装配。

# 2. 自动化装配bean
### Spring从两个角度来实现自动化装配：
组件扫描（component scanning）：Spring会自动发现应用上下文中所创建的bean。
自动装配（autowiring）：Spring自动满足bean之间的依赖。

## 2.1 创建可被发现的bean
使用了@Component注解。这个简单的注解表明该类会作为组件类，并告知Spring要为这个类创建bean。

组件扫描默认是不启用的。我们还需要显式配置一下Spring，从而命令它去寻找带有@Component注解的类，并为其创建bean。

### 启用组件扫描
- 基于Java @ComponentScan注解启用了组件扫描
```
@Configuration
@ComponentScan(basePackages="soundsystem","video")
public class CDPlayerConfig {
}
```
如果没有其他配置的话，@ComponentScan默认会扫描与配置类相同的包。（通过一个空标记接口（remark interface）来这这个工作，是一个很好的实践。）

## 2.2 设置组件扫描的基础包
上面的例子中，所设置的基础包是以String类型表示的。我认为这是可以的，但这种方法是类型不安全（not type-safe）的。除了将包设置为简单的String类型之外，@ComponentScan还提供了另外一种方法，那就是将其指定为包中所包含的类或接口：
```
@Configuration
@ComponentScan(basePackageClasses = {CDPlayer.class})
public class CDPlayerConfig {
}
```

basePackages属性被替换成了basePackageClasses。同时，我们不是再使用String类型的名称来指定包，为basePackageClasses属性所设置的数组中包含了类。这些类所在的包将会作为组件扫描的基础包。

- 通过XML启用组件扫描
```
<context:component-scan base-package="soundsystem" />
```

## 2.3 通过为bean添加注解实现自动装配
为了声明要进行自动装配，我们可以借助Spring的@Autowired注解.
@Autowired注解不仅能够用在构造器上，还能用在属性的Setter方法上。
```
@Autowired
public void setCompactDisc(CompactDisc cd) {
  this.cd=cd;
}
```

如果没有匹配的bean，那么在应用上下文创建的时候，Spring会抛出一个异常。为了避免异常的出现，你可以将@Autowired的required属
性设置为false：
```
@Autowired(required = false)
public CDPlayer(CompactDisc cd){
  this.cd=cd;
}
```

# 3. 通过Java代码装配bean
你想要将第三方库中的组件装配到你的应用中，在这种情况下，是没有办法在它的类上添加@Component和@Autowired注解的，因此就不能使用自动化装配的方案了。 在这种情况下，你必须要采用显式装配的方式。在进行显式配置的时候，有两种可选方案：Java和XML。

JavaConfig是配置代码。这意味着它不应该包含任何业务逻辑，JavaConfig也不应该侵入到业务逻辑代码之中。尽管不是必须的，但通常会将JavaConfig放到单独的包中，使它与其他的应用程序逻辑分离开来，这样对于它的意图就不会产生困惑了。

- 声明简单的bean
```
@Bean
public CompactDisc sgtPeppers() {
  return new SgtPeppers();
}
```

- 借助JavaConfig实现注入
```
@Bean
public CDPlayer cdPlayer(CompactDisc compactDisc) {
  return new CDPlayer(compactDisc);
}
```
通过这种方式引用其他的bean通常是最佳的选择，因为它不会要求将CompactDisc声明到同一个配置类之中。在这里甚至没有要求CompactDisc必须要在JavaConfig中声明，实际上它可以通过组件扫描功能自动发现或者通过XML来进行配置。

- 使用方法替代构造器的注入
需要提醒的是，我们在这里使用CDPlayer的构造器实现了DI功能，但是我们完全可以采用其他风格的DI配置。比如说，如果你想通过Setter方法注入CompactDisc的话，那么代码看起来应该是这样的：
```
@Bean
public CDPlayer cdPlayer(CompactDisc compactDisc) {
  CDPlayer cdPlayer = new CDPlayer(compactDisc);
  cdPlayer.setCompactDisc(compactDisc);
  return cdPlayer;
}
```


# 4. 通过XML装配bean
- 声明一个简单的`<bean>`
```
<bean class="soundsystem.SgtPeppers"/>
```

- 借助构造器注入初始化bean
```
<bean id="cdPlayer" class="soundsystem.CDPlayer">
        <constructor-arg ref="compactDisc"/>
</bean>
```

- 使用方法替代构造器的注入
```
<bean id="cdPlayer"  class="soundsystem.CDPlayer">
        <property name="compactDisc" ref="compactDisc"/>
</bean>
```
`<property>`元素为属性的Setter方法所提供的功能与`<constructor-arg>`元素为构造器所提供的功能是一样的。

- 将字面量注入到属性中
有Java对象如下：
```
public class BlankDisc implements CompactDisc {

  private String title;
  private String artist;
  private List<String> tracks;

  public BlankDisc(String title, String artist, List<String> tracks) {
    this.title = title;
    this.artist = artist;
    this.tracks = tracks;
  }

  public void play() {
    System.out.println("Playing " + title + " by " + artist);
    for (String track : tracks) {
      System.out.println("-Track: " + track);
    }
  }

}
```
可用以下xml将字面值（`String`、`List<String>`）注入到属性中：
```
<bean id="compactDisc"
      class="soundsystem.properties.BlankDisc">
  <property name="title" value="Sgt. Pepper's Lonely Hearts Club Band" />
  <property name="artist" value="The Beatles" />
  <property name="tracks">
    <list>
      <value>Sgt. Pepper's Lonely Hearts Club Band</value>
      <value>With a Little Help from My Friends</value>
      <value>Lucy in the Sky with Diamonds</value>
      <value>Getting Better</value>
      <value>Fixing a Hole</value>
      <value>She's Leaving Home</value>
      <value>Being for the Benefit of Mr. Kite!</value>
      <value>Within You Without You</value>
      <value>When I'm Sixty-Four</value>
      <value>Lovely Rita</value>
      <value>Good Morning Good Morning</value>
      <value>Sgt. Pepper's Lonely Hearts Club Band (Reprise)</value>
      <value>A Day in the Life</value>
    </list>
  </property>
</bean>
```

# 5. 导入和混合配置
## 5.1 在JavaConfig中引用XML配置
- Java配置
```
@Configuration
public class CDPlayerConfig {

  @Bean
  public CDPlayer cdPlayer(CompactDisc compactDisc) {
    return new CDPlayer(compactDisc);
  }

}
```

- XML配置
```
<bean id="compactDisc"
      class="soundsystem.BlankDisc"
      c:_0="Sgt. Pepper's Lonely Hearts Club Band"
      c:_1="The Beatles">
  <constructor-arg>
    <list>
      <value>Sgt. Pepper's Lonely Hearts Club Band</value>
      <value>With a Little Help from My Friends</value>
      <value>Lucy in the Sky with Diamonds</value>
      <value>Getting Better</value>
      <value>Fixing a Hole</value>
      <!-- ...other tracks omitted for brevity... -->
    </list>
  </constructor-arg>
</bean>
```
> 以上xml配置中使用了`c命名空间`,这里_0,_1相当与第一个参数和第二个参数。

- 在Java配置中引入XML配置
两个bean——配置在JavaConfig中的CDPlayer以及配置在XML中BlankDisc——都会被加载到Spring容器之中。代码如下：
```
@Configuration
@Import(CDPlayerConfig.class)
@ImportResource("classpath:cd-config.xml")
public class SoundSystemConfig {

}
```
@Import将两个配置类组合在一起;
@ImportResource可在JavaConfig中引入XML配置；
以上代码是利用一个新的Java配置类，引入了已有的Java配置类以及XML配置文件。当然也可以在已有的Java配置类中直接引入XML配置文件。

## 5.2 在XML配置中引用JavaConfig
- Java配置
```
@Configuration
public class CDConfig {
  @Bean
  public CompactDisc compactDisc() {
    return new SgtPeppers();
  }
}
```

- XML配置
```
<bean id="cdPlayer"
      class="soundsystem.CDPlayer"
      c:cd-ref="compactDisc" />
```

- 在XML配置中引入Java配置
```
<bean class="soundsystem.CDConfig" />

<bean id="cdPlayer"
      class="soundsystem.CDPlayer"
      c:cd-ref="compactDisc" />
```
以上代码是在已有的XML配置文件中引入Java配置类，当然可以建立一个新的XML引入已有的XML配置文件和已有的Java配置类，如：
```
<bean class="soundsystem.CDConfig"/>
<import resource="cdplayer-config.xml"/>

```
XML配置文件应用已有XML配置文件使用`<import resource>`标签。

# 注意
不管使用JavaConfig还是使用XML进行装配，我通常都会创建一个根配置（root configuration），也就是这里展现的这样，这个配置会将两个或更多的装配类和/或XML文件组合起来。我也会在根配置中启用组件扫描（通过`<context:component-scan>`或@ComponentScan）。
