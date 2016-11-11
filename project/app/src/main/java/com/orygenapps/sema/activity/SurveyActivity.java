package com.orygenapps.sema.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;

import com.cengalabs.flatui.FlatUI;
import com.cengalabs.flatui.views.FlatButton;
import com.orygenapps.sema.R;
import com.orygenapps.sema.data.Answer;
import com.orygenapps.sema.data.AnswerSet;
import com.orygenapps.sema.data.AnswerSetTriggerMode;
import com.orygenapps.sema.data.Program;
import com.orygenapps.sema.data.Question;
import com.orygenapps.sema.data.SyncJob;
import com.orygenapps.sema.data.TimeConverter;

import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmList;


public class SurveyActivity extends ActionBarActivity implements WidgetFragment.OnFragmentInteractionListener, TextWidgetFragment.OnValidationChangedListener {

    private SEMAApplication mApplication;
    private FlatButton mSubmitAnswerButton;
    private RealmList<Question> mOrderedQuestionList;
    private int mCurQuestionIndex;
    private int mNumQuestions;
    private WidgetFragment mWidgetFragment;
    private AnswerSet mAnswerSet;
    private float fadedAlpha = 0.4f;
    private Handler mHandler;
    private Runnable mRunnable;
    private int expiryCheckInterval = 10000; // 10 seconds

    private int programId;
    private String answerSetUUID;

    final int QUESTION_TYPE_TEXT = 0;
    final int QUESTION_TYPE_CHECKBOX = 1;
    final int QUESTION_TYPE_RADIO = 2;
    final int QUESTION_TYPE_SLIDER = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        Bundle bundle = getIntent().getExtras();

        programId = bundle.getInt("programId");
        answerSetUUID = bundle.getString("answerSetUUID");

        mApplication = ((SEMAApplication)getApplication());

        Realm realm = null;

        // setup the expiry check runnable
        mRunnable = new Runnable() {

            @Override
            public void run() {

                // exit if expired
                exitIfExpired();

            }
        };

        // Setup the timer handler
        mHandler = new Handler();

