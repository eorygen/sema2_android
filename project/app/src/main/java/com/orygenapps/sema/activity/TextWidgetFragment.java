package com.orygenapps.sema.activity;

import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import com.cengalabs.flatui.views.FlatEditText;
import com.cengalabs.flatui.views.FlatTextView;
import com.orygenapps.sema.R;
import com.orygenapps.sema.data.Answer;
import com.orygenapps.sema.data.AnswerSet;
import com.orygenapps.sema.data.MarkdownConverter;

import io.realm.Realm;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TextWidgetFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TextWidgetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TextWidgetFragment extends WidgetFragment {

    private TextView mQuestionTextView;
    private EditText mAnswerEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_text_widget, container, false);

        mQuestionTextView = (TextView) fragmentView.findViewById(R.id.text_widget_question);
        Spanned spannedText = MarkdownConverter.markdownToSpanned(mQuestion.getQuestionText());
        mQuestionTextView.setText(MarkdownConverter.trimSpanned(spannedText));

        mAnswerEditText = (EditText) fragmentView.findViewById(R.id.text_widget_answer);
        mAnswerEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkValidation();
            }
        });

        return fragmentView;
    }

    public void checkValidation() {
        boolean prev = mIsValid;

        if (mAnswerEditText.getText().length() > 0) {
            mIsValid = true;
        }
        else {
            mIsValid = false;
        }

        if (mIsValid != prev) {
            mValidationChangedListener.onValidationChanged(mIsValid);
        }
    }

    @Override
    public void saveAnswerToDb() {
        super.saveAnswerToDb();

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            AnswerSet answerSet = realm.where(AnswerSet.class).equalTo("uuid", mDbAnswerSetUUID).findFirst();
            realm.beginTransaction();

            Answer answer = realm.createObject(Answer.class);
            answer.setSet(answerSet);
            answer.setDbSurveyId(mDbSurveyId);
            answer.setDbQuestionSetId(mDbQuestionSetId);
            answer.setDbQuestionId(mDbQuestionId);
            answer.setAnswerValue(getValue());
            answer.setDisplayedTimestamp(mDisplayedTimestamp);
            answer.setAnsweredTimestamp(mAnsweredTimestamp);
            answer.setReactionTimeMs(mAnsweredTimestamp-mDisplayedTimestamp);

            realm.commitTransaction();
        }
        finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public String getValue() {
        String value = mAnswerEditText.getText().toString();
        return value;
    }
}