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
import android.os.Message;
import android.util.Log;

import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;


/**
 * 一个tcp客户端，仅用于测试
 */
public class TcpDevice implements Runnable {

    private static final String TAG = "TcpDevice";
    private final String host;
    private final int port;
    private Context mContext;
    private Handler mHandler;
    private String nodeId;
    private String status = "WAIT";

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public TcpDevice(String nodeId, Context mContext, Handler mHandler, String host, int port) {
        this.nodeId = nodeId;
        this.mContext = mContext;
        this.host = host;
        this.port = port;
        this.mHandler = mHandler;
    }

    public void start() {
        new Thread(this, nodeId).start();
    }

    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new SimpleClientInitializer());
            Channel channel = bootstrap.connect(host, port).sync().channel();
            int i = 0;
            //第一条消息发送nodeId,告知网关上线
            channel.writeAndFlush(nodeId);
            while (true) {
                //模拟子设备上线成功，向网关发送信息
                if ("ONLINE".equals(status)) {
                    Thread.sleep(2000);
                    channel.writeAndFlush("子设备" + nodeId + "发的信息:" + (++i));
                }
                //模拟子设备下线
                if ("OFFLINE".equals(status)) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
        } finally {
            group.shutdownGracefully();
        }
    }

    public class SimpleClientHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
            Log.i(TAG, "channelRead0:" + s);
            Message message = Message.obtain();
            message.what = 1;
            message.obj = "子设备" + nodeId + "收到的数据：" + s;
            mHandler.sendMessage(message);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            Log.i(TAG, "exceptionCaught " + cause.toString());
            ctx.close();
        }
    }

    public class SimpleClientInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            Log.i(TAG, "initChannel...");

            pipeline.addLast("decoder", new StringDecoder());
            pipeline.addLast("encoder", new StringEncoder());
            pipeline.addLast("handler", new SimpleClientHandler());
        }
    }
}
