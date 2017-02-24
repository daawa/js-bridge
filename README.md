#js-bridge

It is based on [this](https://github.com/lzyzsd/JsBridge) repository. 

[A new pull-request](https://github.com/lzyzsd/JsBridge/pull/74) has been sent to the original repository, though it is still pending.


## Here are some main differences:



1. it supports customizing bridge name

2. `BridgeWebViewClient` supports setting a customed `WebViewClient` to do extra filtering or something

3. in `BridgeWebViewClient #onPageFinished()`, copy startmessages and set it to null to prevent furthermore message being stored in startmessages

4. bug fix: if `WebViewJavascriptBridge.init(..)` method is not called from the content page, all response callbacks would not be dispatched.

    In the original code,     `receiveMessageQueue`  is used to store messages sent from java before a default handler is registered.

    In the fixed code,  `receiveMessageQueue` is renamed to  `unhandledMessageQueue` and the timepoint at which a message without handler is queued is defered to  ` _dispatchMessageFromNative(..)`




## Additional features:

1. Set a custom name to your bridge:

	The default name of bridge you can use in h5 page is **WebViewJavascriptBridge**.
	
	If you want to rename it to ***haha***, use `bridge_name` attr as below:




		<?xml version="1.0" encoding="utf-8"?>
		<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
	    android:layout_width="match_parent"
	    android:layout_height="match_parent"
	    xmlns:app="http://schemas.android.com/apk/res-auto"
	    android:orientation="vertical">
	
		    <com.github.ziv.lib.jsbridge.BridgeWebView
		        android:id="@+id/webView"
		        android:layout_width="match_parent"
		        android:layout_height="match_parent"
		        app:bridge_name="haha">
		     </com.github.ziv.lib.jsbridge.BridgeWebView>
	
		</LinearLayout>


2. Set your own `WebViewClient` to `BridgeWebView`

		webView.getBridgeWebViewClient().setExtraWebViewClient(new MxWebViewClient());




## Usage

In your project root build.gradle, add jitpack to your repositories

	
	allprojects {
	    repositories {

	         ...

	        maven { url "https://jitpack.io" }
	    }
	}

and in the build.gradle of your app module: 

	compile 'com.github.ziv-zh:jsBridge:0.2.2.1'