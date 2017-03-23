package com.iyuce.itoefl.Control.Main;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iyuce.itoefl.Common.Constants;
import com.iyuce.itoefl.Control.Listening.Activity.TopListeneringActivity;
import com.iyuce.itoefl.Control.Main.Adapter.DisCreteScrollAdapter;
import com.iyuce.itoefl.R;
import com.iyuce.itoefl.Utils.DbUtil;
import com.iyuce.itoefl.Utils.SDCardUtil;
import com.iyuce.itoefl.Utils.ToastUtil;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.Pivot;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by LeBang on 2017/1/22
 */
public class FragmentExercise extends Fragment
        implements View.OnClickListener, DisCreteScrollAdapter.OnScrollSelectListener {

    private CollapsingToolbarLayout mCollapLayout;
    private FloatingActionButton mFloatBtn;

    private DiscreteScrollView mDisScrollview;
    private DisCreteScrollAdapter mAdapter;
    private ArrayList<Integer> mImgList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_exercise, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mCollapLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar_layout);
        //改变字体颜色
        mCollapLayout.setExpandedTitleColor(Color.parseColor("#FCFCFC"));
        mCollapLayout.setCollapsedTitleTextColor(Color.parseColor("#FF3370"));

        mFloatBtn = (FloatingActionButton) view.findViewById(R.id.fab_fragment_exercise);
        mFloatBtn.setOnClickListener(this);

        mDisScrollview = (DiscreteScrollView) view.findViewById(R.id.discrete_scroll_fragment_exercise);

        mImgList.add(R.mipmap.img_bg_golden_gate_bridge);
        mImgList.add(R.mipmap.img_bg_sea_stone);
        mImgList.add(R.mipmap.img_background_fog_city);
        mAdapter = new DisCreteScrollAdapter(mImgList, getActivity());
        mAdapter.setOnScrollSelectListener(this);
        mDisScrollview.setAdapter(mAdapter);
        mDisScrollview.scrollToPosition(1);
        mDisScrollview.setItemTransformer(new ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER) // CENTER is a default one
                .setPivotY(Pivot.Y.BOTTOM) // CENTER is a default one
                .build());
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

    @Override
    public void onClick(View v) {
        if (decidedEnter())
            getActivity().startActivity(new Intent(getActivity(), TopListeneringActivity.class));
        else
            ToastUtil.showMessage(getActivity(), "努力加载数据中,请稍候");
    }

    @Override
    public void onSelect(int pos) {
        if (decidedEnter())
            getActivity().startActivity(new Intent(getActivity(), TopListeneringActivity.class));
        else
            ToastUtil.showMessage(getActivity(), "努力加载数据中,请稍候");
    }
}