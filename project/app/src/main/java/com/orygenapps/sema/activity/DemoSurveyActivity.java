package com.orygenapps.sema.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;

import com.cengalabs.flatui.FlatUI;
import com.cengalabs.flatui.views.FlatButton;
import com.orygenapps.sema.R;
import com.orygenapps.sema.data.Program;
import com.orygenapps.sema.data.Question;
import com.orygenapps.sema.data.QuestionSet;
import com.orygenapps.sema.data.Survey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by starehe on 13/04/15.
 */
public class DemoSurveyActivity extends ActionBarActivity implements WidgetFragment.OnFragmentInteractionListener, TextWidgetFragment.OnValidationChangedListener {

    private FlatButton mSubmitAnswerButton;
    private ArrayList<Map<String, Object>> mOrderedQuestionDict;
    private int mCurQuestionIndex;
    private int mNumQuestions;
    private WidgetFragment mWidgetFragment;
    float fadedAlpha = 0.4f;

    private int programId;
    private int surveyId;

    // TODO: confirm what values these should be
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
        surveyId = bundle.getInt("surveyId");

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            Program program = realm.where(Program.class).equalTo("dbProgramId", programId).findFirst();
            setTitle(program.getDisplayName());

            Survey survey = realm.where(Survey.class).equalTo("dbSurveyId", surveyId).findFirst();
            RealmList<QuestionSet> questionSets = survey.getQuestionSets();

            mOrderedQuestionDict = new ArrayList<Map<String, Object>>();

            for (QuestionSet questionSet : questionSets) {
                RealmList<Question> questions = questionSet.getQuestions();

                for (Question question : questions) {
                    Map<String, Object> dictionary = new HashMap<String, Object>();
                    dictionary.put("dbSurveyId", surveyId);
                    dictionary.put("dbQuestionSetId", questionSet.getDbQuestionSetId());
                    dictionary.put("dbQuestionId", question.getDbQuestionId());
                    dictionary.put("questionType", question.getQuestionType());
                    mOrderedQuestionDict.add(dictionary);
                }
            }

            mNumQuestions = mOrderedQuestionDict.size();

            mCurQuestionIndex = 0;

            mSubmitAnswerButton = (FlatButton) findViewById(R.id.survey_submit_answer_button);

            mSubmitAnswerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submitAnswer();
                }
            });

            setupQuestion();

            String programName = program.getDisplayName();

            // Demo notification
            Notification n = new NotificationCompat.Builder(this)
                    .setContentTitle(programName)
                    .setContentText("Demo")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setAutoCancel(true)
                    .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification_extended))
                    .build();

            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(1, n); // TODO: alternatively could use the same app wide if want only one entry in notifications
        }

        finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public void setupQuestion() {

        mSubmitAnswerButton.setEnabled(false);

        Map<String, Object> curQuestionDict = mOrderedQuestionDict.get(mCurQuestionIndex);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("dbAnswerSetUUID", (String)curQuestionDict.get("dbAnswerSetUUID"));
        bundle.putInt("dbSurveyId", (int) curQuestionDict.get("dbSurveyId"));
        bundle.putInt("dbQuestionSetId", (int) curQuestionDict.get("dbQuestionSetId"));
        bundle.putInt("dbQuestionId", (int) curQuestionDict.get("dbQuestionId"));
        bundle.putInt("questionType", (int) curQuestionDict.get("questionType"));

        int questionType = (int) curQuestionDict.get("questionType");
        switch (questionType) {
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
            mSubmitAnswerButton.setText("Done");
            mSubmitAnswerButton.getAttributes().setTheme(FlatUI.CANDY, getResources());
        }
    }

    public void submitAnswer() {

        dismissKeyboard(mWidgetFragment.getView()); // TODO: only need to do this if text question

        if (isLastQuestion()) {

            // Start activity
            Intent i = new Intent(this, SubmitDemoSurveyActivity.class);
            startActivity(i);
        }
        else {
            mCurQuestionIndex++;
            setupQuestion();
        }
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
}
