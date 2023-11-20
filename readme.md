# Router 



这里只实现了activity的跳转，未实现fragment等的路由，下图以activity为例子，说明了整体的思路。

在编译期，通过注解处理技术，对自定义注解@Destination进行解析，生成对应的子工程映射表，在大型项目中，还必须考虑到多模块，必须使用字节码技术将所有的子工程映射表收集，得到总映射表。

运行期，在有跳转请求时，先加载总映射表（如果为加载的话）然后找到对应类，解析参数，最后实现跳转。



![design](C:\Users\liyk1\Desktop\Android\github\Router\readmeImage\design.jpg)