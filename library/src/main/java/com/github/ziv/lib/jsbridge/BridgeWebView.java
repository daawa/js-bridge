package com.github.ziv.lib.jsbridge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.ziv.lib.library.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("SetJavaScriptEnabled")
public class BridgeWebView extends WebView implements WebViewJavascriptBridge {

    public static boolean debugMode = false;
    private final String TAG = "BridgeWebView";
    private BridgeWebViewClient client;
    private volatile String bridgeName = "WebViewJavascriptBridge";

    public static final String assetJsFile = "WebViewJavascriptBridge.js";
    Map<String, CallBackFunction> responseCallbacks = new HashMap<String, CallBackFunction>();
    Map<String, BridgeHandler> messageHandlers = new HashMap<String, BridgeHandler>();
    BridgeHandler defaultHandler = new DefaultHandler();

    private List<Message> startupMessage = new ArrayList<Message>();

    public List<Message> getStartupMessage() {
        return startupMessage;
    }

    public void setStartupMessage(List<Message> startupMessage) {
        this.startupMessage = startupMessage;
    }

    private long uniqueId = 0;

    public BridgeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BridgeWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public BridgeWebView(Context context) {
        this(context, null);
    }

    public void setBridgeName(String name) {
        this.bridgeName = name;
    }

    public String getBridgeName() {
        return bridgeName;
    }

    /**
     * @param handler default handler,handle messages send by js without assigned handler name,
     *                if js message has handler name, it will be handled by named handlers registered by native
     */
    public void setDefaultHandler(BridgeHandler handler) {
        this.defaultHandler = handler;
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        super.setWebViewClient(client);
        if (client instanceof BridgeWebViewClient) {
            this.client = (BridgeWebViewClient) client;
        }
    }

    public BridgeWebViewClient getBridgeWebViewClient() {
        return client;
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.BridgeWebView);
            if (typedArray != null ) {
                try {
                    String name = typedArray.getString(R.styleable.BridgeWebView_bridge_name);
                    if (!TextUtils.isEmpty(name)) {
                        setBridgeName(name);
                    }
                }catch (RuntimeException e){

                }

            }
        }
        this.setVerticalScrollBarEnabled(false);
        this.setHorizontalScrollBarEnabled(false);
        this.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        this.setWebViewClient(generateBridgeWebViewClient());

        BridgeUtil.getInstance(bridgeName).preLoadJsString(this, assetJsFile);
    }

    protected BridgeWebViewClient generateBridgeWebViewClient() {
        return new BridgeWebViewClient(this);
    }

    void handlerReturnData(String url) {
        String functionName = BridgeUtil.getFunctionFromReturnUrl(url);
        CallBackFunction f = responseCallbacks.get(functionName);
        String data = BridgeUtil.getDataFromReturnUrl(url);
        if (f != null) {
            f.onCallBack(data);
            responseCallbacks.remove(functionName);
            return;
        }
    }

    @Override
    public void send(Object data) {
        send(data, null);
    }


    @Override
    public void send(Object data, CallBackFunction responseCallback) {
        doSend(null, data, responseCallback);
    }

    private void doSend(String handlerName, Object data, CallBackFunction responseCallback) {
        Message m = new Message();
        if (data != null) {
            m.setData(data);
        }
        if (responseCallback != null) {
            String callbackStr = String.format(BridgeUtil.CALLBACK_ID_FORMAT, ++uniqueId + (BridgeUtil.UNDERLINE_STR + SystemClock.currentThreadTimeMillis()));
            responseCallbacks.put(callbackStr, responseCallback);
            m.setCallbackId(callbackStr);
        }
        if (!TextUtils.isEmpty(handlerName)) {
            m.setHandlerName(handlerName);
        }
        queueMessage(m);
    }

    private void queueMessage(Message m) {
        if (startupMessage != null) {
            startupMessage.add(m);
        } else {
            dispatchMessage(m);
        }
    }

    void dispatchMessage(Message m) {
        String messageJson = Message.toJson2(m);
        //escape special characters for json string
        messageJson = messageJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
        messageJson = messageJson.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");
        String javascriptCommand = String.format(BridgeUtil.getInstance(this.getBridgeName()).JS_HANDLE_MESSAGE_FROM_JAVA, messageJson);
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            this.loadUrl(javascriptCommand);
        }
    }

    void flushMessageQueue() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            loadUrl(BridgeUtil.getInstance(this.getBridgeName()).JS_FETCH_QUEUE_FROM_JAVA, CONSUME_MESSAGE_HANDLER);
        }
    }

    public void loadUrl(String jsUrl, CallBackFunction returnCallback) {
        this.loadUrl(jsUrl);
        responseCallbacks.put(BridgeUtil.getInstance(this.getBridgeName()).parseFunctionName(jsUrl), returnCallback);
    }

    /**
     * register handler,so that javascript can call it
     *
     * @param handlerName
     * @param handler
     */
    public void registerHandler(String handlerName, BridgeHandler handler) {
        if (handler != null) {
            messageHandlers.put(handlerName, handler);
        }
    }

    /**
     * call javascript registered handler
     *
     * @param handlerName
     * @param data
     * @param callBack
     */
    public void callHandler(String handlerName, String data, CallBackFunction callBack) {
        doSend(handlerName, data, callBack);
    }

    public void loadBridgeJs() {
        BridgeUtil.getInstance(this.getBridgeName()).webViewLoadLocalJs(this, BridgeWebView.assetJsFile);
    }

    // messages from native to web
    CallBackFunction CONSUME_MESSAGE_HANDLER = new CallBackFunction() {

        @Override
        public void onCallBack(Object data) {
            // deserializeMessage
            List<Message> list = null;
            if(data == null){
                data = "" ;
            }
            try {
                list = Message.toArrayList(data.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            if (list == null || list.size() == 0) {
                return;
            }
            for (int i = 0; i < list.size(); i++) {
                Message m = list.get(i);
                String responseId = m.getResponseId();
                // if message has a responseId, then it is a RESPONSE from javascript to java（ we　called js handler by WebView.callHandler(..))
                if (!TextUtils.isEmpty(responseId)) {
                    CallBackFunction function = responseCallbacks.get(responseId);
                    Object responseData = m.getResponseData();
                    function.onCallBack(responseData);
                    responseCallbacks.remove(responseId);
                } else { // if message has a callbackId, then it is a CALL from javascript to java ( js called XXBridge.callHandler(...) )
                    CallBackFunction responseFunction = null;
                    final String callbackId = m.getCallbackId();
                    if (!TextUtils.isEmpty(callbackId)) {
                        responseFunction = new CallBackFunction() {
                            @Override
                            public void onCallBack(Object data) {
                                Message responseMsg = new Message();
                                responseMsg.setResponseId(callbackId);
                                responseMsg.setResponseData(data);
                                queueMessage(responseMsg);
                            }
                        };
                    } else {
                        responseFunction = new CallBackFunction() {
                            @Override
                            public void onCallBack(Object data) {
                                // do nothing
                            }
                        };
                    }
                    BridgeHandler handler;
                    if (!TextUtils.isEmpty(m.getHandlerName())) {
                        handler = messageHandlers.get(m.getHandlerName());
                    } else {
                        handler = defaultHandler;
                    }
                    if (handler != null) {
                        handler.handler(m.getData(), responseFunction);
                    }
                }
            }
        }
    };
}
