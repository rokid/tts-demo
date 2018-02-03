package com.tts;

import org.testng.annotations.Test;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 测试tts类
 * key、sercret、deviceTypeId传值如下步骤，其中deviceId 可以随意填写一般填写下面步骤的account_id值即可
 *  获取key、sercret、deviceTypeId步骤，登录 https://developer.rokid.com/#/ ，选择语音接入tab页面，然后点击创建新设备按钮，进入到新设备创建页面，如resources目录下tts.png所示
 *
 * @author mashuangwei
 * @create 2018-01-02 11:40
 **/

public class TtsDemo {

    String key = "";
    String deviceTypeId = "";
    String secret = "";
    String deviceId = "";
    String url = "wss://apigwws.open.rokid.com/api";

    @Test
    public void test(){
        TtsBasic ttsBasic = null;
        try {
            ttsBasic = new TtsBasic(new URI(url));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        ttsBasic.init(key, deviceTypeId, secret, deviceId);
        ttsBasic.sendTts("今天天气怎么样","zh","pcm", 24000);
        ttsBasic.close();
    }
}
