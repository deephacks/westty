![My image](https://raw.github.com/deephacks/westty/master/westty.png)

Westty is a NIO server engine built on a foundation of Weld and Netty, enabling a lightweight CDI 
programming model for building web applications. 

* [Netty](http://netty.io)
* [Weld](http://seamframework.org/Weld)
* [tools4j-config](https://github.com/deephacks/tools4j-config)

The mission of Westty is to provide a modular and extendable server that keep your runtime clean 
from irrevelant dependencies. No servlets, jsp, jsf, jstl or other shenanigans incorporated. 
Westty starts in around 5 seconds (spent mostly on jar/class scanning) and consumes less than 10mb of JVM heap space. 

that provide the following CDI drop-in modules. 
These are optional and does not pollute the runtime classpath if not used. All modules are 
self-sustained with clearly separated concerns.

* [DataSource] (http://docs.oracle.com/javase/6/docs/api/javax/sql/DataSource.html)
* [JPA 2.0] (http://jcp.org/en/jsr/detail?id=317)
* [RestEasy](http://www.jboss.org/resteasy)
* [WebSockets](http://tools.ietf.org/html/rfc6455)
* [Google Protocol Buffers](https://developers.google.com/protocol-buffers/docs/overview)
* [Bean Validation 1.1] (http://beanvalidation.org/)
* [Quartz Job Scheduling](http://quartz-scheduler.org)
* [Mustache](http://mustache.github.com/)


