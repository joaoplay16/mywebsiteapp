package com.example.joao.myapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private Snackbar  snackbar;
    private SwipeRefreshLayout refreshLayout;
    private CoordinatorLayout coordinatorLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        refreshLayout = findViewById(R.id.swipeLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
            executeAsync();
            }
    });
        executeAsync();
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        boolean primeiraVez = true;
        @Override
        public void onReceive(Context context, Intent intent) {

            if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())
                    && primeiraVez == false){
                    executeAsync();
                    primeiraVez = false;
            }
        }
    };

    private boolean temConexao(){
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (cm != null && info != null && info.isConnected());
    }

    private void alertaDeConexao(String mensagem){
        snackbar =  Snackbar.make(coordinatorLayout,
                                                            mensagem, Snackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(Color.RED);
        View sbview = snackbar.getView();
        TextView textView = sbview.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.setAction("conectar", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executeAsync();
            }
        }).show();
        refreshLayout.setRefreshing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }



    private void carregarPagina(){
        WebView webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.loadUrl("https://m.youtube.com");
        webView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
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
                    Log.i("NETWORK", "Abrindo conexao");
                } catch (IOException e) {
                    Log.i("NETWORK", "Error checking internet connection",e.getCause());
                    publishProgress(false);
                }
            } else {
                alertaDeConexao("Nenhuma conexão ativa");
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
            super.onProgressUpdate(values);
            if (values[0]) {
                carregarPagina();
                refreshLayout.setRefreshing(false);
                if(snackbar != null) snackbar.dismiss();
            }else {
                alertaDeConexao("Sem acesso à internet");
                refreshLayout.setRefreshing(false);
            }
        }
    }
}
