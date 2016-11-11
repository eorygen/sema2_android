package com.orygenapps.sema.data;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by ashemah on 15/02/2015.
 */
public class QuestionChoice extends RealmObject {
    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public int getDbChoiceId() {
        return dbChoiceId;
    }

    public void setDbChoiceId(int dbChoiceId) {
        this.dbChoiceId = dbChoiceId;
    }

    public int getChoiceValue() {
        return choiceValue;
    }

    public void setChoiceValue(int choiceValue) {
        this.choiceValue = choiceValue;
    }

    public String getChoiceText() {
        return choiceText;
    }

    public void setChoiceText(String choiceText) {
        this.choiceText = choiceText;
    }

    private Question question;

    private int dbChoiceId;

    private int choiceValue;

    @Required
    private String choiceText;
}
