package com.orygenapps.sema;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.orygenapps.sema.activity.SEMAApplication;
import com.orygenapps.sema.activity.SurveyActivity;
import com.orygenapps.sema.data.AnswerSet;
import com.orygenapps.sema.data.Program;
import com.orygenapps.sema.data.Survey;
import com.orygenapps.sema.data.SurveyDataManager;

import io.realm.Realm;

/**
 * Created by ashemah on 16/02/2015.
 */

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    private SurveyDataManager mDataManager;
    private SurveyAlarmManager mAlarmManager;

    @Override
    public void onReceive(Context context, Intent intent) {

        mDataManager = ((SEMAApplication)context.getApplicationContext()).getDataManager();
        mAlarmManager = ((SEMAApplication)context.getApplicationContext()).getSurveyAlarmManager();

        String action = intent.getAction();

        if (action.equals("android.intent.action.BOOT_COMPLETED")) { // also android.intent.action.QUICKBOOT_POWERON ?

            Log.d("alarms", "***** AlarmBroadcastReceiver - Boot detected! Setting up alarms...");
            mAlarmManager.setupAlarms(context);

        }
        else if (action.equals("com.sema.survey.start")) {

            int programId = intent.getIntExtra("programId", -1);
            int surveyId = intent.getIntExtra("surveyId", -1);
            String answerSetUUID = intent.getStringExtra("answerSetUUID");

            Realm realm = null;

            try {
                realm = Realm.getDefaultInstance();

                Program program = realm.where(Program.class).equalTo("dbProgramId", programId).findFirst();
                Survey survey = realm.where(Survey.class).equalTo("dbSurveyId", surveyId).findFirst();
                AnswerSet answerSet = realm.where(AnswerSet.class).equalTo("uuid", answerSetUUID).findFirst();
                Log.d("alarms", "** AlarmBroadcastReceiver - Received start alarm with AnswerSet UUID " + answerSetUUID + " (Program ID " + answerSet.getDbProgramId() + ")");

                // Post a notification
                postNotification(context, answerSet, "A new survey is available");
                Log.d("alarms", "** AlarmBroadcastReceiver - Posted notification");

                // Pass the message to the dashboard
                Intent bcast = new Intent();
                bcast.setAction("com.sema.survey.start.bcast");
                Log.d("alarms", "** AlarmBroadcastReceiver - Broadcasting start to dashboard...");
                context.sendBroadcast(bcast);
                Log.d("alarms", "** AlarmBroadcastReceiver - Broadcast done.");

            }

            finally {
                if (realm != null) {
                    realm.close();
                }
            }
        }
        else if (action.equals("com.sema.survey.firstreminder")) {

            String answerSetUUID = intent.getStringExtra("answerSetUUID");

            Realm realm = null;

            try {
                realm = Realm.getDefaultInstance();
                AnswerSet answerSet = realm.where(AnswerSet.class).equalTo("uuid", answerSetUUID).findFirst();
                Log.d("alarms", "** AlarmBroadcastReceiver - Received first reminder with AnswerSet UUID " + answerSetUUID + " (Program ID " + answerSet.getDbProgramId() + ")");

                // Post a reminder notification (sound only)
                postNotification(context, answerSet, "You have an outstanding survey to complete");

            }

            finally {
                if (realm != null) {
                    realm.close();
                }
            }
        }
        else if (action.equals("com.sema.survey.secondreminder")) {

            String answerSetUUID = intent.getStringExtra("answerSetUUID");

            Realm realm = null;

            try {
                realm = Realm.getDefaultInstance();
                AnswerSet answerSet = realm.where(AnswerSet.class).equalTo("uuid", answerSetUUID).findFirst();
                Log.d("alarms", "** AlarmBroadcastReceiver - Received second reminder with AnswerSet UUID " + answerSetUUID + " (Program ID " + answerSet.getDbProgramId() + ")");

                // Post a reminder notification (sound only)
                postNotification(context, answerSet, "You have an outstanding survey to complete");

            }

            finally {
                if (realm != null) {
                    realm.close();
                }
            }
        }
        else if (action.equals("com.sema.survey.expiry")) {

            String answerSetUUID = intent.getStringExtra("answerSetUUID");

            Realm realm = null;

            try {
                realm = Realm.getDefaultInstance();
                AnswerSet answerSet = realm.where(AnswerSet.class).equalTo("uuid", answerSetUUID).findFirst();
                Log.d("alarms", "** AlarmBroadcastReceiver - Received expiry alarm with AnswerSet UUID " + answerSetUUID + " (Program ID " + answerSet.getDbProgramId() + ")");

                // reset streak
                SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("current_streak", 0);
                editor.commit();

                // remove the notification for this survey
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(answerSet.getStartAlarmRequestCode());

                // Pass the message to the dashboard
                Intent bcast = new Intent();
                bcast.setAction("com.sema.survey.expiry.bcast");
                Log.d("alarms", "** AlarmBroadcastReceiver - Broadcasting expiry to dashboard...");
                context.sendBroadcast(bcast);
                Log.d("alarms", "** AlarmBroadcastReceiver - Broadcast done.");

            }

            finally {
                if (realm != null) {
                    realm.close();
                }
            }
        }
    }

    private void postReminderNotification(Context context, AnswerSet answerSet) {

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification n = new NotificationCompat.Builder(context)
                .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification_extended))
                .build();

        notificationManager.notify(answerSet.getStartAlarmRequestCode(), n);

        }

    private void postNotification(Context context, AnswerSet answerSet, String message) {

        int surveyId = answerSet.getDbSurveyId();

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, SurveyActivity.class);
        intent.putExtra("programId", answerSet.getDbProgramId());
        intent.putExtra("answerSetUUID", answerSet.getUuid());
        intent.setAction("com.sema.notification." + surveyId);

        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Survey survey = answerSet.getSurvey();

        Program program = survey != null ? survey.getProgram() : null;

        if (survey != null && program != null) {

            String programName = program.getDisplayName();

            Notification n = new NotificationCompat.Builder(context)
                    .setContentTitle(programName)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification_extended))
                    .build();

            notificationManager.notify(answerSet.getStartAlarmRequestCode(), n); // TODO: alternatively could use the same app wide if want only one entry in notifications
        }
    }
}