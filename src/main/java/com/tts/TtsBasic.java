package com.tts;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.protobuf.InvalidProtocolBufferException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import rokid.open.speech.Auth;
import rokid.open.speech.v1.Tts;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * tts demo
 * 参考文档 https://developer-dev.rokid.com/docs/3-ApiReference/openvoice-api.html
 * @author mashuangwei
 * @create 2018-01-02 11:21
 **/


public class TtsBasic extends WebSocketClient {
    private volatile boolean finishFlag = false;
    boolean connectFlag = false;
    /**
     *  tts服务返回的语音流数据，在此演示把语音流文件保存在本地
     */
    String filename = System.getProperty("user.dir") + "/src/main/resources/files/" + new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date()) + ".opu2";

    public TtsBasic(URI serverURI) {
        super(serverURI);
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        SSLSocketFactory factory = sslContext.getSocketFactory();
        try {
            this.setSocket(factory.createSocket());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init(String key, String deviceTypeId, String secret, String deviceId) {
        long time = System.currentTimeMillis();
        String beforeSign = "key=" + key + "&device_type_id=" + deviceTypeId + "&device_id=" + deviceId + "&service=tts&version=1.0&time=" + time + "&secret=" + secret;
        String sign = getMD5(beforeSign);

        this.connect();
        for (int i = 0; i < 15; i++) {
            CustomSleep.sleep(0.6);
            if (connectFlag) {
                break;
            }
        }
        Auth.AuthRequest authRequest = Auth.AuthRequest.newBuilder().setDeviceId(deviceId).setDeviceTypeId(deviceTypeId).setKey(key).setService("tts").setVersion("1.0").setTimestamp("" + time).setSign(sign).build();
        this.send(authRequest.toByteArray());
        CustomSleep.sleep(1);

    }

    public String getMD5(String sign) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(sign.getBytes());
        String md5Value = Hashing.md5().hashString(sign, Charsets.UTF_8).toString();
        System.out.println("sign md5 is " + md5Value);
        return md5Value;
    }

    /**
     *
     * @param text: 需要转换的text文本
     * @param declaimer: 发音者，如"zh","zhangsan","rose" , 目前可用的是zh、xmly
     * @param codec: 语音流的编码，目前支持PCM，OPU，OPU2。
     */
    public void sendTts(String text, String declaimer, String codec) {
        Tts.TtsRequest ttsRequest = Tts.TtsRequest.newBuilder().setId(new Random().nextInt()).setDeclaimer(declaimer).setCodec(codec).setText(text).build();
        this.send(ttsRequest.toByteArray());
        // 这边的sleep 处理只是为了模仿同步生成语音文件，把tts返回的语音流写入文件，这里只是参考，开发者可以使用异步方式另行处理
        for (int i = 0; i < 8; i++) {
            if (finishFlag) {
                finishFlag = false;
                break;
            }
            CustomSleep.sleep(0.5);
        }
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.err.println("Connected");
        connectFlag = true;
    }

    @Override
    public void onMessage(String message) {
        System.err.println("got: " + message);
    }

    @Override
    public void onMessage(ByteBuffer message) {
        byte[] byteMessage = message.array();
        Auth.AuthResponse authResponse = null;
        Tts.TtsResponse ttsResponse = null;

        // 第一次拿到的请求结果是认证结果，这边根据长度做了一个判断，其实是认证长度恰好为2的缘故，这边是偷懒处理了一下，第三方开发者可以根据第一次请求返回是认证结果来做判断
        if (byteMessage.length == 2) {
            try {
                authResponse = Auth.AuthResponse.parseFrom(byteMessage);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }

            System.err.println("Auth Result is " + authResponse.getResult());
            if (authResponse.getResult().equals(Auth.AuthErrorCode.AUTH_FAILED)) {
                System.err.println("Auth Result is " + authResponse.getResult());
                this.onClose(1006, "AUTH_FAILED", true);
            }
        } else {
            try {
                ttsResponse = Tts.TtsResponse.parseFrom(byteMessage);
                // getFinish() 为true表示整个语音流返回结束
                System.err.println("getFinish: " + ttsResponse.getFinish());
                System.err.println("getText: " + ttsResponse.getText());
                System.err.println("getVoice: " + ttsResponse.getVoice());
                System.err.println("hasVoice: " + ttsResponse.hasVoice());
                System.err.println("hasFinish: " + ttsResponse.hasFinish());
                System.err.println("hasText: " + ttsResponse.hasText());
                System.err.println("hasId: " + ttsResponse.hasId());
                System.err.println("getResult: " + ttsResponse.getResult());
                System.err.println("---------------------------------------------");

                if (ttsResponse.hasVoice() && !ttsResponse.getFinish()) {
                    try {
                        File outfile = new File(filename);
                        if (!outfile.exists()) {
                            outfile.createNewFile();
                        }
                        DataOutputStream fw = new DataOutputStream(new FileOutputStream(outfile, true));
                        fw.write(ttsResponse.getVoice().toByteArray());
                        fw.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                finishFlag = ttsResponse.getFinish();

            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.err.println("Disconnected" + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }


}

