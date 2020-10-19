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

package com.huaweicloud.sdk.iot.device.transport;

import android.os.Build;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * 解决5.0以下版本不支持TLSV1.2的问题
 */
public class IoTSSLSocketFactory extends SSLSocketFactory {

    private static final String[] ENABLE_PROTOCOL_ARRAY;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ENABLE_PROTOCOL_ARRAY = new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ENABLE_PROTOCOL_ARRAY = new String[]{"SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"};
        } else {
            ENABLE_PROTOCOL_ARRAY = new String[]{"SSLv3", "TLSv1"};
        }
    }

    /**
     * 传入默认的sslSocketFactory
     */
    private SSLSocketFactory sslSocketFactory;

    public IoTSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    /**
     * 不做修改，使用传入sslSocketFactory的
     */
    @Override
    public String[] getDefaultCipherSuites() {
        return sslSocketFactory.getDefaultCipherSuites();
    }

    /**
     * 不做修改，使用传入sslSocketFactory的
     */
    @Override
    public String[] getSupportedCipherSuites() {
        return sslSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        Socket retSocket = sslSocketFactory.createSocket(s, host, port, autoClose);
        setTLsOnSocket(retSocket);
        return retSocket;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        Socket retSocket = sslSocketFactory.createSocket(host, port);
        setTLsOnSocket(retSocket);
        return retSocket;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        Socket retSocket = sslSocketFactory.createSocket(host, port, localHost, localPort);
        setTLsOnSocket(retSocket);
        return retSocket;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        Socket retSocket = sslSocketFactory.createSocket(host, port);
        setTLsOnSocket(retSocket);
        return retSocket;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        Socket retSocket = sslSocketFactory.createSocket(address, port, localAddress, localPort);
        setTLsOnSocket(retSocket);
        return retSocket;
    }

    @Override
    public Socket createSocket() throws IOException {
        Socket retSocket = sslSocketFactory.createSocket();
        setTLsOnSocket(retSocket);
        return retSocket;
    }

    private void setTLsOnSocket(Socket socket) {
        if (socket != null && socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledProtocols(ENABLE_PROTOCOL_ARRAY);
        }
    }
}
