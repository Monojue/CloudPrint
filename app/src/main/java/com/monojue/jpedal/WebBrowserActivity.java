package com.monojue.jpedal;



import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import static com.monojue.jpedal.R.mipmap.ic_launcher_round;

public class WebBrowserActivity extends AppCompatActivity {

    WebView webView;

    EditText editURl;
    SwipeRefreshLayout refreshLayout;
    ImageButton load;
    ProgressBar progressBar;
    Button print;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_browser);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(ic_launcher_round);

        webView = (WebView) findViewById(R.id.webView);
        editURl = (EditText) findViewById(R.id.editURL);
        load = (ImageButton) findViewById(R.id.load);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        print = (Button) findViewById(R.id.print_btn);

        editURl.setOnKeyListener(new View.OnKeyListener() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                if (keyEvent.getKeyCode() == keyEvent.KEYCODE_ENTER){
                    loadURL();
                }
                return false;
            }
        });

        print.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                createWebPrintJob(webView);
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onRefresh() {
                loadURL();
            }
        });

        progressBar.setVisibility(View.GONE);
        editURl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editURl.selectAll();
            }
        });

        load.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View view) {
                loadURL();
                hideKeyboard();
            }
        });






    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void loadURL() {

        String address = editURl.getText().toString();

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setWebViewClient(new webclient());

        if (!isNetworkAvailable()){
            Toast.makeText(this, "Network is not Available!", Toast.LENGTH_SHORT).show();
        }
        if (address.matches("")){
            Toast.makeText(this, "Enter the URL!", Toast.LENGTH_SHORT).show();

        }
        if (isNetworkAvailable() && !address.matches("")){
            progressBar.setVisibility(View.VISIBLE);
            webView.loadUrl("http://" + address );
        }

    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null&&activeNetworkInfo.isConnected();
    }



    private class webclient extends WebViewClient {

        public webclient() {
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar.setVisibility(View.VISIBLE);
            hideKeyboard();
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            editURl.setText("");
            editURl.setText(url.substring(url.indexOf("//")+2));

            progressBar.setVisibility(View.GONE);
            refreshLayout.setRefreshing(false);
            super.onPageFinished(view, url);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void createWebPrintJob(WebView webView) {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

        // Create a print job with name and adapter instance
        String jobName = webView.getTitle();


        printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
//        PrintJob printJob = printManager.print(jobName, printAdapter,
//                new PrintAttributes.Builder().build());

        // Save the job object for later status checking
//        mPrintJobs.add(printJob);
    }
    boolean doublePressed = false;
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()){
            webView.goBack();
        }else{

            if (doublePressed){
                super.onBackPressed();
                return;
            }

            this.doublePressed = true;
            Toast.makeText(this, "Press again to Exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doublePressed = false;
                }
            },2000);
        }
    }

    public void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view == null){
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}