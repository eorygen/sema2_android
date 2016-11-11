package com.orygenapps.sema.data;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by ashemah on 15/02/2015.
 */

public class Answer extends RealmObject {

    public AnswerSet getSet() {
        return set;
    }

    public void setSet(AnswerSet set) {
        this.set = set;
    }

    public int getDbSurveyId() {
        return dbSurveyId;
    }

    public void setDbSurveyId(int dbSurveyId) {
        this.dbSurveyId = dbSurveyId;
    }

    public int getDbQuestionSetId() {
        return dbQuestionSetId;
    }

    public void setDbQuestionSetId(int dbQuestionSetId) {
        this.dbQuestionSetId = dbQuestionSetId;
    }

    public int getDbQuestionId() {
        return dbQuestionId;
    }

    public void setDbQuestionId(int dbQuestionId) {
        this.dbQuestionId = dbQuestionId;
    }

    public String getAnswerValue() {
        return answerValue;
    }

    public void setAnswerValue(String answerValue) {
        this.answerValue = answerValue;
    }

    public long getDisplayedTimestamp() {
        return displayedTimestamp;
    }

    public void setDisplayedTimestamp(long displayedTimestamp) {
        this.displayedTimestamp = displayedTimestamp;
    }

    public long getAnsweredTimestamp() {
        return answeredTimestamp;
    }

    public void setAnsweredTimestamp(long answeredTimestamp) {
        this.answeredTimestamp = answeredTimestamp;
    }

    public long getReactionTimeMs() {
        return reactionTimeMs;
    }

    public void setReactionTimeMs(long reactionTimeMs) {
        this.reactionTimeMs = reactionTimeMs;
    }

    private AnswerSet set;
    private int dbSurveyId;
    private int dbQuestionSetId;
    private int dbQuestionId;

    @Required
    private String answerValue;

    private long displayedTimestamp;
    private long answeredTimestamp;

    private long reactionTimeMs;
}
