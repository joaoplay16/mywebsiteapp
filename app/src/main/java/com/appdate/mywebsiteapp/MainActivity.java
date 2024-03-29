package com.appdate.mywebsiteapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @Autor João Pedro
 *
 *
 * */
public class MainActivity extends AppCompatActivity {


    /**
     ** {@code MYWEBSITE} substitua pela url do seu site.
     **/
    public static final String MYWEBSITE = "https://www.bbc.com";

    private Snackbar snackbar;
    private WebView webView;
    private SwipeRefreshLayout refreshLayout;
    private CoordinatorLayout coordinatorLayout;
    private NetworkReceiver networkReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webview);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        refreshLayout = findViewById(R.id.swipeLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                    webView.reload();
                if (snackbar != null)
                        snackbar.dismiss();
                refreshLayout.setRefreshing(false);
            }
        });

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        }else {
            executeAsync();
        }

        networkReceiver = new NetworkReceiver();
    }

    class NetworkReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
                if (!temConexao())
                    alertaDeConexao("Sem conexão");
            }
        }
    }

    private boolean temConexao(){
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private void alertaDeConexao(String mensagem){
        snackbar =  Snackbar.make(coordinatorLayout, mensagem, Snackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(Color.RED);
        View sbview = snackbar.getView();
        TextView textView = sbview.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.setAction(getString(R.string.connect), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executeAsync();
            }
        }).show();
        refreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(webView.canGoBack()) {
                webView.goBack();
            }else{
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void carregarPagina(){
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setAppCachePath(this.getApplicationContext().getCacheDir().getPath());
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });
        webView.loadUrl(MYWEBSITE);
    }

    private void  executeAsync(){
            ConexaoAsync conexaoAsync = new ConexaoAsync();
            conexaoAsync.execute(temConexao());
    }

    class ConexaoAsync extends AsyncTask<Boolean, Boolean, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... booleans) {
            if(booleans[0]){
                try {
                    HttpURLConnection urlc = (HttpURLConnection)
                            (new URL("http://clients3.google.com/generate_204")
                                    .openConnection());
                    urlc.setRequestProperty("User-Agent", "Android");
                    urlc.setRequestProperty("Connection", "close");
                    urlc.setConnectTimeout(1500);
                    urlc.connect();
                    boolean conectado = (urlc.getResponseCode() == 204 &&
                            urlc.getContentLength() == 0);
                    if(conectado){
                        publishProgress(true);
                    }else {
                        publishProgress(false);
                    }
                    Log.i("CONEXAO", "Abrindo conexão");
                } catch (IOException e) {
                    Log.i("CONEXAO", "Error checking internet connection",e.getCause());
                    publishProgress(false);
                }
            } else {
                publishProgress(false);
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
            super.onProgressUpdate(values);
            if (values[0]) {
                carregarPagina();
                if(snackbar != null) snackbar.dismiss();
                refreshLayout.setRefreshing(false);
            }else {
                alertaDeConexao(getString(R.string.connection_alert));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) {
            webView.saveState(outState);
        }
    }

}
