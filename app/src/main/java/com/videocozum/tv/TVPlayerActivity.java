package com.videocozum.tv;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TVPlayerActivity extends AppCompatActivity {

    private WebView webView;
    private FrameLayout fullscreenContainer;
    private ProgressBar loadingIndicator;
    private View tvHintBar;

    private View customVideoView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    private String remoteEngineJs = "";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        webView = findViewById(R.id.webview_tv);
        fullscreenContainer = findViewById(R.id.fullscreen_video_container);
        loadingIndicator = findViewById(R.id.loading_indicator);
        tvHintBar = findViewById(R.id.tv_hint_bar);

        loadRemoteEngineJs();
        setupWebView();

        String targetUrl = getIntent().getStringExtra("TARGET_URL");
        if (targetUrl == null || targetUrl.trim().isEmpty()) {
            Toast.makeText(this, "Video adresi bulunamadı!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        webView.loadUrl(targetUrl);

        // İpucu çubuğunu 6 saniye sonra yavaşça gizle
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (tvHintBar != null) {
                tvHintBar.animate().alpha(0f).setDuration(800).withEndAction(() -> tvHintBar.setVisibility(View.GONE));
            }
        }, 6000);
    }

    private void loadRemoteEngineJs() {
        try {
            InputStream is = getAssets().open("tv_remote_engine.js");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            remoteEngineJs = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Web sitelerinin mobil uygulamayı indirin uyarısı yerine masaüstü/TV sürümünü sunması için
        settings.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 VideoCozumTV/1.0");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 80) {
                    loadingIndicator.setVisibility(View.GONE);
                } else {
                    loadingIndicator.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customVideoView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customVideoView = view;
                customViewCallback = callback;
                fullscreenContainer.addView(view);
                fullscreenContainer.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
            }

            @Override
            public void onHideCustomView() {
                if (customVideoView == null) return;
                fullscreenContainer.removeView(customVideoView);
                fullscreenContainer.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                if (customViewCallback != null) {
                    customViewCallback.onCustomViewHidden();
                }
                customVideoView = null;
                customViewCallback = null;
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return super.onConsoleMessage(consoleMessage);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loadingIndicator.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loadingIndicator.setVisibility(View.GONE);

                // D-Pad Kumanda Navigasyon Motorunu Enjekte Et
                if (!remoteEngineJs.isEmpty()) {
                    webView.evaluateJavascript(remoteEngineJs, null);
                }

                // TV Ekranı için Tam Ekran ve Kumanda Kontrol Scripti
                String cssCleanup = "javascript:(function() {" +
                        "var elms = document.querySelectorAll('.app-banner, .mobile-app-download, #smart-banner, .bannerArea');" +
                        "elms.forEach(function(el){ el.style.display = 'none'; });" +
                        "var canvas = document.getElementById('canvas');" +
                        "if (canvas) {" +
                        "  canvas.style.maxWidth = '100%';" +
                        "  canvas.style.maxHeight = '90vh';" +
                        "  canvas.style.margin = '0 auto';" +
                        "  canvas.style.display = 'block';" +
                        "  document.body.style.backgroundColor = '#000000';" +
                        "  document.body.style.overflow = 'hidden';" +
                        "}" +
                        "var audio = document.getElementById('sound');" +
                        "if (audio) {" +
                        "  audio.play().catch(function(e){ console.log(e); });" +
                        "  window.addEventListener('keydown', function(e) {" +
                        "    if (e.key === 'Enter' || e.keyCode === 13) {" +
                        "      if (audio.paused) audio.play(); else audio.pause();" +
                        "    } else if (e.key === 'ArrowLeft' || e.keyCode === 37) {" +
                        "      audio.currentTime = Math.max(0, audio.currentTime - 5);" +
                        "    } else if (e.key === 'ArrowRight' || e.keyCode === 39) {" +
                        "      audio.currentTime = Math.min(audio.duration, audio.currentTime + 5);" +
                        "    }" +
                        "  }, true);" +
                        "}" +
                        "})();";
                webView.evaluateJavascript(cssCleanup, null);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false; // Tüm linkleri TV WebView içinde tut
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Tam ekran video oynatılıyorsa önce videodan çık
            if (customVideoView != null) {
                if (webView.getWebChromeClient() != null) {
                    webView.getWebChromeClient().onHideCustomView();
                }
                return true;
            }
            // Sayfa geçmişi varsa geriye dön
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
            // Aksi halde ana ekrana dön
            finish();
            return true;
        }

        // Kumanda Yön Tuşları (D-Pad) ve OK Tuşu
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            webView.dispatchKeyEvent(event);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
            keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            webView.dispatchKeyEvent(event);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
