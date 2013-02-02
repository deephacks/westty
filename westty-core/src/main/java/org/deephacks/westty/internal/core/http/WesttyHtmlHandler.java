package org.deephacks.westty.internal.core.http;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.enterprise.inject.Alternative;

import org.deephacks.westty.spi.WesttyHttpHandler;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

@Alternative
class WesttyHtmlHandler extends WesttyHttpHandler {
    private final String staticRootPath;

    public WesttyHtmlHandler(File htmlDir) {
        this.staticRootPath = htmlDir.getAbsolutePath();
    }

    @Override
    public boolean accept(String uri) {
        return true;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent event, HttpRequest request)
            throws Exception {
        // Allow only GET methods.
        if (request.getMethod() != HttpMethod.GET) {
            sendHttpResponse(ctx, request, new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.FORBIDDEN));
            return;
        }
        handleStaticContent(ctx, request);

    }

    private static final String READ_ONLY = "r";

    private void handleStaticContent(ChannelHandlerContext ctx, HttpRequest req) throws IOException {
        String uri;
        if ("/".equals(req.getUri())) {
            uri = "/index.html";
        } else {
            uri = req.getUri();
        }

        // File is closed by DefaultFileRegion.releaseExternalResources
        RandomAccessFile file = new RandomAccessFile(staticRootPath + uri, READ_ONLY);
        final DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK);
        HttpHeaders.setContentLength(response, file.length());
        ctx.getChannel().write(response);
        final DefaultFileRegion fileRegion = new DefaultFileRegion(file.getChannel(), 0,
                file.length());
        final ChannelFuture future = ctx.getChannel().write(fileRegion);
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(final ChannelFuture future) {
                fileRegion.releaseExternalResources();
            }
        });
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
        // Generate an error page if response status code is not OK (200).
        if (res.getStatus().getCode() != 200) {
            res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(),
                    CharsetUtil.UTF_8));
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.getChannel().write(res);
        if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
