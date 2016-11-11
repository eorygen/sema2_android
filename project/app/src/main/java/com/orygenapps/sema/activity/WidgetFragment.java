package com.orygenapps.sema.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.commonsware.cwac.anddown.AndDown;
import com.orygenapps.sema.R;
import com.orygenapps.sema.data.Question;
import com.orygenapps.sema.data.TimeConverter;

import io.realm.Realm;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WidgetFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WidgetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public abstract class WidgetFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    protected OnFragmentInteractionListener mListener;
    protected OnValidationChangedListener mValidationChangedListener;
    protected boolean mIsValid;
    protected String mDbAnswerSetUUID;
    protected int mDbSurveyId;
    protected int mDbQuestionSetId;
    protected int mDbQuestionId;
    protected Question mQuestion;
    protected long mDisplayedTimestamp;
    protected long mAnsweredTimestamp;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WidgetFragment.
     */
//    // TODO: Rename and change types and number of parameters
//    public static WidgetFragment newInstance(String param1, String param2) {
//        WidgetFragment fragment = new WidgetFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    public WidgetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mDbAnswerSetUUID = getArguments().getString("dbAnswerSetUUID");
            mDbSurveyId = getArguments().getInt("dbSurveyId");
            mDbQuestionSetId = getArguments().getInt("dbQuestionSetId");
            mDbQuestionId = getArguments().getInt("dbQuestionId");

            Realm realm = null;
            try {
                realm = Realm.getDefaultInstance();
                mQuestion = realm.where(Question.class).equalTo("dbQuestionId", mDbQuestionId).findFirst();
                long currentTimestamp = TimeConverter.currentTimeToMsTimestamp();
                mDisplayedTimestamp = currentTimestamp;
            }
            finally {
                if (realm != null) {
                    realm.close();
                }
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setText(R.string.hello_blank_fragment);
        return textView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
            mValidationChangedListener = (OnValidationChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mValidationChangedListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public interface OnValidationChangedListener {
        public void onValidationChanged(boolean isValid);
    }

    public void saveAnswerToDb() {

        long currentTimestamp = TimeConverter.currentTimeToMsTimestamp();
        mAnsweredTimestamp = currentTimestamp;

    }
}
