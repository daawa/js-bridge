package com.github.ziv.lib.jsbridge;

public class DefaultHandler implements BridgeHandler{

	String TAG = "DefaultHandler";
	
	@Override
	public void handler(Object data, CallBackFunction function) {
		if(function != null){
			function.onCallBack("DefaultHandler response data");
		}
	}

}
