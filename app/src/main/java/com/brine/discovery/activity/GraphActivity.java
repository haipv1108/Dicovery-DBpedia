package com.brine.discovery.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.brine.discovery.AppController;
import com.brine.discovery.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class GraphActivity extends AppCompatActivity {
    private static final String TAG = GraphActivity.class.getCanonicalName();
    public static final String RECOMMENDEDURI = "recommend_uri";

    private CoordinatorLayout mCoordinatorLayout;
    private WebView mWebview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        String recommendedUri = getIntent().getStringExtra(RECOMMENDEDURI);
        List<String> fromUris = AppController.getInstance().getFromUriDecovery();

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mWebview = (WebView) findViewById(R.id.webview_graph);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setWebChromeClient(new WebChromeClient());
        mWebview.addJavascriptInterface(
                new JavaScriptInterface(this, fromUris, recommendedUri, mCoordinatorLayout),
                "JSInterface");

        mWebview.loadUrl("file:///android_asset/Graph_Gojs/index.html");
    }

    public class JavaScriptInterface{
        Context mContext;
        List<String> mFromUris;
        String mRecommendedUri;
        CoordinatorLayout mCoordinatorLayout;
        ProgressDialog mProgressDialog;

        JavaScriptInterface(Context context, List<String> fromUris,
                            String recommendedUri, CoordinatorLayout coordinatorLayout){
            this.mContext = context;
            this.mFromUris = fromUris;
            this.mRecommendedUri = recommendedUri;
            this.mCoordinatorLayout = coordinatorLayout;
        }

        @JavascriptInterface
        public String[] getFromUris() {
            String[] results = new String[5];
            for(int i = 0; i < mFromUris.size(); i++){
                results[i] = mFromUris.get(i);
            }
            return results;
        }

        @JavascriptInterface
        public int getLengthFromUris(){
            return mFromUris.size();
        }

        @JavascriptInterface
        public String getValueOfFromUris(int i){
            return mFromUris.get(i);
        }

        @JavascriptInterface
        public String getRecommendedUri() {
            return mRecommendedUri;
        }

        @JavascriptInterface
        public void showProgressDialog(){
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.show();
        }

        @JavascriptInterface
        public void dismissProgressDialog(){
            mProgressDialog.dismiss();
        }

        @JavascriptInterface
        public void showToast(String toast){
            Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
            Log.d("GraphActivity", "Toast: " + toast);
        }

        @JavascriptInterface
        public void showSnackBar(String label, String description){
            Snackbar snackbar = Snackbar
                    .make(mCoordinatorLayout, label + "\n" + description, Snackbar.LENGTH_LONG)
                    .setDuration(Snackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setMaxLines(6);
            snackbar.show();
        }
    }

}
