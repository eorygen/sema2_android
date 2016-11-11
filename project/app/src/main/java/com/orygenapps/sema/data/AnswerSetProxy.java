package com.orygenapps.sema.data;

import android.content.Context;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by starehe on 23/03/15.
 */

public class AnswerSetProxy {

    private int survey;
    private String uuid;
    private int program_version;
    private int iteration;
    private String created_timestamp;
    private String delivery_timestamp;
    private String expiry_timestamp;
    private String completed_timestamp;
    private String timezone;

    ArrayList<AnswerProxy> answers;

    public AnswerSetProxy(AnswerSet answerSet) {
        this.survey = answerSet.getDbSurveyId();
        this.uuid = answerSet.getUuid();
        this.program_version = answerSet.getDbProgramVersionId();
        this.iteration = answerSet.getIteration();
        this.created_timestamp = TimeConverter.convertTimestampToDateString(answerSet.getCreatedTimestamp());
        this.delivery_timestamp = TimeConverter.convertTimestampToDateString(answerSet.getDeliveryTimestamp());
        this.expiry_timestamp = TimeConverter.convertTimestampToDateString(answerSet.getExpiryTimestamp());
        this.completed_timestamp = TimeConverter.convertTimestampToDateString(answerSet.getCompletedTimestamp());
        this.timezone = answerSet.getTimezone();
    }

    public static AnswerSetProxy initWithAnswerSet(Context context, AnswerSet answerSet) {

        ArrayList<AnswerProxy> answerProxies = new ArrayList<AnswerProxy>();

        // Copy answer set values
        AnswerSetProxy answerSetProxy = new AnswerSetProxy(answerSet);

        // Get related answers
        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();
            RealmResults<Answer> answers = realm.where(Answer.class).equalTo("set.uuid", answerSet.getUuid()).findAll();

            // Copy answer values
            for (int i = answers.size() - 1; i >= 0; i--) { // necessary to do this in reverse due to realm modifying the actual data (and query), which affects the size of the list
                Answer answer = answers.get(i);
                AnswerProxy answerProxy = new AnswerProxy(answer);
                answerProxies.add(answerProxy);
            }

            answerSetProxy.answers = answerProxies;
        }
        finally {
            if (realm != null) {
                realm.close();
            }
        }

        return answerSetProxy;
    }
}
