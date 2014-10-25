package com.greatwall.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.greatwall.R;
import com.greatwall.app.Application;
import com.greatwall.app.listeners.OnUpdateListener;
import com.greatwall.util.NetWorkUtil;

public class CustomWebView extends FrameLayout
{
    private WebView mWebView;
    private TextView tv_reload;
    private LinearLayout ll_nonet;
    private String mUrl;

    private OnUpdateListener<Integer> onUpdateListener;

    public CustomWebView(Context context)
    {
        super(context);
        if (isInEditMode())
        {
            return;
        }

        initView();
    }

    public CustomWebView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        if (isInEditMode())
        {
            return;
        }

        initView();

    }

    public WebView getWebView()
    {
        return mWebView;
    }

    private void initView()
    {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_custom_webview, null);
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(view);

        mWebView = (WebView) findViewById(R.id.wv_content);
        tv_reload = (TextView) findViewById(R.id.tv_reload);
        ll_nonet = (LinearLayout) findViewById(R.id.ll_nonet);

        tv_reload.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if (NetWorkUtil.isNetworkConnected(getContext()))
                {
                    mWebView.removeAllViews();
                    mWebView.loadUrl("about:blank");
                    loadUrl(mUrl);
                    hiddenErrorWebPage();
                    Application.getInstance().runOnUiThreadDelay(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mWebView.setVisibility(VISIBLE);
                        }
                    }, 500);
                } else
                {
                    showErrorWebPage();
                }
            }
        });

        mWebView.setWebViewClient(new WebViewClient()
        {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                super.onReceivedError(view, errorCode, description, failingUrl);
                mWebView.setVisibility(GONE);
                showErrorWebPage();
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int newProgress)
            {
                super.onProgressChanged(view, newProgress);
                if (null != onUpdateListener)
                {
                    onUpdateListener.onUpdate(newProgress);
                }
            }
        });

        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);

    }

    public void loadUrl(String url)
    {
        this.mUrl = url;
        this.mWebView.loadUrl(url);
    }

    public void showErrorWebPage()
    {
        ll_nonet.setVisibility(View.VISIBLE);
    }

    public void hiddenErrorWebPage()
    {
        ll_nonet.setVisibility(View.GONE);
    }

    public void setOnUpdateListener(OnUpdateListener<Integer> onUpdateListener)
    {
        this.onUpdateListener = onUpdateListener;
    }

}
