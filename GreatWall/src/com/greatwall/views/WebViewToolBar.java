package com.greatwall.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.greatwall.R;
import com.greatwall.util.IntentUtils;

public class WebViewToolBar extends LinearLayout
{
    private WebView   mWebView;
    private Context   mContext;
    private ImageView iv_webview_back;
    private ImageView iv_webview_refresh;
    private ImageView iv_webview_forward;
    private ImageView iv_webview_browser;

    public WebViewToolBar(Context context)
    {
        super(context);
        if (isInEditMode())
            return;

        this.mContext = context;
        init();
    }

    public WebViewToolBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        if (isInEditMode())
            return;

        this.mContext = context;
        init();
    }

    private void init()
    {
        removeAllViews();

        View view = LayoutInflater.from(mContext).inflate(R.layout.view_webviewbar, null);

        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(view);

        iv_webview_back = (ImageView) view.findViewById(R.id.iv_webview_back);
        iv_webview_refresh = (ImageView) view.findViewById(R.id.iv_webview_refresh);
        iv_webview_forward = (ImageView) view.findViewById(R.id.iv_webview_forward);
        iv_webview_browser = (ImageView) view.findViewById(R.id.iv_webview_browser);

        iv_webview_back.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if (mWebView.canGoBack())
                    mWebView.goBack();
            }
        });
        iv_webview_refresh.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                mWebView.reload();
            }
        });
        iv_webview_forward.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if (mWebView.canGoForward())
                    mWebView.goForward();
            }
        });
        iv_webview_browser.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                IntentUtils.openLink(mWebView.getUrl());
            }
        });
    }

    public void setWebView(WebView webView)
    {
        this.mWebView = webView;
    }

}
