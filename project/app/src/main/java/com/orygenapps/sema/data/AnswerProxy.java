package com.orygenapps.sema.data;

/**
 * Created by starehe on 24/03/15.
 */
public class AnswerProxy {

    private int question;
    private String answer_value;
    private String displayed_timestamp;
    private String answered_timestamp;
    private long reaction_time_ms;

    public AnswerProxy(Answer answer) {
        this.question = answer.getDbQuestionId();
        this.answer_value = answer.getAnswerValue();
        this.displayed_timestamp = TimeConverter.convertTimestampToDateString(answer.getDisplayedTimestamp());
        this.answered_timestamp = TimeConverter.convertTimestampToDateString(answer.getAnsweredTimestamp());
        this.reaction_time_ms = answer.getReactionTimeMs();
    }
}
