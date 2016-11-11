package com.orygenapps.sema;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.orygenapps.sema.data.AnswerSet;
import com.orygenapps.sema.data.AnswerSetTriggerMode;
import com.orygenapps.sema.data.Program;
import com.orygenapps.sema.data.Survey;
import com.orygenapps.sema.data.TimeConverter;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by starehe on 31/03/15.
 */
public class SurveyAlarmManager {

    public void setupAlarms(Context context) {

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            long currentTimestamp = TimeConverter.currentTimeToMsTimestamp();

            // get all answersets with an expiry time greater than the current time
            List<AnswerSet> answerSets = realm.where(AnswerSet.class)
                    .equalTo("answerTriggerMode", AnswerSetTriggerMode.SCHEDULE)
                    .equalTo("completedTimestamp", -1) // added to ensure we only deal with answer sets that have not yet been completed
                    .greaterThan("expiryTimestamp", currentTimestamp)
                    .findAllSorted("deliveryTimestamp", true);

            // remove all existing alarms
            for (int i = 0; i < answerSets.size(); i++) {
                AnswerSet aset = answerSets.get(i);
                removeAlarmsFromAnswerSet(context, aset);

                // schedule alarms (up to max 64)
                if (i < 64) { // Note: this will result in about 180 alarms
                    scheduleAlarmsForAnswerSet(context, aset);
                }
            }
        }

        finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public void removeAlarmsFromAnswerSet(Context context, AnswerSet answerSet) {

//        Log.d("alarms", "** SurveyAlarmManager - Removing alarms from AnswerSet UUID " + answerSet.getUuid() + " (Program ID " + answerSet.getDbProgramId() +  ")");

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        if (answerSet.getAnswerTriggerMode() == AnswerSetTriggerMode.SCHEDULE) {

            // Cancel start alarm
            int startAlarmRequestCode = answerSet.getStartAlarmRequestCode();
//            Log.d("sema", "CANCEL CODE" + startAlarmRequestCode);
            PendingIntent startAlarmIntent = buildPendingIntent(context, answerSet, "com.sema.survey.start", startAlarmRequestCode);
            alarmManager.cancel(startAlarmIntent);

            // Cancel first reminder alarm
            int firstReminderRequestCode = answerSet.getFirstReminderRequestCode();
//            Log.d("sema", "CANCEL CODE" + firstReminderRequestCode);
            PendingIntent firstReminderIntent = buildPendingIntent(context, answerSet, "com.sema.survey.firstreminder", firstReminderRequestCode);
            alarmManager.cancel(firstReminderIntent);

            // Cancel second reminder alarm
            int secondReminderRequestCode = answerSet.getSecondReminderRequestCode();
//            Log.d("sema", "CANCEL CODE" + secondReminderRequestCode);
            PendingIntent secondReminderIntent = buildPendingIntent(context, answerSet, "com.sema.survey.secondreminder", secondReminderRequestCode);
            alarmManager.cancel(secondReminderIntent);

        }

        // Cancel expiry alarm
        int expiryAlarmRequestCode = answerSet.getExpiryAlarmRequestCode();
//        Log.d("sema", "CANCEL CODE" + expiryAlarmRequestCode);
        PendingIntent expiryAlarmIntent = buildPendingIntent(context, answerSet, "com.sema.survey.expiry", expiryAlarmRequestCode);
        alarmManager.cancel(expiryAlarmIntent);
    }

