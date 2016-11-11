package com.orygenapps.sema.activity;

import android.os.Bundle;
import android.app.Fragment;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.orygenapps.sema.R;
import com.orygenapps.sema.data.Answer;
import com.orygenapps.sema.data.AnswerSet;
import com.orygenapps.sema.data.MarkdownConverter;

import io.realm.Realm;
import io.techery.progresshint.ProgressHintDelegate;
import io.techery.progresshint.addition.widget.SeekBar;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TextWidgetFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TextWidgetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SliderWidgetFragment extends WidgetFragment {

    private TextView mQuestionTextView;
    private TextView mQuestionMinValue;
    private TextView mQuestionMaxValue;
    private TextView mQuestionMinLabel;
    private TextView mQuestionMaxLabel;
    private SeekBar mAnswerSeekbar;
    private int mMinValue;
    private int mMaxValue;
    private String mMinLabel;
    private String mMaxLabel;
    private float fadedAlpha = 0.4f;
    private boolean hasMoved = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_slider_widget, container, false);

        mQuestionTextView = (TextView) fragmentView.findViewById(R.id.slider_widget_question);
        Spanned spannedText = MarkdownConverter.markdownToSpanned(mQuestion.getQuestionText());
        mQuestionTextView.setText(MarkdownConverter.trimSpanned(spannedText));

        mQuestionMaxValue = (TextView) fragmentView.findViewById(R.id.slider_widget_min);
        mQuestionMaxValue.setText(String.valueOf(mQuestion.getMinimumValue()));

        mQuestionMinValue = (TextView) fragmentView.findViewById(R.id.slider_widget_max);
        mQuestionMinValue.setText(String.valueOf(mQuestion.getMaximumValue()));

        mQuestionMaxLabel = (TextView) fragmentView.findViewById(R.id.slider_widget_min_label);
        spannedText = MarkdownConverter.markdownToSpanned(String.valueOf(mQuestion.getMinimumLabel()));
        mQuestionMaxLabel.setText(MarkdownConverter.trimSpanned(spannedText));

        mQuestionMinLabel = (TextView) fragmentView.findViewById(R.id.slider_widget_max_label);
        spannedText = MarkdownConverter.markdownToSpanned(String.valueOf(mQuestion.getMaximumLabel()));
        mQuestionMinLabel.setText(MarkdownConverter.trimSpanned(spannedText));

        mAnswerSeekbar = (SeekBar) fragmentView.findViewById(R.id.slider_widget_seekbar);
        mAnswerSeekbar.setAlpha(fadedAlpha);

        mMinValue = mQuestion.getMinimumValue();
        mMaxValue = mQuestion.getMaximumValue();
        mMinLabel = mQuestion.getMinimumLabel();
        mMaxLabel= mQuestion.getMaximumLabel();
        mAnswerSeekbar.setMax(mMaxValue - mMinValue);
        mAnswerSeekbar.setProgress(mAnswerSeekbar.getMax() / 2);

        mAnswerSeekbar.getHintDelegate().setHintAdapter(new ProgressHintDelegate.SeekBarHintAdapter() {
            @Override
            public String getHint(android.widget.SeekBar seekBar, int progress) {
                return " " + getValue() + " ";
            }
        });

        mAnswerSeekbar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {

                if (!hasMoved) {
                    hasMoved = true;
                    mAnswerSeekbar.setAlpha(1f);
                    mMinValue = mQuestion.getMinimumValue();
                    mMaxValue = mQuestion.getMaximumValue();
                    mMinLabel = mQuestion.getMinimumLabel();
                    mMaxLabel = mQuestion.getMaximumLabel();
                    mAnswerSeekbar.setMax(mMaxValue - mMinValue);
                    mAnswerSeekbar.getHintDelegate().setPopupAlwaysShown(true);
                }
            }

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
                checkValidation();
            }
        });

        return fragmentView;
    }

    public void checkValidation() {
        boolean prev = mIsValid;

        mIsValid = true; // TODO: implement better system than this

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
        String value = String.valueOf(mMinValue + mAnswerSeekbar.getProgress());
        return value;
    }
}