        try {
            realm = Realm.getDefaultInstance();

            mAnswerSet = realm.where(AnswerSet.class).equalTo("uuid", answerSetUUID).findFirst();

            if (mAnswerSet.getAnswerTriggerMode() == AnswerSetTriggerMode.ADHOC && mAnswerSet.getExpiryTimestamp() == -1) {

                realm.beginTransaction();

                // set the delivery timestamp to the current time
                long currentTimestamp = TimeConverter.currentTimeToMsTimestamp();
                mAnswerSet.setDeliveryTimestamp(currentTimestamp);

                // set the adhoc survey to expire in 15 minutes time
                long expiryTimestamp = TimeConverter.addMinutesToTimestamp(currentTimestamp, 15);
                mAnswerSet.setExpiryTimestamp(expiryTimestamp);

                realm.commitTransaction();

            }

            // remove outstanding alarms
            ((SEMAApplication) getApplication()).getSurveyAlarmManager().cancelAlarmsForAnswerSet(this, mAnswerSet);

            if (mAnswerSet.getUploadedTimestamp() == -1) {

                Program program = realm.where(Program.class).equalTo("dbProgramId", programId).findFirst();
                setTitle(program.getDisplayName());

                mOrderedQuestionList = mAnswerSet.getOrderedQuestionList();

                mNumQuestions = mOrderedQuestionList.size();

                mCurQuestionIndex = calculateCurrentQuestionIndex(mAnswerSet, mOrderedQuestionList);

                // tmp: exit to dashboard if no questions
                if (mCurQuestionIndex == -1) {
                    Intent i = new Intent(this, DashboardActivity.class);
                    startActivity(i);
                }

                mSubmitAnswerButton = (FlatButton) findViewById(R.id.survey_submit_answer_button);

                mSubmitAnswerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        submitAnswer();
                    }
                });

                setupQuestion();
            } else {
                finish();
            }
        }

        finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // cancel  auto expiry check
        cancelExpiryCheck();

    }

    @Override
    public void onBackPressed() {

    }

    // Check current progress through survey starting from the first question
    public int calculateCurrentQuestionIndex(AnswerSet answerSet, RealmList<Question> orderedQuestionList) {

        int i = 0;
        while (i < orderedQuestionList.size()) {

            Question question = orderedQuestionList.get(i);

            Answer existingAnswer = getExistingAnswer(answerSet, question);

            if (existingAnswer == null) {
                return i;
            }
            i++;
        }
        return -1; // indicates that all questions have been completed already
    }

    // TODO: confirm question is unique and not shared between sets etc
    public Answer getExistingAnswer(AnswerSet answerSet, Question question) { // may only need answerset and questionid

        Answer answer = null;
        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            answer = realm.where(Answer.class)
                    .equalTo("set.uuid", answerSet.getUuid())
                    .equalTo("dbSurveyId", answerSet.getDbSurveyId())
                    .equalTo("dbQuestionId", question.getDbQuestionId())
                    .findFirst();
        }

        finally {
            if (realm != null) {
                realm.close();
            }
        }

        return answer;
    }

    public void setupQuestion() {

        exitIfExpired();

        mSubmitAnswerButton.setEnabled(false);

        Question question = mOrderedQuestionList.get(mCurQuestionIndex);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("dbAnswerSetUUID", (String) mAnswerSet.getUuid());
        bundle.putInt("dbSurveyId", (int) mAnswerSet.getSurvey().getDbSurveyId());
        bundle.putInt("dbQuestionSetId", (int) question.getSet().getDbQuestionSetId());
        bundle.putInt("dbQuestionId", (int) question.getDbQuestionId());
        bundle.putInt("questionType", (int) question.getQuestionType());

        switch (question.getQuestionType()) {
            case QUESTION_TYPE_TEXT:
                mWidgetFragment = new TextWidgetFragment();
                break;
            case QUESTION_TYPE_CHECKBOX:
                mWidgetFragment = new CheckboxWidgetFragment();
                break;
            case QUESTION_TYPE_RADIO:
                mWidgetFragment = new RadioWidgetFragment();
                break;
            case QUESTION_TYPE_SLIDER:
                mWidgetFragment = new SliderWidgetFragment();
                break;
        }

        mWidgetFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.survey_widget_container, mWidgetFragment);
        fragmentTransaction.commit();

        mSubmitAnswerButton.setAlpha(fadedAlpha);
        if (isLastQuestion()) {
            mSubmitAnswerButton.setText("Save and submit survey");
            mSubmitAnswerButton.getAttributes().setTheme(FlatUI.CANDY, getResources());
        }
    }

    public void submitAnswer() {

        mWidgetFragment.saveAnswerToDb();

        dismissKeyboard(mWidgetFragment.getView()); // TODO: only need to do this if text question

        if (isLastQuestion()) {

            if (allAnswersAnswered()) {

                Realm realm = null;

                try {
                    realm = Realm.getDefaultInstance();
                    realm.beginTransaction();

                    String timezoneID = TimeZone.getDefault().getID();

                    mAnswerSet.setTimezone(timezoneID);

                    // update the completed timestamp
                    long currentTimestamp = TimeConverter.currentTimeToMsTimestamp();
                    mAnswerSet.setCompletedTimestamp(currentTimestamp);

                    realm.commitTransaction();

                    if (!mApplication.mIsSynchronising) {

                        // add a new sync job to the job manager
                        SEMAApplication.getInstance().getJobManager().addJobInBackground(new SyncJob());
                    }

                    // increment the streak
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    int currentStreak = sharedPref.getInt("current_streak", 0);
                    int longestStreak = sharedPref.getInt("longest_streak", currentStreak);
                    currentStreak++;
                    if (currentStreak > longestStreak) {
                        longestStreak = currentStreak;
                    }
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("current_streak", currentStreak);
                    editor.putInt("longest_streak", longestStreak);
                    editor.commit();

                    // remove the notification for this survey if it still exists
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(mAnswerSet.getStartAlarmRequestCode());

                    // Start activity
                    Intent i = new Intent(this, SubmitSurveyActivity.class);
                    startActivity(i);
                }

                finally {
                    if (realm != null) {
                        realm.close();
                    }
                }
            }
            else {
                // TODO: handle this
            }
        }
        else {
            mCurQuestionIndex++;
            setupQuestion();
        }
    }

    public boolean allAnswersAnswered() {
        return (calculateCurrentQuestionIndex(mAnswerSet, mOrderedQuestionList) == -1);
    }

    @Override
    public void onValidationChanged(boolean isValid) {

        if (isValid) { // fade in
            mSubmitAnswerButton.setAlpha(1f);
            mSubmitAnswerButton.setEnabled(true);
            AlphaAnimation fadeInAnim = new AlphaAnimation(fadedAlpha, 1f);
            fadeInAnim.setDuration(250);
            fadeInAnim.setFillAfter(true);
            mSubmitAnswerButton.startAnimation(fadeInAnim);
        }
        else { // fade out
            mSubmitAnswerButton.setEnabled(false);
            AlphaAnimation fadeOutAnim = new AlphaAnimation(1f, fadedAlpha);
            fadeOutAnim.setDuration(250);
            fadeOutAnim.setFillAfter(true);
            mSubmitAnswerButton.startAnimation(fadeOutAnim);
        }
    }

    public boolean isLastQuestion() {
        return (mCurQuestionIndex == mNumQuestions - 1);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_survey, menu);
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

    public void dismissKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void scheduleExpiryCheck() {

        // remove any existing scheduled expiry check and schedule the next one
        cancelExpiryCheck();
        mHandler.postDelayed(mRunnable, expiryCheckInterval);

    }

    public void cancelExpiryCheck() {

        // remove any existing scheduled expiry check
        mHandler.removeCallbacks(mRunnable);

    }

    @Override
     public void onResume() {
        super.onResume();

        exitIfExpired();

    }

    private void exitIfExpired() {

        // check the expiry of the current answerset
        long currentTimestamp = TimeConverter.currentTimeToMsTimestamp();
        if (mAnswerSet.getExpiryTimestamp() != -1 && mAnswerSet.getExpiryTimestamp() < currentTimestamp) { // the addition of the -1 check allows for adhoc surveys to be opened

            // exit to dashboard
            new AlertDialog.Builder(this)
                    .setTitle("Survey Expired")
                    .setMessage("This survey has expired")
                    .setPositiveButton("Return to dashboard", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }

        // schedule the next check (e.g. in 10 seconds)
        scheduleExpiryCheck();

    }
}
