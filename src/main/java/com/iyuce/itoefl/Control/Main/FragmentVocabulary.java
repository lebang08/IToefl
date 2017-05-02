package com.iyuce.itoefl.Control.Main;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iyuce.itoefl.Common.Constants;
import com.iyuce.itoefl.Control.Main.Adapter.VocabularyAdapter;
import com.iyuce.itoefl.Control.Vocabulary.VocabularyActivity;
import com.iyuce.itoefl.LazyFragment;
import com.iyuce.itoefl.Model.Vocabulary;
import com.iyuce.itoefl.R;
import com.iyuce.itoefl.Utils.DbUtil;
import com.iyuce.itoefl.Utils.HttpUtil;
import com.iyuce.itoefl.Utils.Interface.Http.DownLoadInterface;
import com.iyuce.itoefl.Utils.Interface.Http.RequestInterface;
import com.iyuce.itoefl.Utils.LogUtil;
import com.iyuce.itoefl.Utils.NetUtil;
import com.iyuce.itoefl.Utils.ParseJsonUtil;
import com.iyuce.itoefl.Utils.SDCardUtil;
import com.iyuce.itoefl.Utils.StringUtil;
import com.iyuce.itoefl.Utils.ToastUtil;

import java.io.File;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by LeBang on 2017/1/22
 */
public class FragmentVocabulary extends LazyFragment implements VocabularyAdapter.VocabularyListener {

    private RecyclerView mRecyclerView;
    private VocabularyAdapter mAdapter;
    private ArrayList<Vocabulary> mVocabularyList = new ArrayList<>();

    private String downloaded_sql_path;

    private boolean isPrepared;

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible) {
            return;
        }
        /**懒加载*/
        //网络已连接
        if (NetUtil.isConnected(getActivity())) {
            //通过网络初始化数据
            initHttpData();
            //但不是WIFI
            if (!NetUtil.isWifi(getActivity())) {
//                ToastUtil.showMessage(getActivity(), "当前不在WIFI环境");
            }
        } else {
            ToastUtil.showMessage(getActivity(), "未连接网络");
            initNoHttpData();
        }
        isPrepared = true;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.e("Vocabulary onCreateView = ");
        View view = inflater.inflate(R.layout.fragment_main_vocabulary, container, false);
        initView(view);

