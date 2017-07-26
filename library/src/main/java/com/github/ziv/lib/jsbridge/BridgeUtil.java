package com.github.ziv.lib.jsbridge;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;

public class BridgeUtil {
    final static String YY_OVERRIDE_SCHEMA = "yy://";
    final static String YY_RETURN_DATA = YY_OVERRIDE_SCHEMA + "return/";//format   yy://return/{function}/returncontent
    final static String YY_FETCH_QUEUE = YY_RETURN_DATA + "_fetchQueue/";
    final static String EMPTY_STR = "";
    final static String UNDERLINE_STR = "_";
    final static String SPLIT_MARK = "/";

    final static String CALLBACK_ID_FORMAT = "JAVA_CB_%s";
    String JS_HANDLE_MESSAGE_FROM_JAVA = "javascript:WebViewJavascriptBridge._handleMessageFromNative('%s');";
    String JS_FETCH_QUEUE_FROM_JAVA = "javascript:WebViewJavascriptBridge._fetchQueue();";
    public final static String JAVASCRIPT_STR = "javascript:";

    private String bridgeName;
    private static BridgeUtil one;
    private static volatile String preLoadedJs = null;

    public BridgeUtil(String bridgeName) {
        this.bridgeName = bridgeName;

        JS_HANDLE_MESSAGE_FROM_JAVA = "javascript:" + bridgeName + "._handleMessageFromNative('%s');";
        JS_FETCH_QUEUE_FROM_JAVA = "javascript:" + bridgeName + "._fetchQueue();";
    }

    public static BridgeUtil getInstance(String bridgeName) {
        if (one != null && TextUtils.equals(bridgeName, one.bridgeName)) {
            return one;
        } else {
            one = new BridgeUtil(bridgeName);
            return one;
        }
    }

    public String parseFunctionName(String jsUrl) {
        return jsUrl.replace("javascript:" + bridgeName + ".", "").replaceAll("\\(.*\\);", "");
    }


    public static String getDataFromReturnUrl(String url) {
        if (url.startsWith(YY_FETCH_QUEUE)) {
            return url.replace(YY_FETCH_QUEUE, EMPTY_STR);
        }

        String temp = url.replace(YY_RETURN_DATA, EMPTY_STR);
        String[] functionAndData = temp.split(SPLIT_MARK);

        if (functionAndData.length >= 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < functionAndData.length; i++) {
                sb.append(functionAndData[i]);
            }
            return sb.toString();
        }
        return null;
    }

    public static String getFunctionFromReturnUrl(String url) {
        String temp = url.replace(YY_RETURN_DATA, EMPTY_STR);
        String[] functionAndData = temp.split(SPLIT_MARK);
        if (functionAndData.length >= 1) {
            return functionAndData[0];
        }
        return null;
    }


    /**
     * js 文件将注入为第一个script引用
     *
     * @param view
     * @param url
     */
    public static void webViewLoadJs(WebView view, String url) {
        String js = "var newscript = document.createElement(\"script\");";
        js += "newscript.src=\"" + url + "\";";
        js += "document.scripts[0].parentNode.insertBefore(newscript,document.scripts[0]);";
        view.loadUrl("javascript:" + js);
    }

    public void webViewLoadLocalJs(final WebView view, String path) {
        if (preLoadedJs != null) {
            ZLog.w("JsAssert", "begin injecting js..");
            view.loadUrl("javascript:" + preLoadedJs);
            ZLog.w("JsAssert", "end injecting js..");
        } else {
            new LoadJsAsyncTask(view, path){
                @Override
                protected void onPostExecute(Void aVoid) {
                    ZLog.w("JsAssert", "begin injecting js..");
                    view.loadUrl("javascript:" + preLoadedJs);
                    ZLog.w("JsAssert", "end injecting js..");
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void preLoadJsString( WebView webView, final String path){
        LoadJsAsyncTask task = new LoadJsAsyncTask(webView, path);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private synchronized String assetFile2Str(Context c, String urlStr) {
        if (preLoadedJs != null) {
            ZLog.w("JsAssert", "assert preloaded, just return");
            return preLoadedJs;
        }
        ZLog.w("JsAssert", "assert loading begin..");
        InputStream in = null;
        try {
            in = c.getAssets().open(urlStr);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            StringBuilder sb = new StringBuilder();
            sb.append("var bridgeName = \"").append(bridgeName).append("\";");
            do {
                line = bufferedReader.readLine();
                if (line != null && !line.matches("^\\s*\\/\\/.*")) {
                    sb.append(line);
                }
            } while (line != null);

            bufferedReader.close();
            in.close();
            ZLog.w("JsAssert", "assert loading end..");
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    class LoadJsAsyncTask extends AsyncTask<Void, Void, Void> {

        private WebView webView;
        private Context context;
        private String path;
        LoadJsAsyncTask(WebView view, String path){
            this.webView = view;
            this.context = view.getContext();
            this.path = path;
        }

        @Override
        protected Void doInBackground(Void... params) {
            preLoadedJs = assetFile2Str(context, path);
            return null;
        }

    }
}
