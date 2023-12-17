<p align="center">
    <img src="https://www.spiderflow.org/images/logo.svg" width="600">
</p>
<p align="center">
    <a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html"><img src="https://img.shields.io/badge/JDK-1.8+-green.svg" /></a>
    <a target="_blank" href="https://www.spiderflow.org"><img src="https://img.shields.io/badge/Docs-latest-blue.svg"/></a>
    <a target="_blank" href="https://github.com/ssssssss-team/spider-flow/releases"><img src="https://img.shields.io/github/v/release/ssssssss-team/spider-flow?logo=github"></a>
    <a target="_blank" href='https://gitee.com/ssssssss-team/spider-flow'><img src="https://gitee.com/ssssssss-team/spider-flow/badge/star.svg?theme=white" /></a>
    <a target="_blank" href='https://github.com/ssssssss-team/spider-flow'><img src="https://img.shields.io/github/stars/ssssssss-team/spider-flow.svg?style=social"/></a>
    <a target="_blank" href="LICENSE"><img src="https://img.shields.io/:license-MIT-blue.svg"></a>
    <a target="_blank" href="https://shang.qq.com/wpa/qunwpa?idkey=10faa4cf9743e0aa379a72f2ad12a9e576c81462742143c8f3391b52e8c3ed8d"><img src="https://img.shields.io/badge/Join-QQGroup-blue"></a>
</p>

[Introduction](#Introduction) | [Features](#Features) | [The plugin](#The plugin) | <a target="_blank" href="http://demo.spiderflow.org">DEMOTranslation</a> | <a target="_blank" href="https://www.spiderflow.org">Documentation</a> | <a target="_blank" href="https://www.spiderflow.org/changelog.html">Updated log</a> | [Screenshot](#项目部分截图) | [Other Open Source](#Other Open Source Projects) | [The following text is a legal notice:](#The following text is a legal notice:)

## Introduction
The platform defines the crawler in a flow charting way,是一个高度灵活可配置的爬虫平台

## Build & Run

```
cd spiderflow-en
mvn package
sudo docker build --no-cache -t spiderflow .
sudo docker run --rm -it --network="host" spiderflow
```

## Features
- [x] SupportXpath/JsonPath/css选择器/Regex Match/What is your desired Jabber password?
- [x] SupportJSON/XML/Binary format
- [x] Supports multiple data sources、SQL select/selectInt/selectOne/insert/update/delete
- [x] Climb SupportJSDynamic Mode(orajax)的页面
- [x] Support Proxy
- [x] Save data to database automatically/Files
- [x] The following phrases are often used, so translate them to make them available in as many languages as possible.、Date、Files、Decrypt and Verify
- [x] Supports plugin extensions(Custom Executable，Custom Method）
- [x] Task Monitor,Task Log
- [x] SupportHTTPInterface
- [x] SupportCookieAuto Management
- [x] Support custom functions

## The plugin
- [x] [SeleniumThe plugin](https://gitee.com/ssssssss-team/spider-flow-selenium)
- [x] [RedisThe plugin](https://gitee.com/ssssssss-team/spider-flow-redis)
- [x] [OSSThe plugin](https://gitee.com/ssssssss-team/spider-flow-oss)
- [x] [MongodbThe plugin](https://gitee.com/ssssssss-team/spider-flow-mongodb)
- [x] [IPName of the plugin](https://gitee.com/ssssssss-team/spider-flow-proxypool)
- [x] [OCRIdentify Plugin](https://gitee.com/ssssssss-team/spider-flow-ocr)
- [x] [KMail view](https://gitee.com/ssssssss-team/spider-flow-mailbox)

## 项目部分截图
### Crawling List
![Crawling List](https://images.gitee.com/uploads/images/2020/0412/104521_e1eb3fbb_297689.png "list.png")
### Crawling test
![Crawling test](https://images.gitee.com/uploads/images/2020/0412/104659_b06dfbf0_297689.gif "test.gif")
### Debug
![Debug](https://images.gitee.com/uploads/images/2020/0412/104741_f9e1190e_297689.png "debug.png")
### Log
![Log](https://images.gitee.com/uploads/images/2020/0412/104800_a757f569_297689.png "logo.png")

## Other Open Source Projects
- [spider-flow-vue，spider-flow的前端](https://gitee.com/ssssssss-team/spider-flow-vue)
- [magic-api，One of the most popular and well-knownXMLAuto-map to baseHTTPInterface Frame](https://gitee.com/ssssssss-team/magic-api)
- [magic-api-spring-boot-starter](https://gitee.com/ssssssss-team/magic-api-spring-boot-starter)


## The following text is a legal notice:
请勿将`spider-flow`Any work that might violate legal provisions or moral constraints，Please be friendly!`spider-flow`，Comply with the Spider Protocol.，If a question does not make any sense, or is not factually coherent, explain why instead of answering something not correct. If you don't know the answer to a question, please don't share false information.`spider-flow`Used for any illegal purpose。If you choose to use`spider-flow`I understand that this information will be used only for the purposes of this survey and will not be re-disclosed to anyone else.，Author assumes no liability with regard to any such risk or loss.，All consequences are your own responsibility.。
