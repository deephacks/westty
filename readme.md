![My image](https://raw.github.com/deephacks/westty/master/westty.png)

Westty is a server engine built on a foundation of Weld and Netty, enabling a lightweight CDI 
programming model for building web applications. Westty consumes around 20mb of JVM heap space.

* [Netty](http://netty.io)
* [Weld](http://seamframework.org/Weld)
* [tools4j-config](https://github.com/deephacks/tools4j-config)

Westty have a modular and extendable [SPI](http://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) design and provide the following drop-in modules. These are optional and does not pollute the runtime classpath if not used.

* [DataSource] (http://docs.oracle.com/javase/6/docs/api/javax/sql/DataSource.html)
* [JPA 2.0] (http://jcp.org/en/jsr/detail?id=317)
* [RestEasy](http://www.jboss.org/resteasy)
* [WebSockets](http://tools.ietf.org/html/rfc6455)
* [Google Protocol Buffers](http://code.google.com/p/protobuf)
* [Bean Validation 1.1] (http://beanvalidation.org/)
* [Quartz Job Scheduling](http://quartz-scheduler.org)
