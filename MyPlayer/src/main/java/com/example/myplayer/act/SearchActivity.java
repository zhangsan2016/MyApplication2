package com.example.myplayer.act;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myplayer.R;
import com.example.myplayer.act.adapter.SearchAdapter;
import com.example.myplayer.act.domain.SearchBean;
import com.example.myplayer.act.utils.Constants;
import com.example.myplayer.act.utils.JsonParser;
import com.google.gson.Gson;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by ldgd on 2017/3/28.
 * 介绍：搜索页面
 */

public class SearchActivity extends Activity {
    private EditText etInput;
    private ImageView ivVoice;
    private TextView tvSearch;
    private TextView tvNodata;
    private ProgressBar progressBar;
    private ListView listview;

    private SearchAdapter adapter;
    private String url;
    private List<SearchBean.ItemsBean> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        findViews();

    }


    private void findViews() {
        etInput = (EditText) findViewById(R.id.et_input);
        ivVoice = (ImageView) findViewById(R.id.iv_voice);
        tvSearch = (TextView) findViewById(R.id.tv_search);
        tvNodata =  (TextView) findViewById(R.id.tv_nodata);
        listview = (ListView) this.findViewById(R.id.listview);
        progressBar =(ProgressBar) findViewById(R.id.progressBar);

        MyOnClickListener myOnClickListener = new MyOnClickListener();
        ivVoice.setOnClickListener(myOnClickListener);
        tvSearch.setOnClickListener(myOnClickListener);

    }


    class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_voice://语音输入
                    //    showDialog();
                  //  dictation();
                    // Toast.makeText(SearchActivity.this, "语音输入", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.tv_search://搜索
//                    Toast.makeText(SearchActivity.this, "搜索", Toast.LENGTH_SHORT).show();
                      searchText();
                    break;
            }
        }
    }

    private void searchText() {
       String text =  etInput.getText().toString().trim();
        if(!text.isEmpty()){
            try {
                text =  URLEncoder.encode(text,"utf-8");
                url = Constants.SEARCH_URL + text;
                getDataFromNet();

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }

    }

    private void getDataFromNet() {
        progressBar.setVisibility(View.VISIBLE);
        RequestParams requestParams = new RequestParams(url);
        x.http().get(requestParams,new Callback.CommonCallback<String>(){

            @Override
            public void onSuccess(String result) {
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                progressBar.setVisibility(View.GONE);
            }
        });


    }

    private void processData(String result) {
        SearchBean searchBean = parsedJson(result);
        items = searchBean.getItems();
        showData();
    }

    private void showData() {
        if(items != null && items.size() > 0){
            adapter = new SearchAdapter(this,items);
            listview.setAdapter(adapter);
            tvNodata.setVisibility(View.GONE);
        }else{
            tvNodata.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
        tvNodata.setVisibility(View.GONE);
    }

    private SearchBean parsedJson(String result) {
        return new Gson().fromJson(result,SearchBean.class);
    }

    private void showDialog() {
        //1.创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(SearchActivity.this, new MyInitListener());
        //2.设置accent、 language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        //若要将UI控件用于语义理解，必须添加以下参数设置，设置之后onResult回调返回将是语义理解
        //结果
        // mDialog.setParameter("asr_sch", "1");
        // mDialog.setParameter("nlp_version", "2.0");
        //3.设置回调接口
        mDialog.setListener(new MyRecognizerDialogListener());
        //4.显示dialog，接收语音输入
        mDialog.show();

    }

    private void dictation() {
        //1.创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(this, new MyInitListener());
        //2.设置accent、 language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        //若要将UI控件用于语义理解，必须添加以下参数设置，设置之后onResult回调返回将是语义理解
        //结果
        // mDialog.setParameter("asr_sch", "1");
        // mDialog.setParameter("nlp_version", "2.0");
        //3.设置回调接口
        mDialog.setListener(new MyRecognizerDialogListener());
        //4.显示dialog，接收语音输入
        mDialog.show();


    }

    public class MyInitListener implements InitListener {

        @Override
        public void onInit(int code) {

            Log.d("myapplication", "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {

                Toast.makeText(SearchActivity.this, "初始化失败，错误码：" + code, Toast.LENGTH_LONG);
            }

        }
    }

    public class MyRecognizerDialogListener implements RecognizerDialogListener {
        /**
         * 听写结束
         *
         * @param results
         * @param b
         */
        @Override
        public void onResult(RecognizerResult results, boolean b) {
            Log.e("aa", "recognizerResult = " + results.getResultString());

        }

        @Override
        public void onError(SpeechError speechError) {

        }
    }
}
