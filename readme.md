![My image](https://raw.github.com/deephacks/westty/master/westty.png)

Westty is a NIO server engine built on a foundation of Weld and Netty, enabling a lightweight CDI 
programming model for building highly optimized web applications. 

* [Netty](http://netty.io)
* [Weld](http://seamframework.org/Weld)
* [tools4j-config](https://github.com/deephacks/tools4j-config)

The mission is to enable modular and extendable application designs that keep the underlying server runtime process 
clean from irrevelant dependencies, encouraging frontend technologies such as [angular.js](http://angularjs.org) or 
[ember.js](http://emberjs.com) maybe with some [Twitter Bootstrap](http://twitter.github.com/bootstrap/) sprayed ontop. 
No servlets, jsp, jsf, jstl or other shenanigans incorporated. Static resources and HTTPS/SSL are preferably delegated 
to a web proxy; such as [nginx](http://wiki.nginx.org/Main) which works really well with westty. 

Westty is _programmatic_ and give users the freedom to implement any mechanism to configure it. The server engine 
starts in around 5 seconds (spent mostly on jar/class scanning) and consumes less than 10mb of JVM heap space.

Westty is cluster aware through [Hazelcast](http://www.hazelcast.com) by choice (using westty-cluster jar) and can be 
deployed as such on private networks or on amazon ec2 using the provided [Apache Whirr](http://whirr.apache.org) recipe.

The following (optional) CDI drop-in modules are provided. All modules are self-sustained with clearly separated 
concerns and does not pollute classpath if not used. 


* [DataSource] (http://docs.oracle.com/javase/6/docs/api/javax/sql/DataSource.html)
* [JPA 2.0] (http://jcp.org/en/jsr/detail?id=317)
* [RestEasy](http://www.jboss.org/resteasy)
* [SockJS](https://github.com/sockjs/sockjs-protocol) and [WebSockets](http://tools.ietf.org/html/rfc6455)
* [Hazelcast](http://www.hazelcast.com)
* [Google Protocol Buffers](https://developers.google.com/protocol-buffers/docs/overview)
* [Bean Validation 1.1] (http://beanvalidation.org/)
* [Quartz Job Scheduling](http://quartz-scheduler.org)
* [Mustache](http://mustache.github.com/)