//         isPrepared = true;
//         lazyLoad();
        return view;
    }

    private void initView(View view) {
        //数据库路径
        downloaded_sql_path = SDCardUtil.getVocabularyPath() + File.separator + Constants.SQLITE_DOWNLOAD;

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_fragment_vocabulary);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void initHttpData() {
        HttpUtil.get(Constants.URL_GET_PDF_BOOK, new RequestInterface() {
            @Override
            public void doSuccess(String result, Call call, Response response) {
                mVocabularyList = ParseJsonUtil.parseVocabulary(result);
                mAdapter = new VocabularyAdapter(getActivity(), mVocabularyList);
                mAdapter.setOnVocabularyListener(FragmentVocabulary.this);
                mRecyclerView.setAdapter(mAdapter);
                //如果有数据库，就将数据存入数据库,没有，就创建，再存入

                if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    createDatabase();
                    saveDataIntoSql(mVocabularyList);
                }
            }
        });
    }

    private void initNoHttpData() {
        //创建数据库
        createDatabase();
        //读取
        mVocabularyList.clear();
        mVocabularyList = getDataFromSql();
        if (mVocabularyList == null) {
            ToastUtil.showMessage(getActivity(), "您还没有书籍哦，请链接网络重试");
        }
        mAdapter = new VocabularyAdapter(getActivity(), mVocabularyList);
        mAdapter.setOnVocabularyListener(FragmentVocabulary.this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private ArrayList<Vocabulary> getDataFromSql() {
        SQLiteDatabase mDatabase = DbUtil.getHelper(getActivity(), downloaded_sql_path).getReadableDatabase();
        String isNone_PDF = DbUtil.queryToString(mDatabase, Constants.TABLE_SQLITE_MASTER, Constants.NAME, Constants.TABLE_NAME, Constants.TABLE_PDF_DOWNLOAD);
        if (TextUtils.equals(isNone_PDF, Constants.NONE)) {
            mDatabase.close();
            return null;
        }
        Cursor cursor = mDatabase.query(Constants.TABLE_PDF_DOWNLOAD, null, null, null, null, null, null);
        if (cursor != null) {
            Vocabulary mVocabulary;
            while (cursor.moveToNext()) {
                mVocabulary = new Vocabulary();
                mVocabulary.title = cursor.getString(cursor.getColumnIndex(Constants.Title));
                mVocabulary.description = cursor.getString(cursor.getColumnIndex(Constants.Description));
                mVocabulary.path = cursor.getString(cursor.getColumnIndex(Constants.Path));
                mVocabulary.img = cursor.getString(cursor.getColumnIndex(Constants.PdfImage));
                mVocabulary.size = cursor.getLong(cursor.getColumnIndex(Constants.Size));
                mVocabulary.key = cursor.getInt(cursor.getColumnIndex(Constants.ID));
                mVocabulary.modify_at = cursor.getString(cursor.getColumnIndex(Constants.ModifyAt));
                mVocabulary.book_mark = cursor.getString(cursor.getColumnIndex(Constants.BookMark));
                mVocabularyList.add(mVocabulary);
            }
            cursor.close();
        }
        mDatabase.close();
        return mVocabularyList;
    }

    private void saveDataIntoSql(ArrayList<Vocabulary> mVocabularyList) {
        //Replace 插入或替代数据
        SQLiteDatabase mDatabase = DbUtil.getHelper(getActivity(), downloaded_sql_path).getReadableDatabase();
        for (int i = 0; i < mVocabularyList.size(); i++) {
            //判断是否存在，不存在则插入，存在则更新
            String sql_replace = "replace into " + Constants.TABLE_PDF_DOWNLOAD + " ("
                    + Constants.ID + ","
                    + Constants.UserId + " ,"
                    + Constants.Title + ","
                    + Constants.Description + ","
                    + Constants.PdfImage + ","
                    + Constants.ModifyAt + ","
                    + Constants.BookMark + ","
                    + Constants.Path + ","
                    + Constants.Size + ")values(\""
                    + mVocabularyList.get(i).key + "\",\""
                    + "user_default" + "\",\""
                    + StringUtil.changeSpecial(mVocabularyList.get(i).title) + "\",\""
                    + StringUtil.changeSpecial(mVocabularyList.get(i).description) + "\",\""
                    + StringUtil.changeSpecial(mVocabularyList.get(i).img) + "\",\""
                    + mVocabularyList.get(i).modify_at + "\",\""
                    + mVocabularyList.get(i).book_mark + "\",\""
                    + mVocabularyList.get(i).path + "\",\""
                    + mVocabularyList.get(i).size + "\" )";
            mDatabase.execSQL(sql_replace);
        }
        mDatabase.close();
    }

    private void createDatabase() {
        //打开或者创建我的本地数据库
        SQLiteDatabase mDatabase = DbUtil.getHelper(getActivity(), downloaded_sql_path).getReadableDatabase();
        String create = "create table if not exists " + Constants.TABLE_PDF_DOWNLOAD + "("
                + Constants.ID + " integer primary key,"
                + Constants.UserId + " text,"
                + Constants.Title + " text,"
                + Constants.Description + " text,"
                + Constants.PdfImage + " text,"
                + Constants.ModifyAt + " text,"
                + Constants.BookMark + " text,"
                + Constants.Path + " text,"
                + Constants.Size + " text)";
        mDatabase.execSQL(create);
        mDatabase.close();
    }

    //防止无权限时崩溃
    public boolean hasPermission(String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    @Override
    public void OnItemClick(int pos) {
        File file = new File(SDCardUtil.getVocabularyPath() + File.separator + mVocabularyList.get(pos).title + ".pdf");
        if (file.exists()) {
            if (mVocabularyList.get(pos).downLoading) {
                ToastUtil.showMessage(getActivity(), "下载中，请稍候...");
                return;
            }
            Intent intent = new Intent(getActivity(), VocabularyActivity.class);
            intent.putExtra("Vocabulary", mVocabularyList.get(pos));
            startActivity(intent);
        } else {
            downLoad(pos);
        }
    }

    @Override
    public void OnItemLongClick(int pos) {
        File file = new File(SDCardUtil.getVocabularyPath() + File.separator + mVocabularyList.get(pos).title + ".pdf");
        if (file.exists()) {
            deleteAlertDialog(pos, file);
        }
    }

    private void deleteAlertDialog(final int pos, final File file) {
        new AlertDialog.Builder(getActivity())
                .setTitle("删除该书本地缓存")
                .setMessage("确定要删除吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (file.delete()) {
                            ToastUtil.showMessage(getActivity(), "删除本地书籍成功");
                            TextView textView = (TextView) mRecyclerView.getChildAt(pos).findViewById(R.id.txt_item_vocabulary_download);
                            textView.setText("准备下载");
                            textView.setVisibility(View.VISIBLE);
                        } else {
                            ToastUtil.showMessage(getActivity(), "删除本地书籍失败");
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 下载
     */
    private void downLoad(final int pos) {
        ToastUtil.showMessage(getActivity(), "您希望下载" + mVocabularyList.get(pos).title + ",请稍等");
        HttpUtil.downLoad(mVocabularyList.get(pos).path, SDCardUtil.getVocabularyPath(), mVocabularyList.get(pos).title + ".pdf", new DownLoadInterface() {
            @Override
            public void inProgress(long currentSize, long totalSize, float progress, long networkSpeed) {
//                progressdialogcancel();
                TextView textView = (TextView) mRecyclerView.getChildAt(pos).findViewById(R.id.txt_item_vocabulary_download);
                textView.setText(((int) (progress * 100) + "%"));
            }

            @Override
            public void doSuccess(File file, Call call, Response response) {
                TextView textView = (TextView) mRecyclerView.getChildAt(pos).findViewById(R.id.txt_item_vocabulary_download);
                textView.setVisibility(View.GONE);
            }

            @Override
            public void onBefore() {
//                progressdialogshow(getActivity());
                mVocabularyList.get(pos).downLoading = true;
            }

            @Override
            public void onAfter() {
                mVocabularyList.get(pos).downLoading = false;
            }
        });
    }
}
