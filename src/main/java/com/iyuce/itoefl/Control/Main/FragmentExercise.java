package com.iyuce.itoefl.Control.Main;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iyuce.itoefl.Common.Constants;
import com.iyuce.itoefl.Control.Listening.Activity.TopListeneringActivity;
import com.iyuce.itoefl.R;
import com.iyuce.itoefl.Utils.DbUtil;
import com.iyuce.itoefl.Utils.SDCardUtil;
import com.iyuce.itoefl.Utils.ToastUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by LeBang on 2017/1/22
 */
public class FragmentExercise extends Fragment
        implements View.OnClickListener, FragmentShowFlip.OnFlipShowListener {

    private CollapsingToolbarLayout mCollapLayout;
    private FloatingActionButton mFloatBtn;

    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private ArrayList<Fragment> mFragmentList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_exercise, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mFloatBtn = (FloatingActionButton) view.findViewById(R.id.fab_fragment_exercise);
        mFloatBtn.setOnClickListener(this);

        mCollapLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar_layout);
        //加载Gif图
//        mImg = (ImageView) view.findViewById(R.id.img_fragment_exercise_header);
//        Glide.with(getActivity()).load(R.raw.gif_header_exercise).into(mImg);
        //改变字体颜色
        mCollapLayout.setExpandedTitleColor(Color.parseColor("#FCFCFC"));
        mCollapLayout.setCollapsedTitleTextColor(Color.parseColor("#FF3370"));

        mViewPager = (ViewPager) view.findViewById(R.id.viewPager_fragment_exercise);
        FragmentShowFlip mFrag = new FragmentShowFlip();
        mFrag.setOnFlipShowListener(this);
        mFragmentList.add(mFrag);
        mFragmentList.add(new FragmentShowFlip());
        mAdapter = new FlipAdapter(getFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(mFragmentList.size() - 1);
    }

    private boolean decidedEnter() {
        String path = SDCardUtil.getExercisePath();
        String tpo_path = path + File.separator + Constants.SQLITE_TPO;

        SQLiteDatabase mDatabase = DbUtil.getHelper(getActivity(), tpo_path).getWritableDatabase();
        //从默认主表中查，是否有这张表
        String isNone_Paper = DbUtil.queryToString(mDatabase, Constants.TABLE_SQLITE_MASTER, Constants.NAME, Constants.TABLE_NAME, Constants.TABLE_PAPER);
        mDatabase.close();
        if (TextUtils.equals(isNone_Paper, Constants.NONE)) {
            ToastUtil.showMessage(getActivity(), "未找到所请求数据目录");
            return false;
        }
        return true;
    }

    private class FlipAdapter extends FragmentPagerAdapter {
        public FlipAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }
    }

    @Override
    public void onClick(View v) {
        if (decidedEnter())
            getActivity().startActivity(new Intent(getActivity(), TopListeneringActivity.class));
        else
            ToastUtil.showMessage(getActivity(), "努力加载数据中,请稍候");
    }

    @Override
    public void OnFlipShowClick() {
        if (decidedEnter())
            getActivity().startActivity(new Intent(getActivity(), TopListeneringActivity.class));
        else
            ToastUtil.showMessage(getActivity(), "努力加载数据中,请稍候");
    }
}