    public void scheduleAlarmsForAnswerSet(Context context, AnswerSet answerSet) {

        Log.d("alarms", "** SurveyAlarmManager - Scheduling alarms from AnswerSet UUID " + answerSet.getUuid() + " (Program ID " + answerSet.getDbProgramId() +  ")");

        long currentTimestamp = TimeConverter.currentTimeToMsTimestamp();

        // Schedule the thing
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        if (answerSet.getAnswerTriggerMode() == AnswerSetTriggerMode.SCHEDULE) {

            // Setup start alarm
            int startAlarmRequestCode = answerSet.getStartAlarmRequestCode();
            long startAlarmTimestamp = answerSet.getDeliveryTimestamp();
//            Log.d("sema", "REQUEST CODE" + startAlarmRequestCode);

            if (startAlarmTimestamp > currentTimestamp) {
                PendingIntent startAlarmIntent = buildPendingIntent(context, answerSet, "com.sema.survey.start", startAlarmRequestCode);
                alarmManager.set(AlarmManager.RTC_WAKEUP, startAlarmTimestamp, startAlarmIntent);
                //alarmManager.setExact(AlarmManager.RTC_WAKEUP, startAlarmTimestamp, startAlarmIntent);
            }

            // Setup first reminder alarm
            int firstReminderRequestCode = answerSet.getFirstReminderRequestCode();
            long firstReminderTimestamp = answerSet.getFirstReminderTimestamp();
//            Log.d("sema", "REQUEST CODE" + firstReminderRequestCode);

            if (firstReminderTimestamp > currentTimestamp) {
                PendingIntent firstReminderIntent = buildPendingIntent(context, answerSet, "com.sema.survey.firstreminder", firstReminderRequestCode);
                alarmManager.set(AlarmManager.RTC_WAKEUP, firstReminderTimestamp, firstReminderIntent);
                //alarmManager.setExact(AlarmManager.RTC_WAKEUP, firstReminderTimestamp, firstReminderIntent);
            }

            // Setup second reminder alarm
            int secondReminderRequestCode = answerSet.getSecondReminderRequestCode();
            long secondReminderTimestamp = answerSet.getSecondReminderTimestamp();
//            Log.d("sema", "REQUEST CODE" + secondReminderRequestCode);

            if (secondReminderTimestamp > currentTimestamp) {
                PendingIntent secondReminderIntent = buildPendingIntent(context, answerSet, "com.sema.survey.secondreminder", secondReminderRequestCode);
                alarmManager.set(AlarmManager.RTC_WAKEUP, secondReminderTimestamp, secondReminderIntent);
                //alarmManager.setExact(AlarmManager.RTC_WAKEUP, secondReminderTimestamp, secondReminderIntent);
            }

        }

        // Setup expiry alarm
        int expiryAlarmRequestCode = answerSet.getExpiryAlarmRequestCode();
        long expiryAlarmTimestamp = answerSet.getExpiryTimestamp();
//        Log.d("sema", "REQUEST CODE" + expiryAlarmRequestCode);

        if (expiryAlarmTimestamp > currentTimestamp) {
            PendingIntent expiryAlarmIntent = buildPendingIntent(context, answerSet, "com.sema.survey.expiry", expiryAlarmRequestCode);
            alarmManager.set(AlarmManager.RTC_WAKEUP, expiryAlarmTimestamp, expiryAlarmIntent);
        }
    }

    private PendingIntent buildPendingIntent(Context context, AnswerSet answerSet, String action, int requestCode) {

        int surveyId = answerSet.getDbSurveyId();
        Intent alarmIntent = new Intent(context, AlarmBroadcastReceiver.class);
        alarmIntent.putExtra("programId", answerSet.getDbProgramId());
        alarmIntent.putExtra("surveyId", surveyId);
        alarmIntent.putExtra("answerSetUUID", answerSet.getUuid());
        alarmIntent.setAction(action);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, alarmIntent, 0);

        return pendingIntent;
    }

    private void cancelPendingIntent(Context context, AnswerSet answerSet, String action, int requestCode) {

        int surveyId = answerSet.getDbSurveyId();
        Intent alarmIntent = new Intent(context, AlarmBroadcastReceiver.class);
        alarmIntent.putExtra("programId", answerSet.getDbProgramId());
        alarmIntent.putExtra("surveyId", surveyId);
        alarmIntent.putExtra("answerSetUUID", answerSet.getUuid());
        alarmIntent.setAction(action);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, alarmIntent, 0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public void cancelAlarmsForAnswerSet(Context context, AnswerSet answerSet) {
        cancelPendingIntent(context, answerSet, "com.sema.survey.start", answerSet.getStartAlarmRequestCode());
        cancelPendingIntent(context, answerSet, "com.sema.survey.firstreminder", answerSet.getFirstReminderRequestCode());
        cancelPendingIntent(context, answerSet, "com.sema.survey.secondreminder", answerSet.getSecondReminderRequestCode());
        cancelPendingIntent(context, answerSet, "com.sema.survey.expiry", answerSet.getExpiryAlarmRequestCode());
    }
}
