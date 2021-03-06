package com.iyuce.itoefl.Control.Listening.Activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.iyuce.itoefl.BaseActivity;
import com.iyuce.itoefl.Common.Constants;
import com.iyuce.itoefl.R;
import com.iyuce.itoefl.Utils.DbUtil;
import com.iyuce.itoefl.Utils.LogUtil;
import com.iyuce.itoefl.Utils.SDCardUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by LeBang on 2017/1/24
 */
public class PageReadyActivity extends BaseActivity {

    private TextView mTxtHeadTitle, mTxtEnglish, mTxtChinese, mTxtCategory, mTxtReview, mTxtReviewEntry;
    private ImageView mImgLevel;
    private String local_paper_code, local_path, local_music_question;
    private String title_chinese, title_english, img_scene, img_level;

    private ArrayList<String> mSortList;
    private ArrayList<String> mQuestionIdList;
    private ArrayList<String> mQuestionTypeList;
    private ArrayList<String> mQuestionContentList;
    private ArrayList<String> mUserSelectList;
    private ArrayList<String> mAnswerList;
    private ArrayList<String> mBingoList;
    private ArrayList<String> mTimeCountList;

    private ArrayList<String> mMusicAnswerList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_ready);

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initView() {
        local_path = getIntent().getStringExtra("local_path");
        //不从表中查，直接拼装上一级的section和module
        String local_section = getIntent().getStringExtra("local_code");
        String local_module = getIntent().getStringExtra("local_module");
        local_paper_code = local_section + "_" + local_module;
        local_music_question = getIntent().getStringExtra("local_music_question"); //主音频名称

        String path = SDCardUtil.getExercisePath();
        String tpo_path = path + File.separator + Constants.SQLITE_TPO;
        SQLiteDatabase mDatabase = DbUtil.getHelper(this, tpo_path).getWritableDatabase();
        String sql_query;
        sql_query = "select " + Constants.QuestionGrade + " from " + Constants.TABLE_PAPER_RULE + " where " + Constants.PaperCode + " = ? and " + Constants.RuleName + " = ?";
        img_level = DbUtil.cursorToNotNullString(mDatabase.rawQuery(sql_query, new String[]{local_section, local_module}));
        sql_query = "select " + Constants.RuleTitle + " from " + Constants.TABLE_PAPER_RULE + " where " + Constants.PaperCode + " = ? and " + Constants.RuleName + " = ?";
        title_chinese = DbUtil.cursorToNotNullString(mDatabase.rawQuery(sql_query, new String[]{local_section, local_module}));
        sql_query = "select " + Constants.RuleTitle_En + " from " + Constants.TABLE_PAPER_RULE + " where " + Constants.PaperCode + " = ? and " + Constants.RuleName + " = ?";
        title_english = DbUtil.cursorToNotNullString(mDatabase.rawQuery(sql_query, new String[]{local_section, local_module}));
        sql_query = "select " + Constants.MusicPicture + " from " + Constants.TABLE_PAPER_RULE + " where " + Constants.PaperCode + " = ? and " + Constants.RuleName + " = ?";
        img_scene = DbUtil.cursorToNotNullString(mDatabase.rawQuery(sql_query, new String[]{local_section, local_module}));
        mDatabase.close();

        findViewById(R.id.txt_header_title_menu).setVisibility(View.GONE);
        findViewById(R.id.imgbtn_header_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTxtHeadTitle = (TextView) findViewById(R.id.txt_header_title_item);
        mTxtEnglish = (TextView) findViewById(R.id.txt_activity_page_ready_title_english);
        mTxtChinese = (TextView) findViewById(R.id.txt_activity_page_ready_title_chinese);
        mTxtCategory = (TextView) findViewById(R.id.txt_activity_page_ready_title_category);
        mTxtReview = (TextView) findViewById(R.id.txt_activity_page_ready_review);
        mTxtReviewEntry = (TextView) findViewById(R.id.txt_activity_page_ready_review_entry);
        mImgLevel = (ImageView) findViewById(R.id.img_activity_page_ready_title_level);

        mTxtHeadTitle.setText(local_paper_code);
        mTxtChinese.setText(title_chinese);
        mTxtEnglish.setText(title_english);
        switch (img_level) {
            case "易":
                Glide.with(this).load(R.mipmap.icon_level_easy).into(mImgLevel);
                break;
            case "中":
                Glide.with(this).load(R.mipmap.icon_level_middle).into(mImgLevel);
                break;
            case "难":
                Glide.with(this).load(R.mipmap.icon_level_difficult).into(mImgLevel);
                break;
            default:
                Glide.with(this).load(R.mipmap.icon_level_difficult).into(mImgLevel);
        }
    }

    /**
     * 查用户操作表数据
     */
    private void initData() {
        String downloaded_sql_path = SDCardUtil.getExercisePath() + File.separator + Constants.SQLITE_DOWNLOAD;
        SQLiteDatabase mDatabase = DbUtil.getHelper(this, downloaded_sql_path).getWritableDatabase();
        String isNone = DbUtil.queryToString(mDatabase, Constants.TABLE_SQLITE_MASTER, Constants.NAME, Constants.TABLE_NAME, Constants.TABLE_ALREADY_PRACTICED);
        if (TextUtils.equals(isNone, Constants.NONE)) {
            mDatabase.close();
            mTxtReview.setText("您还没有练习过哦，赶快开始吧!");
            mTxtReviewEntry.setVisibility(View.GONE);
            mTxtReview.setClickable(false);
            return;
        }
        //TODO local_paper_code这个查询条件可能要改细，改成两个拼装的,
        mSortList = DbUtil.queryToArrayList(mDatabase, Constants.TABLE_ALREADY_PRACTICED, Constants.Sort, Constants.RuleName + " =? ", local_paper_code);
        mQuestionIdList = DbUtil.queryToArrayList(mDatabase, Constants.TABLE_ALREADY_PRACTICED, Constants.QuestionId, Constants.RuleName + " =? ", local_paper_code);
        mQuestionTypeList = DbUtil.queryToArrayList(mDatabase, Constants.TABLE_ALREADY_PRACTICED, Constants.QuestionType, Constants.RuleName + " =? ", local_paper_code);
        mQuestionContentList = DbUtil.queryToArrayList(mDatabase, Constants.TABLE_ALREADY_PRACTICED, Constants.Content, Constants.RuleName + " =? ", local_paper_code);
        mUserSelectList = DbUtil.queryToArrayList(mDatabase, Constants.TABLE_ALREADY_PRACTICED, Constants.UserSelect, Constants.RuleName + " =? ", local_paper_code);
        mAnswerList = DbUtil.queryToArrayList(mDatabase, Constants.TABLE_ALREADY_PRACTICED, Constants.Answer, Constants.RuleName + " =? ", local_paper_code);
        mBingoList = DbUtil.queryToArrayList(mDatabase, Constants.TABLE_ALREADY_PRACTICED, Constants.Bingo, Constants.RuleName + " =? ", local_paper_code);
        mTimeCountList = DbUtil.queryToArrayList(mDatabase, Constants.TABLE_ALREADY_PRACTICED, Constants.TimeCount, Constants.RuleName + " =? ", local_paper_code);
        mDatabase.close();
        LogUtil.i("mUserSelecttList" + mUserSelectList);

        if (mTimeCountList.size() == 0) {
            mTxtReview.setText("您还没有练习过哦，赶紧开始吧!");
            mTxtReviewEntry.setVisibility(View.GONE);
            mTxtReview.setClickable(false);
            return;
        }

        //装载上次做题记录
        int total = 0;
        for (int i = 0; i < mBingoList.size(); i++) {
            if (mBingoList.get(i).equals("true")) {
                total++;
            }
        }
        String result_review = "上次做题结果 : " + total + "/" + mBingoList.size();
        mTxtReviewEntry.setVisibility(View.VISIBLE);
//        mTxtReview.setClickable(true);
        mTxtReview.setText(result_review);
    }

    public void showResult(View view) {
        Intent intent = new Intent(this, DoResultActivity.class);
        intent.putExtra("local_path", local_path);
        intent.putExtra("title_chinese", title_chinese);
        intent.putExtra("title_english", title_english);
        intent.putExtra(Constants.PaperCode, local_paper_code);
        intent.putExtra(Constants.MusicQuestion, local_music_question);

        intent.putStringArrayListExtra("mAnswerList", mAnswerList);
        intent.putStringArrayListExtra("mBingoList", mBingoList);
        intent.putStringArrayListExtra("mSelectedAnswerList", mUserSelectList);
        intent.putStringArrayListExtra("mTimeCountList", mTimeCountList);
        intent.putStringArrayListExtra("mSortList", mSortList);
        intent.putStringArrayListExtra("mQuestionIdList", mQuestionIdList);
        intent.putStringArrayListExtra("mQuestionTypeList", mQuestionTypeList);
        intent.putStringArrayListExtra("mQuestionContentList", mQuestionContentList);
        //分段录音
//        intent.putStringArrayListExtra(Constants.MusicAnswer, mMusicAnswerList);
        startActivity(intent);
    }

    public void beginPractice(View view) {
        Intent intent = new Intent(this, DoQuestionReadyActivity.class);
        intent.putExtra("img_scene", img_scene);
        intent.putExtra("title_chinese", title_chinese);
        intent.putExtra("title_english", title_english);
        intent.putExtra("local_path", local_path);
        intent.putExtra(Constants.PaperCode, local_paper_code);
        intent.putExtra(Constants.MusicQuestion, local_music_question);
        startActivity(intent);
    }

    public void beginListen(View view) {
//        DbHelper mHelper = DbUtil.getHelper(this, "/storage/emulated/0/ITOEFL_JSON/user.db", Constants.DATABASE_VERSION);
//        SQLiteDatabase mDatabase1 = mHelper.getWritableDatabase();
//        LogUtil.i("long = insert " + DbUtil.insert(mDatabase1, Constants.TABLE_USER, "dream", "free"));
//        LogUtil.i("int update = " + DbUtil.update(mDatabase1, Constants.TABLE_USER, "dream", "eating", "_id >5"));
//        LogUtil.i("int delete = " + DbUtil.delete(mDatabase1, Constants.TABLE_USER, "dream = ", new String[]{"eating"}));
//        DbUtil.createTable(mDatabase1, Constants.TABLE_USER, "people", "dream");
//        LogUtil.i("list = column_1" + DbUtil.queryToArrayList(mDatabase1, Constants.TABLE_USER, null, 0).toString());
//        LogUtil.i("list = dream" + DbUtil.queryToArrayList(mDatabase1, Constants.TABLE_USER, null, "dream").toString());
//        mDatabase1.close();
    }
}