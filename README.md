# tts-demo开发环境
1. 开发IDE：intellij idea、maven
2. 开发语言：java
3. 协议：websocket、grpc

# tts依赖的protobuf文件，这三个文件已放入demo中
1. auth.proto
2. speech_types.proto
3. tts.proto

# 使用protobuf文件生成java类
1. 工程中的pom文件已配置protobuf生成java类插件
2. 在terminal下首先执行protobuf:compile，然后再执行protobuf:compile-custom命令，执行成功后会在target目录下生成对应的java文件

# tts说明
1. 向tts服务发送一段文本，tts服务会返回该文本对应的语音流
2. tts服务有一个认证过程，在第一次请求时会进行认证，开发者首先要在rokid云开放服务平台（https://developer.rokid.com/#/）创建一个虚拟的语音设备以获取对应的key、sercret、devicetype，只有拿到这些信息才能通过tts接口认证请求
3. tts服务地址是 wss://apigwws.open.rokid.com/api