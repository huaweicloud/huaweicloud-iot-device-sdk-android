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

package com.huaweicloud.sdk.iot.device.utils;

import android.content.Context;
import android.util.Log;

import com.huaweicloud.sdk.iot.device.client.ClientConf;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;


/**
 * Provides an IoT utility class.
 */
public class IotUtil {


    private static final String TLS_V_1_2 = "TLSv1.2";
    private static final String TAG = "IotUtil";
    private static AtomicLong requestId = new AtomicLong(0);

    /**
     * Obtains the request ID from a topic.
     *
     * @param topic Indicates the topic.
     * @return Returns the request ID.
     */
    public static String getRequestId(String topic) {
        String[] tmp = topic.split("request_id=");
        return tmp[1];
    }

    /**
     * Obtains the node ID from a device ID.
     *
     * @param deviceId Indicates the device ID.
     * @return Returns the node ID.
     */
    public static String getNodeIdFromDeviceId(String deviceId) {

        try {
            return deviceId.substring(deviceId.indexOf("_") + 1);
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
            return null;
        }

    }

    /**
     * Constructs a response topic based on a request topic.
     *
     * @param topic Indicates the request topic.
     * @return Returns the response topic.
     */
    public static String makeRspTopic(String topic) {

        try {
            String[] tmp = topic.split("request_id");
            return tmp[0] + "response/" + "request_id" + tmp[1];
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
            return null;
        }
    }

    /**
     * Obtains the current timestamp.
     *
     * @return Returns the timestamp.
     */
    public static String getTimeStamp() {

        String msgTimestampFormat = "yyyyMMdd'T'HHmmss'Z'";

        SimpleDateFormat df = new SimpleDateFormat(msgTimestampFormat);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date(System.currentTimeMillis()));

    }

    /**
     * Generates a request ID.
     *
     * @return Returns the request ID.
     */
    public static String generateRequestId() {


        return Long.toString(requestId.incrementAndGet());

    }

    /**
     * Encrypts a string using HMAC-SHA256.
     *
     * @param str Indicates the string.
     * @param timeStamp Indicates a timestamp.
     * @return Returns the encrypted string.
     */
    public static String sha256_mac(String str, String timeStamp) {
        String passWord = null;
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(timeStamp.getBytes("UTF-8"), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] bytes = sha256Hmac.doFinal(str.getBytes("UTF-8"));
            passWord = byteArrayToHexString(bytes);
        } catch (Exception e) {
            Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
        }
        return passWord;
    }

    /**
     * Converts a byte array to a hexadecimal string.
     *
     * @param b Indicates the byte array.
     * @return Returns a hexadecimal string.
     */
    public static String byteArrayToHexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        String temp;
        for (int i = 0; b != null && i < b.length; i++) {
            temp = Integer.toHexString(b[i] & 0XFF);
            if (temp.length() == 1) {
                sb.append('0');
            }
            sb.append(temp);
        }
        return sb.toString().toLowerCase(Locale.US);
    }


    private static TrustManager[] getTrustManager(Context mContext) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("BKS");
        keyStore.load(mContext.getAssets().open("DigiCertGlobalRootCA.bks"), null);

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        return trustManagers;
    }

    private static SSLContext getSSLContextWithKeystore(Context mContext, KeyStore keyStore, String keyPassword) throws Exception {
        SSLContext sslContext = SSLContext.getInstance(TLS_V_1_2);

        KeyManagerFactory managerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        managerFactory.init(keyStore, keyPassword.toCharArray());
        sslContext.init(managerFactory.getKeyManagers(), getTrustManager(mContext), null);
        return sslContext;
    }

    /**
     * Obtains the SSL context based on the client configuration.
     *
     * @param clientConf Indicates the client configuration.
     * @return Returns the SSL context.
     * @throws Exception Throws this exception if any error occurs.
     */
    public static SSLContext getSSLContext(Context mContext, ClientConf clientConf) throws Exception {


        if (clientConf.getKeyStore() != null) {
            return getSSLContextWithKeystore(mContext, clientConf.getKeyStore(), clientConf.getKeyPassword());
        } else {
            SSLContext sslContext = SSLContext.getInstance(TLS_V_1_2);
            sslContext.init(null, getTrustManager(mContext), new SecureRandom());
            return sslContext;
        }
    }

}
