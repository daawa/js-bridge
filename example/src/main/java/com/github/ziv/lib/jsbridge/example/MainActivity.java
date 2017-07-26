package com.github.ziv.lib.jsbridge.example;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.github.ziv.lib.jsbridge.BridgeHandler;
import com.github.ziv.lib.jsbridge.BridgeWebView;
import com.github.ziv.lib.jsbridge.CallBackFunction;
import com.github.ziv.lib.jsbridge.DefaultHandler;
import com.github.ziv.lib.jsbridge.ZLog;
import com.google.gson.Gson;

public class MainActivity extends Activity implements OnClickListener {

    private final String TAG = "MainActivity";

    BridgeWebView webView;

    View button;
    View loadJs;

    int RESULT_CODE = 0;

    ValueCallback<Uri> mUploadMessage;

    static class Location {
        String address;
    }

    static class User {
        String name;
        Location location;
        String testStr;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (BridgeWebView) findViewById(R.id.webView);
        webView.debugMode = true;

        button = findViewById(R.id.button);
        loadJs = findViewById(R.id.loadjs);

        button.setOnClickListener(this);
        loadJs.setOnClickListener(this);

        webView.setDefaultHandler(new DefaultHandler());

        webView.setWebChromeClient(new WebChromeClient() {

            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
                this.openFileChooser(uploadMsg);
            }

            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
                this.openFileChooser(uploadMsg);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                pickFile();
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                ZLog.w(TAG, "onProgressChanged: " + newProgress);
            }
        });

        webView.loadUrl("file:///android_asset/demo.html");

        webView.registerHandler("submitFromWeb", new BridgeHandler() {

            @Override
            public void handler(Object data, CallBackFunction function) {
                Log.i(TAG, "handler = submitFromWeb, data from web = " + data);
                function.onCallBack("submitFromWeb exe, response data 中文 from Java");
            }

        });

        final Model model = new Model();

        webView.registerHandler("giveMeJson", new BridgeHandler() {

            @Override
            public void handler(Object data, CallBackFunction function) {
                Log.i(TAG, "handler = giveMeToast, data from web = " + data);
                Toast.makeText(MainActivity.this, "handler = giveMeToast, data from web = " + data, Toast.LENGTH_SHORT).show();
                function.onCallBack(model);
            }

        });

        webView.registerHandler("giveMeString", new BridgeHandler() {

            @Override
            public void handler(Object data, CallBackFunction function) {
                Log.i(TAG, "handler = giveMeToast, data from web = " + data);
                Toast.makeText(MainActivity.this, "handler = giveMeToast, data from web = " + data, Toast.LENGTH_SHORT).show();
                function.onCallBack("Sample String");
            }

        });

        webView.registerHandler("giveMeBoolean", new BridgeHandler() {

            @Override
            public void handler(Object data, CallBackFunction function) {
                Log.i(TAG, "handler = giveMeToast, data from web = " + data);
                Toast.makeText(MainActivity.this, "handler = giveMeToast, data from web = " + data, Toast.LENGTH_SHORT).show();
                function.onCallBack(true);
            }

        });

        User user = new User();
        Location location = new Location();
        location.address = "SDU";
        user.location = location;
        user.name = "大头鬼";

        webView.callHandler("functionInJs", new Gson().toJson(user), new CallBackFunction() {
            @Override
            public void onCallBack(Object data) {

                Toast.makeText(MainActivity.this,
                        "functionInJs returned:" + data,
                        Toast.LENGTH_LONG)
                        .show();

            }
        });

        webView.send("hello", new CallBackFunction() {
            @Override
            public void onCallBack(Object data) {

            }
        });

    }

    public void pickFile() {
        Intent chooserIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooserIntent.setType("image/*");
        startActivityForResult(chooserIntent, RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RESULT_CODE) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                webView.callHandler("functionInJs", "data from Java", new CallBackFunction() {

                    @Override
                    public void onCallBack(Object data) {
                        Log.i(TAG, "reponse data from js " + data);
                    }

                });
                break;
            case R.id.loadjs:
                webView.loadBridgeJs();
        }
    }

}
