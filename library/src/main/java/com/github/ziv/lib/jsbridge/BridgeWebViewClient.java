package com.github.ziv.lib.jsbridge;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruce on 10/28/15.
 */
public class BridgeWebViewClient extends WebViewClient {
    private static final String TAG = BridgeWebViewClient.class.getSimpleName();
    private BridgeWebView webView;
    private WebViewClient extra;

    public BridgeWebViewClient(BridgeWebView webView) {
        this.webView = webView;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ZLog.w("WebClient:",url);

        if (url.startsWith(BridgeUtil.getInstance(webView).BRIDGE_RETURN_DATA)) { //
            webView.handlerReturnData(url);
            return true;
        } else if (url.startsWith(BridgeUtil.getInstance(webView).BRIDGE_OVERRIDE_SCHEMA)) { //
            webView.flushMessageQueue();
            return true;
        } else {
            if (extra != null) {
                return extra.shouldOverrideUrlLoading(view, url);
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (extra != null) {
            extra.onPageStarted(view, url, favicon);
        } else {
            super.onPageStarted(view, url, favicon);
        }

        ZLog.w(TAG, "onPageStarted");
    }

    /**
     * it is essential to set startUpMessages to null,
     * otherwise all subsequent messages would not be consumed.
     * check {@link BridgeWebView#queueMessage(Message)}  for more details
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        if (extra != null) {
            extra.onPageFinished(view, url);
        } else {
            super.onPageFinished(view, url);
        }

        injectLocalJs();

        if (webView.getStartupMessage() != null) {
            //todo: synchronize these operations
            List<Message> copy = new ArrayList<>(webView.getStartupMessage().size());
            copy.addAll(webView.getStartupMessage());
            webView.setStartupMessage(null);

            for (Message m : copy) {
                webView.dispatchMessage(m);
            }
        }

        ZLog.w(TAG, "onPageFinished");

    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (extra != null) {
            extra.onReceivedError(view, errorCode, description, failingUrl);
        } else {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (extra != null) {
            return extra.shouldInterceptRequest(view, url);
        }
        return super.shouldInterceptRequest(view, url);

    }

    @TargetApi(21)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        if (extra != null) {
            return extra.shouldInterceptRequest(view, request);
        }
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        if (extra != null) {
            extra.onLoadResource(view, url);
        } else {
            super.onLoadResource(view, url);
        }

    }

    @TargetApi(23)
    @Override
    public void onPageCommitVisible(WebView view, String url) {
        if(extra != null){
            extra.onPageCommitVisible(view, url);
        } else {
            super.onPageCommitVisible(view, url);
        }

    }

    @Override
    public void onTooManyRedirects(WebView view, android.os.Message cancelMsg, android.os.Message continueMsg) {
        if(extra != null){
            extra.onTooManyRedirects(view, cancelMsg, continueMsg);
        } else {
            super.onTooManyRedirects(view, cancelMsg, continueMsg);
        }

    }

    @TargetApi(23)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        if(extra != null){
            extra.onReceivedError(view, request, error);
        } else {
            super.onReceivedError(view, request, error);
        }

    }

    @TargetApi(23)
    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        if(extra != null){
            extra.onReceivedHttpError(view, request, errorResponse);
        } else {
            super.onReceivedHttpError(view, request, errorResponse);
        }

    }

    @Override
    public void onFormResubmission(WebView view, android.os.Message dontResend, android.os.Message resend) {
        if(extra != null){
            extra.onFormResubmission(view, dontResend, resend);
        } else {
            super.onFormResubmission(view, dontResend, resend);
        }

    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        if(extra != null ){
            extra.doUpdateVisitedHistory(view, url, isReload);
        } else {
            super.doUpdateVisitedHistory(view, url, isReload);
        }

    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        if(extra != null) {
            extra.onReceivedSslError(view, handler, error);
        } else {
            super.onReceivedSslError(view, handler, error);
        }
    }

    @TargetApi(21)
    @Override
    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
        if(extra != null ){
            extra.onReceivedClientCertRequest(view, request);
        } else {
            super.onReceivedClientCertRequest(view, request);
        }
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        if(extra == null){
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
        } else {
            extra.onReceivedHttpAuthRequest(view,handler,host,realm);
        }
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        if(extra == null){
            return super.shouldOverrideKeyEvent(view, event);
        } else {
            return extra.shouldOverrideKeyEvent(view, event);
        }
    }

    @Override
    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
        if(extra == null){
            super.onUnhandledKeyEvent(view, event);
        } else {
            extra.onUnhandledKeyEvent(view, event);
        }
    }

    @TargetApi(21)
    @Override
    public void onUnhandledInputEvent(WebView view, InputEvent event) {
        if(extra == null){
            super.onUnhandledInputEvent(view, event);
        } else {
            extra.onUnhandledInputEvent(view, event);
        }
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        if(extra==null){
            super.onScaleChanged(view, oldScale, newScale);
        } else {
            extra.onScaleChanged(view, oldScale, newScale);
        }
    }

    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
        if(extra == null){
            super.onReceivedLoginRequest(view, realm, account, args);
        } else {
            extra.onReceivedLoginRequest(view, realm, account, args);
        }
    }

    public void setExtraWebViewClient(WebViewClient client) {
        extra = client;
    }

    private void injectLocalJs(){
        if (BridgeWebView.assetJsFile != null) {
            BridgeUtil.getInstance(webView).webViewLoadLocalJs(webView, BridgeWebView.assetJsFile);
        }
    }
}