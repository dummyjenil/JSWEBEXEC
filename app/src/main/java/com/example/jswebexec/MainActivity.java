package com.example.jswebexec;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String FILE_TYPE = "*/*";
    private WebView webview;
    private ValueCallback<Uri[]> filePathCallback;
    private ValueCallback<Uri> uploadMessage;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webview = findViewById(R.id.webview);
        Button urlsubmit = findViewById(R.id.urlsubmit);
        EditText editurl = findViewById(R.id.editurl);
        Button jssubmit = findViewById(R.id.jssubmit);
        EditText editjs = findViewById(R.id.editjs);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch desktopmode = findViewById(R.id.desktopmode);
        WebSettings websetting = webview.getSettings();
        websetting.setJavaScriptEnabled(true);
        websetting.setSupportZoom(true);
        websetting.setDomStorageEnabled(true);
        websetting.setAllowContentAccess(true);
        websetting.setAllowFileAccess(true);
        websetting.setLoadWithOverviewMode(true);
        websetting.setUseWideViewPort(true);
        websetting.setBuiltInZoomControls(true);
        websetting.setDisplayZoomControls(false);
        websetting.setAllowContentAccess(true);

        webview.setWebViewClient(new WebViewClient(){
           @Override
            public void onPageFinished(WebView view, String url) {
               editurl.setText(url);
               super.onPageFinished(view, url);
            }
        });
        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> localfilePathCallback, FileChooserParams fileChooserParams) {
                filePathCallback = localfilePathCallback;
                openFileChooser();
                return true;
            }

        });
        desktopmode.setOnCheckedChangeListener((v, isChecked) -> {
            if(isChecked){
                websetting.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
            }else{
                websetting.setUserAgentString("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36");
            }
            webview.loadUrl(Objects.requireNonNull(webview.getUrl()));
        });
        webview.loadUrl("https://www.google.com/");
        urlsubmit.setOnClickListener(v -> webview.loadUrl(String.valueOf(editurl.getText())));
        jssubmit.setOnClickListener(v -> webview.evaluateJavascript(String.valueOf(editjs.getText()),null));
    }
    @Override
    public void onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack();
        } else {
            super.onBackPressed();
        }
    }
    private void openFileChooser() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(FILE_TYPE);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Use ActivityResultLauncher for handling activity result
        fileChooserLauncher.launch(Intent.createChooser(intent, "Select File"));
    }
    private final ActivityResultLauncher<Intent> fileChooserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (filePathCallback != null) {
                        filePathCallback.onReceiveValue(new Uri[]{fileUri});
                        filePathCallback = null;
                    } else if (uploadMessage != null) {
                        uploadMessage.onReceiveValue(fileUri);
                        uploadMessage = null;
                    }
                } else {
                    if (filePathCallback != null) {
                        filePathCallback.onReceiveValue(null);
                        filePathCallback = null;
                    } else if (uploadMessage != null) {
                        uploadMessage.onReceiveValue(null);
                        uploadMessage = null;
                    }
                    Toast.makeText(this, "File selection canceled", Toast.LENGTH_SHORT).show();
                }
            }
    );
}