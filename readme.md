# Router 



这里只实现了activity的跳转，未实现fragment等的路由，下图以activity为例子，说明了整体的思路。

在编译期，通过注解处理技术，对自定义注解@Destination进行解析，生成对应的子工程映射表，在大型项目中，还必须考虑到多模块，必须使用字节码技术将所有的子工程映射表收集，得到总映射表。

运行期，在有跳转请求时，先加载总映射表（如果为加载的话）然后找到对应类，解析参数，最后实现跳转。



![design](/readmeImage/design.jpg)





# **APT介绍**

## APT的理解

现在有很多主流库都用上了 APT，比如 Dagger2, ButterKnife, EventBus3 等

代表框架：

- DataBinding

- Dagger2

- ButterKnife

- EventBus3

- DBFlow

  

**APT**(Annotation Processing Tool)，即 **注解处理器**，是javac中提供的**编译时扫描和处理注解**的工具，它对源代码文件进行检测找出其中的注解，然后使用注解进行额外的处理。

**注解**就像是一个[标签](https://cloud.tencent.com/developer/techpedia/1416?from_column=20065&from=20065)，有很多类型，可以贴在某些元素上面进行标记，并且标签上可以写一些信息。APT就是用来处理标签的工具，在编译开始后，可以拿到自己所关心的类型的所有标签，然后根据标签信息和被标记的元素信息，做一些事情。做那些事呢，这就看你如何写APT了，你让他干啥他就干啥，通常都是会生成一些帮助类——帮助完成你的目的的类。后面无论对这种标签的使用是增加、减少了，**每次编译**都会重新走这一过程，而上一次的处理结果会被清空。

宏观上理解，APT就是javac提供给开发者在编译时处理注解的一种技术；微观上，具体到实例中就是指 继承自javax.annotation.processing.**AbstractProcessor** 的实现类，即一个处理特定注解的处理器。（下文提到的APT都是宏观上理解，具体的处理器简称为Processor）

**那不使用APT能否完成目的呢？** 也是可以的，毕竟APT就是为了帮助我完成目的，那我自己肯定也是可以完成目的的，只不过有了APT会很省事。例如，上篇提到的帮助类，目的就是为了收集路由元信息（路由目标类class），我们如果不使用ARouter，那么就需要自己定义一个moduleA、moduelB共同依赖的muduleX，把需要进行跳转的XXXActivity这些类的class手工写代码保存到muduleX的Map中，key可以用XXXActivity的类名，value就是XXXActivity.class，这样moduleA、moduelB之间发起跳转时 就通过想要跳转的Activity的类名 从muduleX的Map中获取目标Activity的class，这样也是能完成 无相互依赖的moduleA、moduelB之前进行页面跳转的。

而使用了APT，只要使用注解进行标记即可，无论使用者怎么标记，**每次编译时都由APT统一处理，不会出错、也不担心有遗漏**。

通常用到APT技术的都是像ARouter这样的通用能力框架：

- 提供定义好的注解，例如@Route
- 提供简洁的API接口，例如ARouter.getInstance().build("/module/1").navigation()

这样上层业务使用极为方便，只需要使用注解进行标记，然后调用API即可。**信息如何收集和存储、如何寻找和使用，框架使用者是不用关心的。**

APT还有两个特点：

- 获取注解及生成代码都是在代码编译时候完成的，相比反射在运行时处理注解大大提高了程序性能。

- 注意APT并不能对源文件进行修改，只能获取注解信息和被注解对象的信息，然后做一些自定义的处理，例如生成java类。

  

## **APT的原理**

我们先来看下Java的编译过程：

![img](/readmeImage/java_compile.png)



可以看到在Java源码到class文件之间，需要经过**注解处理器**的处理，注解处理器生成的代码也同样会经过这一过程，最终一起生成class文件。在[Android](https://cloud.tencent.com/developer/techpedia/1819?from_column=20065&from=20065)中，class文件还会被打进Dex文件中，最后生成APK文件。

注解处理器的执行是在编译的初始阶段，并且会有多个processor（查看所有注册的processor：intermediates/annotation_processor_list/debug/annotationProcessors.json）。

那么我们自定义的注解处理器，如何注册进去呢？又如何实现一个注解处理器呢？

来看一个简单的实例：

```javascript
@AutoService(Processor.class) //把 TestProcessor注册到编译器中
@SupportedAnnotationTypes({"com.hfy.test_annotations.TestAnnotation"})//TestProcessor 要处理的注解
@SupportedSourceVersion(SourceVersion.RELEASE_8)//设置jdk环境为java8
//@SupportedOptions() //一些支持的可选配置项
public class TestProcessor extends AbstractProcessor {
    public static final String ACTIVITY = "android.app.Activity";
    //Filer 就是文件流输出路径，当我们用AbstractProcess生成一个java类的时候，我们需要保存在Filer指定的目录下
    Filer mFiler;
    //类型 相关的工具类。当process执行的时候，由于并没有加载类信息，所以java文件中的类信息都是用element来代替了。
    //类型相关的都被转化成了一个叫TypeMirror，其getKind方法返回类型信息，其中包含了基础类型以及引用类型。
    Types types;
    //Elements 获取元素信息的工具，比如说一些类信息继承关系等。
    Elements elementUtils;
    //来报告错误、警告以及提示信息
    //用来写一些信息给使用此注解库的第三方开发者的
    Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnv.getFiler();
        types = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        if (annotations == null || annotations.size() == ){
            return false;
        }
        //获取所有包含 @TestAnnotation 注解的元素
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(TestAnnotation.class);

        //每个Processor的独自的逻辑，其他的写法一般都是固定的
        parseAnnotation(elements)

        return true;
    }

    //解析注解并生成java文件
    private boolean parseAnnotation(Set<? extends Element> elements) {
       ...
    }

}
```

复制

上面的TestProcessor就是一个典型的注解处理器，继承自javax.annotation.processing.**AbstractProcessor**。注意到TestProcessor重写了init()和process()两个方法，并且添加了几个注解。

几个注解是**处理器的注册和配置** ：

1. 使用注解`@AutoService`进行注册，这样在编译这段就会执行这个处理器了。需要依赖com.google.auto.service:auto-service:1.0-rc4' 才能使用@AutoService
2. 使用注解`@SupportedAnnotationTypes` 设置 TestProcessor 要处理的注解，要使用目标注解全类名
3. 使用注解`@SupportedSourceVersion`设置支持的Java版本、使用注解@SupportedOptions()配置一些支持的可选配置项

重写的两个方法：init()、process()是Processor的初始化和处理过程:

1. init()

    方法是初始化处理器，每个注解处理器被初始化的时候都会被调用，通常是这里使用 处理环境-

   ProcessingEnvironment

    获取一些工具实例，如上所示是一般通用的写法：

   - **mFiler**，就是文件流输出路径，当我们用AbstractProcess生成一个java类的时候，我们需要保存在Filer指定的目录下
   - **types**，类型 相关的工具类。当process执行的时候，由于并没有加载类信息，所以java文件中的类信息都是用element来代替了。类型相关的都被转化成了一个叫TypeMirror，其getKind方法返回类型信息，其中包含了基础类型以及引用类型。
   - **elementUtils**，获取元素信息的工具，比如说一些类信息继承关系等。
   - **messager**，来报告错误、警告以及提示信息，用来写一些信息给使用此注解库的第三方开发者的

2. process()

    方法，注解处理器实际处理方法，在这里写处理注解的代码，以及生成Java文件。它有两个参数：

   - **annotations**，是@SupportedAnnotationTypes声明的注解 和 未被其他Processor消费的注解的子集，也就是剩下的且是本Processor关注的注解，类型是**TypeElement**
   - **roundEnvironment**，有关当前和之前处理器的环境信息，可以让你查询出包含特定注解的被注解元素
   - 返回值，true表示@SupportedAnnotationTypes中声明的注解 由此 Processor 消费掉，不会传给下个Processor。注解不断向下分发，每个processor都可以决定是否消费掉自己声明的注解。

**在编译流程进入Processor前，APT会对整个Java源文件进行扫描，这样就会获取到 所有添加了的注解和对应被注解的类。注解和被注解的类，一起被视为一个元素，即TypeElement，就是process()方法参数annotations的数据类型。通过TypeElement，我们可以获取注解的所有信息、被注解类的所有信息，这样就可以根据这些信息来生成 我们需要的帮助类了**。

> 这里重点是理解Processor的原理，关于注解和Element的知识这里不过多介绍，可自行了解。

到这里，Processor的工作流程我们了解了，并且其 定义、配置、注册 这些都是固定的写法，一般无需过多关注。**最重要的就是 process()方法的实现：拿到所有关注的注解元素后，就是每个Processor的独自的逻辑——解析注解并生成需要的java文件。**



## Reference

胡飞洋. (2022, November 8). *“终于懂了” 系列：组件化框架 ARouter 完全解析（二）APT技术*. https://cloud.tencent.com/developer/article/2154577

