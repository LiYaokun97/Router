# Router 



这里只实现了activity的跳转，未实现fragment等的路由，下图以activity为例子，说明了整体的思路。

在编译期，通过注解处理技术，对自定义注解@Destination进行解析，生成对应的子工程映射表，在大型项目中，还必须考虑到多模块，必须使用字节码技术将所有的子工程映射表收集，得到总映射表。

运行期，在有跳转请求时，先加载总映射表（如果为加载的话）然后找到对应类，解析参数，最后实现跳转。



![design](/readmeImage/design.jpg)



## Android APT

安卓AOP三剑客: APT, AspectJ, Javassist



![Android APT 技术浅谈_java](https://s2.51cto.com/images/blog/202109/09/0c28d76f56544caf3f513c6f02a42e56.jpeg?x-oss-process=image/watermark,size_16,text_QDUxQ1RP5Y2a5a6i,color_FFFFFF,t_30,g_se,x_10,y_10,shadow_20,type_ZmFuZ3poZW5naGVpdGk=/format,webp/resize,m_fixed,w_1184)



APT(Annotation Processing Tool 的简称)，可以在代码编译期解析注解，并且生成新的 Java 文件，减少手动的代码输入。现在有很多主流库都用上了 APT，比如 Dagger2, ButterKnife, EventBus3 等

代表框架：

- DataBinding
- Dagger2
- ButterKnife
- EventBus3
- DBFlow





## Reference

胡飞洋. (2022, November 8). *“终于懂了” 系列：组件化框架 ARouter 完全解析（二）APT技术*. https://cloud.tencent.com/developer/article/2154577

