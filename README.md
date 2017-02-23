#js-bridge

It is based on [this](https://github.com/lzyzsd/JsBridge) repository. 

[A new pull-request](https://github.com/lzyzsd/JsBridge/pull/74) has been sent to the original repository, and it is still pending.


## Here are some main differences:



1. it supports customizing bridge name

2. BridgeWebClient supports setting a customed WebViewClient to do extra filtering or something

3. in BridgeWebClient #onPageFinished(), copy startmessages and set it to null to prevent furthermore message being stored in startmessages

4. bug fix: if `WebViewJavascriptBridge.init(..)` method is not called from the content page, all response callbacks would not be dispatched.

    In the original code,     `receiveMessageQueue`  is used to store messages sent from java before a default handler is registered.

    In the fixed code,  `receiveMessageQueue` is renamed to  `unhandledMessageQueue` and the timepoint a message without handler is queued is defered to  ` _dispatchMessageFromNative(..)`



