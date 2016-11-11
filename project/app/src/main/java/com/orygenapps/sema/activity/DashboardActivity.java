package com.orygenapps.sema.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.merge.MergeAdapter;
import com.orygenapps.sema.CheckingVersionsEvent;
import com.orygenapps.sema.NoChangeEvent;
import com.orygenapps.sema.ProgramsUpdatedEvent;
import com.orygenapps.sema.R;
import com.orygenapps.sema.SendingAnswersEvent;
import com.orygenapps.sema.SyncFinishedEvent;
import com.orygenapps.sema.SyncJobFailedEvent;
import com.orygenapps.sema.SyncStartedEvent;
import com.orygenapps.sema.data.AnswerSet;
import com.orygenapps.sema.data.AnswerSetAdapter;
import com.orygenapps.sema.data.Program;
import com.orygenapps.sema.data.ProgramAdapter;
import com.orygenapps.sema.data.SurveyDataManager;
import com.orygenapps.sema.UpdatingDatabaseEvent;
import com.orygenapps.sema.data.SyncJob;
import com.orygenapps.sema.data.TimeConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmResults;


public class DashboardActivity extends ActionBarActivity {

    private SEMAApplication mApplication;
    private AnswerSetAdapter mActiveSurveysAdapter;
    private ProgramAdapter mProgramsAdapter;
    private MergeAdapter mMergeAdapter;
    private SurveyDataManager mDataManager;
    private ArrayList<AnswerSet> mSurveys = new ArrayList<AnswerSet>();
    private ArrayList<Program> mPrograms = new ArrayList<Program>();
    private View mStreakView;
    private View mSurveyHeaderView;
    private View mSurveyEmptyView;
    private View mProgramHeaderView;
    private View mProgramEmptyView;
    private View mVersionView;
    private long syncInterval = 20000;
    //    private boolean alarmsRescheduled = false;
    private Handler mHandler;
    private Runnable mRunnable;
    private int refreshInterval = 30000; // 30 seconds
    private Realm mRealm;

    private EventBus mBus;

