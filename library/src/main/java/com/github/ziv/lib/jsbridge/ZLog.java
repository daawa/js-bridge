package com.github.ziv.lib.jsbridge;

import android.util.Log;

/**
 * Created by hzzhangzhenwei on 2017/2/23.
 */

public class ZLog {
    public static void d(String tag, String msg){
        if(BridgeWebView.debugMode){
            Log.d(tag,msg);
        }
    }

    public static void i(String tag, String msg){
        if(BridgeWebView.debugMode){
            Log.i(tag,msg);
        }
    }

    public static void w(String tag, String msg){
        if(BridgeWebView.debugMode){
            Log.w(tag,msg);
        }
    }

    public static void e(String tag, String msg){
        if(BridgeWebView.debugMode){
            Log.e(tag,msg);
        }
    }
}
