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

package com.huaweicloud.sdk.iot.device.ota;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class OTAPackage implements Parcelable {

    private String url;

    private String version;

    @SerializedName("file_size")
    private Integer fileSize;

    @SerializedName("access_token")
    private String token;

    private Integer expires;

    private String sign;

    public OTAPackage() {
    }


    protected OTAPackage(Parcel in) {
        url = in.readString();
        version = in.readString();
        if (in.readByte() == 0) {
            fileSize = null;
        } else {
            fileSize = in.readInt();
        }
        token = in.readString();
        if (in.readByte() == 0) {
            expires = null;
        } else {
            expires = in.readInt();
        }
        sign = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(version);
        if (fileSize == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(fileSize);
        }
        dest.writeString(token);
        if (expires == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(expires);
        }
        dest.writeString(sign);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OTAPackage> CREATOR = new Creator<OTAPackage>() {
        @Override
        public OTAPackage createFromParcel(Parcel in) {
            return new OTAPackage(in);
        }

        @Override
        public OTAPackage[] newArray(int size) {
            return new OTAPackage[size];
        }
    };

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public double getExpires() {
        return expires;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "OTAPackage{"
                + "url='" + url + '\''
                + ", version='" + version + '\''
                + ", fileSize=" + fileSize
                + ", token='" + token + '\''
                + ", expires=" + expires
                + ", sign='" + sign + '\''
                + '}';
    }
}
