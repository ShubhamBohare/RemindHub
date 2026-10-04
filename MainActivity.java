package com.example.remindhub;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.webkit.WebViewAssetLoader;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#059669"));
        }

        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(window, window.getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(false);
        }

        int statusBarHeight = getStatusBarHeight();
        View rootContentView = findViewById(android.R.id.content);
        if (rootContentView != null) {
            rootContentView.setBackgroundColor(Color.parseColor("#059669"));
            if (statusBarHeight > 0) {
                rootContentView.setPadding(0, statusBarHeight, 0, 0);
            }

            ViewCompat.setOnApplyWindowInsetsListener(rootContentView, (view, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
                int topPadding = insets.top > 0 ? insets.top : statusBarHeight;
                view.setPadding(0, topPadding, 0, 0);
                return windowInsets;
            });
            ViewCompat.requestApplyInsets(rootContentView);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        webView = findViewById(R.id.remindHubWebView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);

        webView.addJavascriptInterface(new WebAppInterface(this), "AndroidBridge");

        // Set up the asset loader to run locally under an authorized domain
        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("whatsapp://") || url.startsWith("sms:") || url.startsWith("intent:")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }
                return false;
            }
        });

        // Load through virtual domain instead of file:///
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html");

        // Back gesture controller
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView == null) {
                    moveTaskToBack(true);
                    return;
                }

                webView.evaluateJavascript("window.handleBackNavigation && window.handleBackNavigation()", result -> {
                    if ("true".equals(result)) {
                        return;
                    }
                    moveTaskToBack(true);
                });
            }
        });
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void setDarkMode(boolean isDark) {
            runOnUiThread(() -> {
                Window window = getWindow();
                WindowInsetsControllerCompat insets = WindowCompat.getInsetsController(window, window.getDecorView());
                if (isDark) {
                    window.setStatusBarColor(Color.parseColor("#0f172a"));
                    if (insets != null) insets.setAppearanceLightStatusBars(false);
                } else {
                    window.setStatusBarColor(Color.parseColor("#059669"));
                    if (insets != null) insets.setAppearanceLightStatusBars(false);
                }
            });
        }

        @JavascriptInterface
        public void schedulePaymentReminder(int hour, int minute, boolean enable) {
            scheduleAlarm(hour, minute, enable, "PAYMENT", 1001);
        }

        @JavascriptInterface
        public void scheduleMedicineAlarm(int hour, int minute, boolean enable) {
            scheduleAlarm(hour, minute, enable, "MEDICINE", 1002);
        }

        @JavascriptInterface
        public void scheduleRentalReminder(int hour, int minute, boolean enable) {
            scheduleAlarm(hour, minute, enable, "RENTAL", 1003);
        }

        @JavascriptInterface
        public void shareGeneric(String title, String text) {
            runOnUiThread(() -> {
                try {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, title != null ? title : "RemindHub Details");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                    sendIntent.setType("text/plain");

                    Intent shareIntent = Intent.createChooser(sendIntent, "Share with");
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(shareIntent);
                } catch (Exception e) {
                    Toast.makeText(mContext, "Unable to share: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public void shareViaWhatsApp(String text) {
            runOnUiThread(() -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.setPackage("com.whatsapp");
                    intent.putExtra(Intent.EXTRA_TEXT, text);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(mContext, "WhatsApp not installed. Launching generic share...", Toast.LENGTH_SHORT).show();
                    shareGeneric("Share via", text);
                } catch (Exception e) {
                    Toast.makeText(mContext, "Error launching WhatsApp: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public void shareViaSms(String text) {
            runOnUiThread(() -> {
                try {
                    Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                    sendIntent.setData(Uri.parse("sms:"));
                    sendIntent.putExtra("sms_body", text);
                    sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(sendIntent);
                } catch (Exception e) {
                    shareGeneric("Send SMS", text);
                }
            });
        }

        private void scheduleAlarm(int hour, int minute, boolean enable, String type, int requestCode) {
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(mContext, AlarmReceiver.class);
            intent.putExtra("REMINDER_TYPE", type);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    mContext,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (!enable) {
                if (alarmManager != null) {
                    alarmManager.cancel(pendingIntent);
                }
                runOnUiThread(() -> Toast.makeText(mContext, type + " reminder disabled", Toast.LENGTH_SHORT).show());
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }
                runOnUiThread(() -> Toast.makeText(mContext, type + " set for " + String.format(Locale.getDefault(), "%02d:%02d", hour, minute), Toast.LENGTH_SHORT).show());
            }
        }
    }
}