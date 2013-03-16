package org.deephacks.westty.test;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.impl.DefaultVertx;
import org.vertx.java.deploy.Container;
import org.vertx.java.deploy.Verticle;
import org.vertx.java.deploy.impl.VerticleManager;

public class Vertx extends Verticle {
    public static void main(String[] args) throws Exception {
        Vertx v = new Vertx();
        DefaultVertx dv = new DefaultVertx();
        v.setVertx(dv);
        VerticleManager m = new VerticleManager(dv);
        Container c = new Container(m);
        c.deployModule("main");
        v.setContainer(c);
        v.start();
        Thread.sleep(100000);
    }

    public void start() {
        System.out.println("start");
        HttpServer s = vertx.createHttpServer().websocketHandler(new Handler<ServerWebSocket>() {
            public void handle(final ServerWebSocket ws) {
                System.out.println("sjkgh");
                if (ws.path.equals("/myapp")) {
                    ws.dataHandler(new Handler<Buffer>() {
                        public void handle(Buffer data) {
                            ws.writeTextFrame(data.toString()); // Echo it back
                        }
                    });
                } else {
                    ws.reject();
                }
            }
        }).requestHandler(new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest req) {
                if (req.path.equals("/"))
                    req.response.sendFile("websockets/ws.html"); // Serve the html
            }
        }).listen(8080);
    }

}
