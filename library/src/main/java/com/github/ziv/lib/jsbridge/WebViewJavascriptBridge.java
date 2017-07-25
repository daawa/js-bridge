package com.github.ziv.lib.jsbridge;


public interface WebViewJavascriptBridge {
	
	public void send(Object data);
	public void send(Object data, CallBackFunction responseCallback);
	
	

}
