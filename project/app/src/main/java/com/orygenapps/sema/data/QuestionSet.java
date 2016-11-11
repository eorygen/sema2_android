package com.orygenapps.sema.data;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by ashemah on 15/02/2015.
 */
public class QuestionSet extends RealmObject {
    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    public int getDbQuestionSetId() {
        return dbQuestionSetId;
    }

    public void setDbQuestionSetId(int dbQuestionSetId) {
        this.dbQuestionSetId = dbQuestionSetId;
    }

    public boolean getRandomiseQuestionDisplayOrder() {
        return randomiseQuestionDisplayOrder;
    }

    public void setRandomiseQuestionDisplayOrder(boolean randomiseQuestionDisplayOrder) {
        this.randomiseQuestionDisplayOrder = randomiseQuestionDisplayOrder;
    }

    public RealmList<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(RealmList<Question> questions) {
        this.questions = questions;
    }

    private Survey survey;

    private int dbQuestionSetId;
    private boolean randomiseQuestionDisplayOrder;

    private RealmList<Question> questions;
}
