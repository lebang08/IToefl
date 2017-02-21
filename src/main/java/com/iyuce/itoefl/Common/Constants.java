package com.iyuce.itoefl.Common;

/**
 * Created by LeBang on 2017/1/23
 */
public class Constants {

    public static final String URL_WEB_ONE_TO_ONE = "http://www.iyuce.com/m/appjxy.html";

    //
    public static final String NONE = "none";

    //音频状态
    public static final int FLAG_AUDIO_PLAY = 0;
    public static final int FLAG_AUDIO_PAUSE = 1;

    //文件夹路径相关
    public static final String FILE_PATH_ITOEFL = "/ITOEFL";
    public static final String FILE_PATH_ITOEFL_EXERCISE = "/ITOEFL/EXERCISE";

    //数据库相关
    public static final int DATABASE_VERSION = 1;
    public static final String SQLITE_TPO = "TPO.sqlite";
    public static final String TABLE_TPO_PAPER = "tpopaper_table";
    public static final String SQLITE_DOWNLOAD = "downloaded.db";
    public static final String TABLE_ALREADY_DOWNLOAD = "downloaded_table";
    public static final String TABLE_ALREADY_PRACTICED = "practiced_table";

    //Exercise 相关的表
    public static final String TABLE_SQLITE_MASTER = "sqlite_master";

    public static final String TABLE_PAPER = "Iup_TpoPaper";
    public static final String TABLE_PAPER_RULE = "Iup_TpoPaperRule";
    public static final String TABLE_PAPER_QUESTION = "Iup_TpoPaperQuestion";
    public static final String TABLE_QUESTION = "Iup_TpoQuestion";
    public static final String TABLE_OPTION = "Iup_TpoOption";

    //Exercise 相关表中的字段column
    public static final String TABLE_NAME = "tbl_name";
    public static final String ID = "Id";
    public static final String PaperName = "PaperName";
    public static final String PaperId = "PaperId";
    public static final String PaperCode = "PaperCode";
    public static final String QuestionType = "QuestionType";
    public static final String RuleName = "RuleName";
    public static final String QuestionCount = "QuestionCount";
    public static final String Sort = "Sort";
    public static final String MusicQuestion = "MusicQuestion";

    public static final String QuestionId = "QuestionId";
    public static final String PaperRuleId = "PaperRuleId";
    public static final String MusicAnswer = "MusicAnswer";

    public static final String MasterId = "MasterId";
    public static final String Content = "Content";
    public static final String Answer = "Answer";
    public static final String Detial = "Detial";

    public static final String Code = "Code";
}