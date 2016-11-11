package com.orygenapps.sema.activity;

import android.os.Bundle;
import android.app.Fragment;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.cengalabs.flatui.views.FlatRadioButton;
import com.cengalabs.flatui.views.FlatTextView;
import com.commonsware.cwac.merge.MergeAdapter;
import com.orygenapps.sema.R;
import com.orygenapps.sema.data.Answer;
import com.orygenapps.sema.data.AnswerSet;
import com.orygenapps.sema.data.MarkdownConverter;
import com.orygenapps.sema.data.QuestionChoice;
import com.orygenapps.sema.data.QuestionChoiceRadioAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TextWidgetFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TextWidgetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RadioWidgetFragment extends WidgetFragment {

    private TextView mQuestionTextView;
    private TextView mQuestionPromptView;
    private ListView mQuestionChoiceListView;
    private MergeAdapter mMergeAdapter;
    private QuestionChoiceRadioAdapter mQuestionChoiceAdapter;
    private RealmList<QuestionChoice> mRealmQuestionChoices;
    private ArrayList<QuestionChoice> mQuestionChoices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_radio_widget, container, false);

        mMergeAdapter = new MergeAdapter();

        // Inflate a view header
        View questionView = getActivity().getLayoutInflater().inflate(R.layout.list_question_view, null);

        mQuestionTextView = (TextView) questionView.findViewById(R.id.question_text);
        Spanned spannedText = MarkdownConverter.markdownToSpanned(mQuestion.getQuestionText());
        mQuestionTextView.setText(MarkdownConverter.trimSpanned(spannedText));

        mQuestionPromptView = (TextView) questionView.findViewById(R.id.question_prompt);
        mQuestionPromptView.setText("Select a single item:");

        mMergeAdapter.addView(questionView);

        mRealmQuestionChoices = mQuestion.getChoices(); // TODO: consider swapping later so that the adapter uses RealmList directly
        mQuestionChoices = new ArrayList<QuestionChoice>();
        for (QuestionChoice questionChoice : mRealmQuestionChoices) {
            mQuestionChoices.add(questionChoice);
        }

        // Shuffle choices if randomisation enabled
        if (mQuestion.isRandomiseChoiceDisplayOrder()) {
            long seed = System.nanoTime();
            Collections.shuffle(mQuestionChoices, new Random(seed));
        }

        mQuestionChoiceAdapter = new QuestionChoiceRadioAdapter(getActivity(), mQuestionChoices);

        mMergeAdapter.addAdapter(mQuestionChoiceAdapter);

        mQuestionChoiceListView = (ListView) fragmentView.findViewById(R.id.radio_widget_listview);
        mQuestionChoiceListView.setAdapter(mMergeAdapter);
        mQuestionChoiceListView.setDivider(null);

        mQuestionChoiceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int mergedPos = position - 1;
                FlatRadioButton rowRadio = (FlatRadioButton) view.findViewById(R.id.radio_widget_listitem_radio);
                mQuestionChoiceAdapter.setSelectedIndex(mergedPos);

                mQuestionChoiceListView.invalidateViews();

                checkValidation();
            }
        });

        return fragmentView;
    }

    public void checkValidation() {
        boolean prev = mIsValid;

        mIsValid = (mQuestionChoiceAdapter.getSelectedIndex() != null);

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

            QuestionChoice questionChoice = (QuestionChoice) mQuestionChoices.get(mQuestionChoiceAdapter.getSelectedIndex());
            String answerValue = String.valueOf(questionChoice.getDbChoiceId());

            Answer answer = realm.createObject(Answer.class);
            answer.setSet(answerSet);
            answer.setDbSurveyId(mDbSurveyId);
            answer.setDbQuestionSetId(mDbQuestionSetId);
            answer.setDbQuestionId(mDbQuestionId);
            answer.setDisplayedTimestamp(mDisplayedTimestamp);
            answer.setAnsweredTimestamp(mAnsweredTimestamp);
            answer.setAnswerValue(answerValue);
            answer.setReactionTimeMs(mAnsweredTimestamp-mDisplayedTimestamp);

            realm.commitTransaction();

        }

        finally {
            if (realm != null) {
                realm.close();
            }
        }
    }
}
