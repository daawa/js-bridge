package com.github.ziv.lib.jsbridge.example;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.github.ziv.lib.jsbridge.BridgeWebView;

/**
 * Created by hzzhangzhenwei on 2017/10/19.
 */

public class ProgressWebView extends BridgeWebView {
    private ProgressBar progress_bar_;
    public ProgressWebView(Context context) {
        super(context);
        init();
    }

    public ProgressWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private  void init() {
        progress_bar_ = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
        progress_bar_.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 10, 0, 0));
        // 添加drawable
        Drawable drawable = getContext().getResources().getDrawable(R.drawable.progress_drawable, getContext().getTheme());
        progress_bar_.setProgressDrawable(drawable);
        this.addView(progress_bar_);

        setWebChromeClient(new TWebChromeClient());

    }


    class TWebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress == 100) {
                progress_bar_.setVisibility(GONE);
            } else {
                if (progress_bar_.getVisibility() == GONE) {
                    progress_bar_.setVisibility(VISIBLE);
                }
                progress_bar_.setProgress(newProgress);
            }
        }
    }

}
