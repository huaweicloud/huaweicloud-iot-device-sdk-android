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

package com.huaweicloud.sdk.iot.device.demo;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;

import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;

import android.util.Log;

import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.text.TextUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;

import android.content.Context;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class CommonUtils {

    private static final String TAG = "CommonUtils";

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder("");
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            String h = Integer.toHexString(b);
            if (h.length() < 2) {
                sb.append(0);
            }
            sb.append(h);
        }
        return sb.toString();
    }

    /**
     * 获取文件的Hash值
     *
     * @param fileName
     * @return
     */
    public static String getHash(String fileName) {

        if (TextUtils.isEmpty(fileName)) {
            return null;
        }

        File file = new File(fileName);

        MessageDigest messageDigest = null;
        FileInputStream fileInputStream = null;
        byte[] bytes = new byte[1024];
        int length;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            fileInputStream = new FileInputStream(file);
            while ((length = fileInputStream.read(bytes, 0, 1024)) != -1) {
                messageDigest.update(bytes, 0, length);
            }

        } catch (Exception e) {
            Log.e(TAG, "getHash: " + ExceptionUtil.getBriefStackTrace(e));
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "getHash: FileInputStream close failed!");
                }
            }
        }

        return bytesToHexString(messageDigest.digest());
    }

    public static KeyStore getKeyStore(Context context, String keyPassword, String pemFileName, String keyFileName) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {

        Certificate cert = null;
        InputStream inputStream = context.getAssets().open(pemFileName);
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = cf.generateCertificate(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        KeyPair keyPair = null;
        InputStream keyInput = context.getAssets().open(keyFileName);
        try {
            PEMParser pemParser = new PEMParser(new InputStreamReader(keyInput, StandardCharsets.UTF_8));
            Object object = pemParser.readObject();
            BouncyCastleProvider provider = new BouncyCastleProvider();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(provider);
            if (object instanceof PEMEncryptedKeyPair) {
                PEMDecryptorProvider decryptionProvider = new JcePEMDecryptorProviderBuilder().setProvider(provider).build(keyPassword.toCharArray());
                PEMKeyPair keypair = ((PEMEncryptedKeyPair) object).decryptKeyPair(decryptionProvider);
                keyPair = converter.getKeyPair(keypair);
            } else {
                keyPair = converter.getKeyPair((PEMKeyPair) object);
            }
        } finally {
            if (keyInput != null) {
                keyInput.close();
            }
        }
        if (keyPair == null) {
            Log.e(TAG, "keystoreCreate: keyPair is null");
            return null;
        }

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("certificate", cert);
        keyStore.setKeyEntry("private-key", keyPair.getPrivate(), keyPassword.toCharArray(),
                new Certificate[]{cert});

        return keyStore;
    }
}
