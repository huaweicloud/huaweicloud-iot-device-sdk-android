/*Copyright 2020 Huawei Technologies Co., Ltd
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 *
 * */

package com.huaweicloud.sdk.iot.gateway.demo;


import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * 一个传输字符串数据的tcp server，客户端建链后，首条消息是鉴权消息，携带设备标识nodeId。server将收到的消息通过gateway转发给平台
 */
public class StringTcpServer implements Runnable {

    private static final String TAG = "StringTcpServer";

    private Context mContext;
    private SimpleGateway simpleGateway;
    private final int port = 20001;
    private Handler mHandler;
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    public StringTcpServer(Context mContext, Handler mHandler, SimpleGateway simpleGateway) {
        this.mContext = mContext;
        this.mHandler = mHandler;
        this.simpleGateway = simpleGateway;
    }

    public void start() {
        new Thread(this).start();
    }

    public void run() {

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("decoder", new StringDecoder());
                            ch.pipeline().addLast("encoder", new StringEncoder());
                            ch.pipeline().addLast("handler", new StringHandler());

                            Log.i(TAG, "initChannel:" + ch.remoteAddress());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            Log.i(TAG, "tcp server start......");

            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();

        } catch (Exception e) {
            Log.e(TAG, ExceptionUtil.getAllExceptionStackTrace(e));
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();

            Log.i(TAG, "tcp server close");
        }
    }

    public void close() {
        simpleGateway.close();
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public class StringHandler extends SimpleChannelInboundHandler<String> {
        private Random random = new Random();

        /**
         * @param ctx
         * @param s
         * @throws Exception
         */
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
            Channel incoming = ctx.channel();
            Log.i(TAG, "channelRead0" + incoming.remoteAddress() + " msg :" + s);

            //如果是首条消息,创建session
            Session session = simpleGateway.getSessionByChannel(incoming.id().asLongText());
            if (session == null) {
                String nodeId = s;
                session = simpleGateway.createSession(nodeId, incoming);

                //创建会话失败，拒绝连接
                if (session == null) {
                    Log.i(TAG, "close channel");
                    ctx.close();
                } else {
                    Log.i(TAG, session.getDeviceId() + " ready to go online.");
                    simpleGateway.reportSubDeviceStatus(session.getDeviceId(), "ONLINE");
                }

            } else {

                //网关收到子设备上行数据时，可以以消息或者属性上报转发到平台。
                //实际使用时根据需要选择一种即可，这里为了演示，两种类型都转发一遍

                //上报消息用reportSubDeviceMessage
                DeviceMessage deviceMessage = new DeviceMessage(s);
                deviceMessage.setDeviceId(session.getDeviceId());
                simpleGateway.reportSubDeviceMessage(deviceMessage);

                //报属性则调用reportSubDeviceProperties，属性的serviceId和字段名要和子设备的产品模型保持一致
                ServiceProperty serviceProperty = new ServiceProperty();
                serviceProperty.setServiceId("Battery");
                Map<String, Object> props = new HashMap<>();
                //属性值暂且写死，实际中应该根据子设备上报的进行组装
                props.put("batteryThreshold", random.nextInt(99) + 1);
                serviceProperty.setProperties(props);
                simpleGateway.reportSubDeviceProperties(session.getDeviceId(), Arrays.asList(serviceProperty));

            }

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            Channel incoming = ctx.channel();
            Log.i(TAG, "exceptionCaught:" + incoming.remoteAddress());
            // 当出现异常就关闭连接
            Log.e(TAG, ExceptionUtil.getAllExceptionStackTrace(cause));
            ctx.close();
            simpleGateway.removeSession(incoming.id().asLongText());
        }
    }

}
