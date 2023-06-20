<div align="center">

#  simpleIAST  ![1.0beta (shields.io)](https://img.shields.io/badge/1.0beta-brightgreen.svg)

</div>


<p align="center">
simpleIAST是一种交互式应用程序安全测试工具。
</p>

## 支持中间件

* Tomcat
* Springboot

## 支持JDK
* jdk 1.8
* jdk 11

## 支持漏洞
* SQL注入
* 反序列化漏洞
* SSRF
* URL跳转漏洞
* XXE
* 命令注入


## 快速开始

- **下载并自行打包**

```shell
# clone安装包
wget https://github.com/keven1z/simpleIAST/archive/refs/heads/master.zip

```

```shell
mvn clean package
```
- **运行**
>将iast-agent.jar和iast-engine.jar 放在同一目录

1. 跟随应用启动运行
```shell
java -javaagent:iast-agent.jar -jar [app.jar] # 
```

2. 应用启动后attach方式运行
```shell
java -jar iast-engine.jar -p [PID] # attach方式运行

```

## 漏洞上报报文
```json
{
    "http": {
        "url": "http://localhost:8000/raspbasic/sqlByWideByte",
        "method": "GET",
        // http请求报文，base64 两次编码
        "httpMessage": "UjBWVUlDOXlZWE53WW1GemFXTXZjM0ZzUW5sWGFXUmxRbmwwWlQ5cFpEMHhKV1JtSlRWakpUSTNLMjl5S3pFOU1Tc3RMU3N0SUVoVVZGQXZNUzR4RFFwb2IzTjBPbXh2WTJGc2FHOXpkRG80TURBd01BMEtZMjl1Ym1WamRHbHZianByWldWd0xXRnNhWFpsWlEwS2MyVmpMV05vTFhWaE9pSk9iM1F1UVM5Q2NtRnVaQ0k3ZGowaU9DSXNJQ0pEYUhKdmJXbDFiU0k3ZGowaU1URTBJaXdnSWsxcFkzSnZjMjltZENCRlpHZGxJanQyUFNJeE1UUWlJZzBLYzJWakxXTm9MWFZoTFcxdlltbHNaVG8vTURBTkNuTmxZeTFqYUMxMVlTMXdiR0YwWm05eWJUb2lWMmx1Wkc5M2N5SWlEUXAxY0dkeVlXUmxMV2x1YzJWamRYSmxMWEpsY1hWbGMzUnpPakV4RFFwMWMyVnlMV0ZuWlc1ME9rMXZlbWxzYkdFdk5TNHdJQ2hYYVc1a2IzZHpJRTVVSURFd0xqQTdJRmRwYmpZME95QjROalFwSUVGd2NHeGxWMlZpUzJsMEx6VXpOeTR6TmlBb1MwaFVUVXdzSUd4cGEyVWdSMlZqYTI4cElFTm9jbTl0WlM4eE1UUXVNQzR3TGpBZ1UyRm1ZWEpwTHpVek55NHpOaUJGWkdjdk1URTBMakF1TVRneU15NDBNek1OQ21GalkyVndkRHAwWlhoMEwyaDBiV3dzWVhCd2JHbGpZWFJwYjI0dmVHaDBiV3dyZUcxc0xHRndjR3hwWTJGMGFXOXVMM2h0YkR0eFBUQXVPU3hwYldGblpTOTNaV0p3TEdsdFlXZGxMMkZ3Ym1jc0tpOHFPM0U5TUM0NExHRndjR3hwWTJGMGFXOXVMM05wWjI1bFpDMWxlR05vWVc1blpUdDJQV0l6TzNFOU1DNDNOdzBLYzJWakxXWmxkR05vTFhOcGRHVTZibTl1WldVTkNuTmxZeTFtWlhSamFDMXRiMlJsT201aGRtbG5ZWFJsWlEwS2MyVmpMV1psZEdOb0xYVnpaWEk2UHpFeERRcHpaV010Wm1WMFkyZ3RaR1Z6ZERwa2IyTjFiV1Z1ZEhRTkNtRmpZMlZ3ZEMxbGJtTnZaR2x1WnpwbmVtbHdMQ0JrWldac1lYUmxMQ0JpY25JTkNtRmpZMlZ3ZEMxc1lXNW5kV0ZuWlRwNmFDMURUaXg2YUR0eFBUQXVPU3hsYmp0eFBUQXVPQ3hsYmkxSFFqdHhQVEF1Tnl4bGJpMVZVenR4UFRBdU5qWU5DbU52YjJ0cFpUcEpaR1ZoTFdGaU5tRTBaR1l4UFRSak1EYzJNemt6TFRVM01tSXRORGsyWkMwNFlUbGhMVGcyTVRFMFpEQTROemt4WTJNTkNnMEs="
    },
    "taintLinkList": [
        // 污染源
        {
            "invokeId": 2,
            "className": "org/apache/catalina/connector/RequestFacade",
            "method": "getParameter",
            "desc": "(Ljava/lang/String;)Ljava/lang/String;",
            "taintValueType": "java.lang.String",
            "type": "SOURCE",
            // 污染源参数值
            "fromValue": "id",
            // 污染源值
            "toValue": "1�\\' or 1=1 -- -",
            "sanitizer": false
        },
        {
            "invokeId": 4,
            "className": "java/lang/AbstractStringBuilder",
            "method": "appendChars",
            "desc": "(Ljava/lang/CharSequence;II)V",
            "type": "PROPAGATION",
            "fromValue": "1�\\' or 1=1 -- -",
            "toValue": "1�\\\\' or 1=1 -- -",
            "sanitizer": false
        },
        {
            "invokeId": 5,
            "className": "java/lang/StringBuilder",
            "method": "toString",
            "desc": "()Ljava/lang/String;",
            "type": "PROPAGATION",
            "fromValue": "1�\\\\' or 1=1 -- -",
            "toValue": "1�\\\\' or 1=1 -- -",
            "sanitizer": false
        },
        {
            "invokeId": 6,
            "className": "java/lang/StringBuilder",
            "method": "append",
            "desc": "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            "type": "PROPAGATION",
            "fromValue": "1�\\\\' or 1=1 -- -",
            "toValue": "SELECT * FROM users WHERE id='1�\\\\' or 1=1 -- -",
            "sanitizer": false
        },
        {
            "invokeId": 7,
            "className": "java/lang/StringBuilder",
            "method": "toString",
            "desc": "()Ljava/lang/String;",
            "type": "PROPAGATION",
            "fromValue": "SELECT * FROM users WHERE id='1�\\\\' or 1=1 -- -'",
            "toValue": "SELECT * FROM users WHERE id='1�\\\\' or 1=1 -- -'",
            "sanitizer": false
        },
        {
            "invokeId": 8,
            "className": "org/postgresql/jdbc/PgStatement",
            "method": "executeQuery",
            "desc": "(Ljava/lang/String;)Ljava/sql/ResultSet;",
            "taintValueType": "java.lang.String",
            "type": "SINK",
            "fromValue": "SELECT * FROM users WHERE id='1�\\\\' or 1=1 -- -'",
            "vulnType": "sqli",
            "sanitizer": false
        }
    ],
    "timestamp": "2023-06-14 21:31:59",
    "vulType": "sqli"
}
```

## 项目结构
```
|_agent：负责agent的加载、卸载。
|_engine：污点追踪的引擎代码
   |__resource：资源文件
      |___policy.json：hook策略文件
```