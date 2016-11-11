package com.orygenapps.sema.data;

import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.orygenapps.sema.CheckingVersionsEvent;
import com.orygenapps.sema.ProgramsUpdatedEvent;
import com.orygenapps.sema.SurveyAlarmManager;
import com.orygenapps.sema.UpdatingDatabaseEvent;
import com.orygenapps.sema.activity.SEMAApplication;
import com.orygenapps.sema.activity.SEMAService;
import com.orygenapps.sema.SendingAnswersEvent;
import com.orygenapps.sema.SyncJobFailedEvent;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedInput;

/**
 * Created by starehe on 26/03/15.
 */
public class CreateAdHocAnswerSet extends Job {

    SEMAApplication mApplication;
    SEMAService mService;
    SurveyDataManager mDataManager;
    SurveyAlarmManager mSurveyAlarmManager;
    public static final int PRIORITY = 1;

    EventBus bus;

    public CreateAdHocAnswerSet(String surveyUuid, String programUuid) {
        super(new Params(PRIORITY).requireNetwork().persist());
    }

    @Override
    public void onAdded() {
        // Job has been saved to disk.
    }

    @Override
    public void onRun() throws Throwable {

        mApplication = SEMAApplication.getInstance();
        mService = mApplication.getAPI();
        mDataManager = SEMAApplication.getInstance().getDataManager();
        mSurveyAlarmManager = SEMAApplication.getInstance().getSurveyAlarmManager();
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        // An error occurred in onRun.
        // Return value determines whether this job should retry running (true) or abort (false).
        return false; // TODO: consider adding a retry process
    }
    @Override
    protected void onCancel() {
        // Job has exceeded retry attempts or shouldReRunOnThrowable() has returned false.
    }
}