    @Bind(R.id.listView) ListView mListView;
    @Bind(R.id.syncStatusTextView) TextView mSyncStatusTextView;
    @Bind(R.id.syncTextButton) TextView mSyncTextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);
        ButterKnife.bind(this);

        mRealm = Realm.getDefaultInstance();

        mApplication = ((SEMAApplication)getApplication());

        mDataManager = ((SEMAApplication)getApplication()).getDataManager();

        mActiveSurveysAdapter = new AnswerSetAdapter(this, mSurveys);
        mProgramsAdapter = new ProgramAdapter(this, mPrograms);
        mMergeAdapter = new MergeAdapter();

        // Inflate a view header
        mStreakView = getLayoutInflater().inflate(R.layout.list_streak_view, null);
        TextView currentStreakView = (TextView)mStreakView.findViewById(R.id.dashboard_current_streak);
        TextView longestStreakView = (TextView)mStreakView.findViewById(R.id.dashboard_longest_streak);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        int currentStreak = sharedPref.getInt("current_streak", 0);
        int longestStreak = sharedPref.getInt("longest_streak", currentStreak);
        currentStreakView.setText(String.valueOf(currentStreak));
        longestStreakView.setText(String.valueOf(longestStreak));
        mMergeAdapter.addView(mStreakView);

        // Inflate a view header
        mSurveyHeaderView = getLayoutInflater().inflate(R.layout.list_header_view, null);
        TextView surveyHeaderTextView = (TextView)mSurveyHeaderView.findViewById(R.id.header_text);
        surveyHeaderTextView.setText("Active Surveys");

        mMergeAdapter.addView(mSurveyHeaderView);
        mMergeAdapter.addAdapter(mActiveSurveysAdapter);

        // Inflate a view header
        mSurveyEmptyView = getLayoutInflater().inflate(R.layout.list_notification_view, null);
        TextView surveyInfoTextView = (TextView)mSurveyEmptyView.findViewById(R.id.info_text);
        surveyInfoTextView.setText("You have no surveys to complete.");
        mMergeAdapter.addView(mSurveyEmptyView);

        // Inflate a view header
        mProgramHeaderView = getLayoutInflater().inflate(R.layout.list_header_view, null);
        TextView programHeaderTextView = (TextView)mProgramHeaderView.findViewById(R.id.header_text);
        programHeaderTextView.setText("Programs");

        mMergeAdapter.addView(mProgramHeaderView);
        mMergeAdapter.addAdapter(mProgramsAdapter);

        // Inflate a view header
        mProgramEmptyView = getLayoutInflater().inflate(R.layout.list_notification_view, null);
        TextView programInfoTextView = (TextView)mProgramEmptyView.findViewById(R.id.info_text);
        programInfoTextView.setText("You have no active programs");
        mMergeAdapter.addView(mProgramEmptyView);

        mListView.setAdapter(mMergeAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                int pos = position;

                int surveyListStart = 2;
                int programListStart = surveyListStart + mSurveys.size() + (mSurveys.size() > 0 ? 1 : 2);

                if (pos >= surveyListStart && pos < (surveyListStart + mSurveys.size())) { // Skip the header
                    AnswerSet set = mActiveSurveysAdapter.getItem(pos - surveyListStart);

                    long timestamp = set.getCreatedTimestamp();

                    Intent intent = new Intent(DashboardActivity.this, SurveyActivity.class);
                    intent.putExtra("programId", set.getDbProgramId());
                    intent.putExtra("answerSetUUID", set.getUuid());
                    startActivity(intent);
                } else if (pos >= programListStart && pos < (programListStart + mPrograms.size())) { // Skip the second header
                    Program program = mProgramsAdapter.getItem(pos - programListStart);

                    Intent intent = new Intent(DashboardActivity.this, ProgramActivity.class);
                    intent.putExtra("programId", program.getDbProgramId());
                    startActivity(intent);
                }
            }
        });

        // setup the refresh runnable
        mRunnable = new Runnable() {

            @Override
            public void run() {

                refreshSyncStatus();

                // refresh the dashboard
                refreshDashboard();

            }
        };

        // Setup the timer handler
        mHandler = new Handler();

        if (mApplication.mIsSynchronising) {
            disableSyncButton();
        }
        else {
            enableSyncButton();
        }
    }

    public void scheduleRefresh() {

        // remove any existing scheduled refresh and schedule the next one
        cancelRefresh();
        mHandler.postDelayed(mRunnable, refreshInterval);

    }

    public void cancelRefresh() {

        // remove any existing scheduled refresh
        mHandler.removeCallbacks(mRunnable);

    }

    public void onEventMainThread(SendingAnswersEvent event){
        Log.d("com.orygenapps.sema", "SendingAnswersEvent");
//        mProgressDialog.setMessage("Sending your answers...");
//        mProgressDialog.show();
        Toast.makeText(this, "Uploading your answers", Toast.LENGTH_SHORT).show();
    }

    public void onEventMainThread(CheckingVersionsEvent event){
        Log.d("com.orygenapps.sema", "CheckingVersionsEvent");
//        mProgressDialog.setMessage("Searching for program updates...");
//        mProgressDialog.show();
//        Toast.makeText(this, "Searching for updates", Toast.LENGTH_SHORT).show();
    }

    public void onEventMainThread(UpdatingDatabaseEvent event){
        Log.d("com.orygenapps.sema", "UpdatingDatabaseEvent");
//        mProgressDialog.setMessage("Updating programs...");
//        mProgressDialog.show();
        Toast.makeText(this, "Updating programs", Toast.LENGTH_SHORT).show();
    }

    public void onEventMainThread(ProgramsUpdatedEvent event){
        Log.d("com.orygenapps.sema", "ProgramsUpdatedEvent");
//        mProgressDialog.setMessage("Update completed.");
//        mProgressDialog.dismiss();
        Toast.makeText(this, "Update completed", Toast.LENGTH_SHORT).show();
//        refreshDashboard();
    }

    public void onEventMainThread(NoChangeEvent event){
        Log.d("com.orygenapps.sema", "NoChangeEvent");
//        mProgressDialog.setMessage("No update required.");
//        mProgressDialog.dismiss();
//        Toast.makeText(this, "No update required", Toast.LENGTH_SHORT).show();
//        refreshDashboard();
    }

    public void onEventMainThread(SyncStartedEvent event){
        Log.d("com.orygenapps.sema", "SyncStartedEvent");
        mSyncStatusTextView.setText("Synchronising...");
        disableSyncButton();
        refreshDashboard();
    }

    public void onEventMainThread(SyncFinishedEvent event){
        Log.d("com.orygenapps.sema", "SyncFinishedEvent");
        refreshSyncStatus();
        enableSyncButton();
        refreshDashboard();
    }

    public void onEventMainThread(SyncJobFailedEvent event){
        Log.d("com.orygenapps.sema", "SyncJobFailedEvent");
        Toast.makeText(this, "Unable to sync, We'll try again later", Toast.LENGTH_SHORT).show();
        refreshDashboard();
    }

    private void refreshSyncStatus() {

        if (mApplication.mIsSynchronising) {
            mSyncStatusTextView.setText("Synchronising...");
        }
        else {
            SharedPreferences sharedPref = mApplication.getSharedPreferences(mApplication.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            long lastSyncStartTimestamp = sharedPref.getLong(SyncJob.kSyncDataStartTimestamp, 0);
            long lastSyncEndTimestamp = sharedPref.getLong(SyncJob.kSyncDataEndTimestamp, 0);
            boolean lastSyncOffline = sharedPref.getBoolean(SyncJob.kSyncDataOffline, false);
            boolean lastSyncServerTestFailed = sharedPref.getBoolean(SyncJob.kSyncDataServerTestFailed, false);
            long lastSyncCount = sharedPref.getLong(SyncJob.kSyncDataCount, 0);
            long lastSyncSendCount = sharedPref.getLong(SyncJob.kSyncDataSendCount, 0);

            String timeSinceStartTime = TimeConverter.wordedTimeSinceTimestamp(lastSyncStartTimestamp); //[SEMA2API wordedTimeSinceTimestamp:lastSyncStartTimestamp];
            String timeSinceEndTime = TimeConverter.wordedTimeSinceTimestamp(lastSyncEndTimestamp); //[SEMA2API wordedTimeSinceTimestamp:lastSyncEndTimestamp];

            long remainingAnswerSetCount = getRemainingAnswerSetCount();

            boolean showRemaining = true;

            String result = "";
            if (lastSyncOffline) {
                result = "Sync failed " + timeSinceStartTime + ". Device was offline. ";
            } else if (lastSyncServerTestFailed) {
                result = "Sync failed " + timeSinceStartTime + ". Unable to connect. ";
            } else {
                result = "Synced " + timeSinceEndTime + ". ";
            }

            String uploads = "";
            if (lastSyncSendCount < lastSyncCount) {
                uploads = lastSyncSendCount + " of " + lastSyncCount + " surveys uploaded. ";
                showRemaining = false;
            } else if (lastSyncSendCount == 1) {
                uploads = "1 survey uploaded. ";
            } else if (lastSyncSendCount > 1) {
                uploads = lastSyncSendCount + " surveys uploaded. ";
            }

            String remaining = "";
            if (showRemaining) {
                if (remainingAnswerSetCount == 1) {
                    remaining = "1 survey to upload";
                } else if (remainingAnswerSetCount > 1) {
                    remaining = remainingAnswerSetCount + " surveys to upload";
                }
            }

            String syncStatus = result + uploads + remaining;
            mSyncStatusTextView.setText(syncStatus);
        }
    }

    private int getRemainingAnswerSetCount() {

        RealmResults<AnswerSet> remainingAnswerSets = mDataManager.getAnswerSetsToUpload(SEMAApplication.getInstance());

        return remainingAnswerSets.size();
    }

    private void refreshDashboard() {

        TextView currentStreakView = (TextView) mStreakView.findViewById(R.id.dashboard_current_streak);
        TextView longestStreakView = (TextView) mStreakView.findViewById(R.id.dashboard_longest_streak);
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        int currentStreak = sharedPref.getInt("current_streak", 0);
        int longestStreak = sharedPref.getInt("longest_streak", currentStreak);
        currentStreakView.setText(String.valueOf(currentStreak));
        longestStreakView.setText(String.valueOf(longestStreak));

        mSurveys.clear();

        RealmResults<AnswerSet> answerSets = mDataManager.getActiveAnswerSets(mRealm, DashboardActivity.this);
        for (int i = answerSets.size() - 1; i >= 0; i--) {
            AnswerSet answerSet = answerSets.get(i);
            if (answerSet.getSurvey() != null && answerSet.getSurvey().getProgram() != null) {
                mSurveys.add(answerSet);
                Log.w("com.orygenapps.sema", answerSet.getSurvey().getAnswerTriggerMode() + " AnswerSet # " + answerSet.getIteration() + " has uploaded timestamp " + answerSet.getUploadedTimestamp());
            }
        }

        mActiveSurveysAdapter.notifyDataSetChanged();
        if (mSurveys.size() > 0) {
            mMergeAdapter.setActive(mSurveyEmptyView, false);
            mMergeAdapter.setActive(mActiveSurveysAdapter, true);
        } else {
            mMergeAdapter.setActive(mSurveyEmptyView, true);
            mMergeAdapter.setActive(mActiveSurveysAdapter, false);
        }

        mPrograms.clear();

        for (Program program : mDataManager.getPrograms(DashboardActivity.this)) {
            mPrograms.add(program);
        }

        mProgramsAdapter.notifyDataSetChanged();
        if (mPrograms.size() > 0) {
            mProgramEmptyView.setVisibility(View.GONE);
        } else {
            mProgramEmptyView.setVisibility(View.VISIBLE);
        }

        mMergeAdapter.notifyDataSetChanged();

        // schedule the next refresh (e.g. in 30 seconds)
        scheduleRefresh();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DashboardActivity.this.receivedBroadcast(intent);
        }
    };

    private void receivedBroadcast(Intent intent) {

        if (intent.getAction() == "com.sema.survey.start.bcast") {

            refreshDashboard();

        }
        else if (intent.getAction() == "com.sema.survey.expiry.bcast") {

            refreshDashboard();

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onStop() {
        super.onStart();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {

        super.onResume();

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        long currentTimestamp = TimeConverter.currentTimeToMsTimestamp();
        long lastSyncTimestamp = sharedPref.getLong("last_sync_timestamp", 0);
        long timeDiff = currentTimestamp - lastSyncTimestamp;
        if (timeDiff > syncInterval) {
            if (!mApplication.mIsSynchronising) {

                // add a new sync job to the job manager
                SEMAApplication.getInstance().getJobManager().addJobInBackground(new SyncJob());
            }
        }
        else {
            refreshSyncStatus();
            refreshDashboard();
        }

        IntentFilter iff = new IntentFilter();

        iff.addAction("com.sema.survey.start.bcast");
        iff.addAction("com.sema.survey.expiry.bcast");

        registerReceiver(this.mBroadcastReceiver, iff);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(this.mBroadcastReceiver);

        // cancel any auto refresh
        cancelRefresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds mItems to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mRealm != null) {
            mRealm.close();
        }

    }

    public void onSyncTapped(View v) {

        if (!mApplication.mIsSynchronising) {

            // add a new sync job to the job manager
//            mSyncStatusTextView.setText("Synchronising...");
            SEMAApplication.getInstance().getJobManager().addJobInBackground(new SyncJob());
        }
    }

    public void enableSyncButton() {
        mSyncTextButton.setEnabled(true);
        mSyncTextButton.setClickable(true);
        mSyncTextButton.setTextColor(Color.rgb(0, 122, 255));
    }

    public void disableSyncButton() {
        mSyncTextButton.setEnabled(false);
        mSyncTextButton.setClickable(false);
        mSyncTextButton.setTextColor(Color.rgb(200, 200, 200));
    }
}
