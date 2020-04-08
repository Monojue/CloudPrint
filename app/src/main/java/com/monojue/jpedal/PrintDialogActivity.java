package com.monojue.jpedal;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.monojue.jpedal.R.mipmap.ic_launcher_round;

public class PrintDialogActivity extends AppCompatActivity {

    private static final String CLOSE_POST_MESSAGE_NAME = "cp-dialog-on-close";
    private static final String CONTENT_TRANSFER_ENCODING = "base64";
    private static final String JS_INTERFACE = "AndroidPrintDialog";
    private static final String PRINT_DIALOG_URL = "https://www.google.com/cloudprint/dialog.html";
    Intent cloudPrintIntent;
    private WebView dialogWebView;
    RelativeLayout loading;
    SwipeRefreshLayout refreshLayout;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_dialog);
        dialogWebView = (WebView) findViewById(R.id.webview);
        loading = (RelativeLayout) findViewById(R.id.loadingLayout);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(ic_launcher_round);

        cloudPrintIntent = getIntent();
        dialogWebView.getSettings().setJavaScriptEnabled(true);
        dialogWebView.setWebViewClient(new PrintDialogWebClient());
        dialogWebView.addJavascriptInterface(new PrintDialogJavaScriptInterface(), JS_INTERFACE);
        dialogWebView.loadUrl(PRINT_DIALOG_URL);
        loading.setVisibility(View.VISIBLE);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                dialogWebView.loadUrl(PRINT_DIALOG_URL);
            }
        });

        if (!isNetworkAvailable()){
            Toast.makeText(this, "Network is not Available!", Toast.LENGTH_SHORT).show();
            loading.setVisibility(View.GONE);
        }


    }

    final class PrintDialogJavaScriptInterface {
        PrintDialogJavaScriptInterface() {
        }

        public String getType() {
            return PrintDialogActivity.this.cloudPrintIntent.getType();
        }

        public String getTitle() {
            return PrintDialogActivity.this.cloudPrintIntent.getExtras().getString("title");
        }

        public String getContent() {
            try {
                InputStream is = PrintDialogActivity.this.getContentResolver().openInputStream(PrintDialogActivity.this.cloudPrintIntent.getData());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                for (int n = is.read(buffer); n >= 0; n = is.read(buffer)) {
                    baos.write(buffer, 0, n);
                }
                is.close();
                baos.flush();
                return Base64.encodeToString(baos.toByteArray(), 0);
            } catch (FileNotFoundException e) {
                Toast.makeText(PrintDialogActivity.this, "File Not Fonund!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return "";
            } catch (IOException e2) {
                Toast.makeText(PrintDialogActivity.this, "Expection 2", Toast.LENGTH_SHORT).show();
                e2.printStackTrace();
                return "";
            }
        }

        public String getEncoding() {
            return PrintDialogActivity.CONTENT_TRANSFER_ENCODING;
        }

        public void onPostMessage(String message) {
            if (message.startsWith(PrintDialogActivity.CLOSE_POST_MESSAGE_NAME)) {
                Toast.makeText(PrintDialogActivity.this, "Done.", Toast.LENGTH_SHORT).show();
                PrintDialogActivity.this.finish();
            }
        }
    }


    private final class PrintDialogWebClient extends WebViewClient {
        private PrintDialogWebClient() {
        }

        public void onPageFinished(WebView view, String url) {



            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loading.setVisibility(View.INVISIBLE);
                    refreshLayout.setRefreshing(false);
                }
            },1000);


            if (PrintDialogActivity.PRINT_DIALOG_URL.equals(url)) {
                view.loadUrl("javascript:printDialog.setPrintDocument(printDialog.createPrintDocument(window.AndroidPrintDialog.getType(),window.AndroidPrintDialog.getTitle(),window.AndroidPrintDialog.getContent(),window.AndroidPrintDialog.getEncoding()))");
                view.loadUrl("javascript:window.addEventListener('message',function(evt){window.AndroidPrintDialog.onPostMessage(evt.data)}, false)");
            }
        }
    }

    boolean doublePressed = false;

    @Override
    public void onBackPressed() {

        if (doublePressed) {
            super.onBackPressed();
            return;
        }

        this.doublePressed = true;
        Toast.makeText(PrintDialogActivity.this, "Press again to Cancel", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doublePressed = false;
            }
        }, 2000);

    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null&&activeNetworkInfo.isConnected();
    }
}
