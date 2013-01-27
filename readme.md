![My image](https://raw.github.com/deephacks/westty/master/westty.png)

Westty is a server engine built on a foundation of Weld and Netty, enabling a lightweight CDI 
programming model for building web applications using REST and WebSockets.

* [Netty](http://netty.io)
* [Weld](http://seamframework.org/Weld)
* [RestEasy](http://www.jboss.org/resteasy)
* [The WebSocket Protocol](http://tools.ietf.org/html/rfc6455)
* [tools4j-config](https://github.com/deephacks/tools4j-config)

Westty have a modular and extendable [SPI](http://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) design and provide the following optional drop-in modules. No classpath pollution.

* [DataSource] (http://docs.oracle.com/javase/6/docs/api/javax/sql/DataSource.html)
* [JPA 2.0] (http://jcp.org/en/jsr/detail?id=317)
* [google-protobuf](http://code.google.com/p/protobuf)
* [Bean Validation 1.1] (http://beanvalidation.org/)
* [Quartz job scheduling](http://quartz-scheduler.org)
