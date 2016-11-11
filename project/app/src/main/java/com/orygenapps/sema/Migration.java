/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orygenapps.sema;

import android.util.Log;

import com.orygenapps.sema.data.Answer;
import com.orygenapps.sema.data.AnswerSet;
import com.orygenapps.sema.data.Survey;
import com.orygenapps.sema.data.User;

import io.realm.Realm;
import io.realm.RealmMigration;
import io.realm.internal.ColumnType;
import io.realm.internal.Table;

/***************************** NOTE: *********************************************
 * The API for migration is currently using internal lower level classes that will
 * be replaced by a new API very soon! Until then you will have to explore and use
 * below example as inspiration.
 *********************************************************************************
 */


public class Migration implements RealmMigration {

    @Override
    public long execute(Realm realm, long version) {

//        Log.i("migration", ">> start Answer");
//
//        Table t = realm.getTable(Answer.class);
//        for (int i = 0; i < t.getColumnCount(); i++) {
//            Log.i("migration", t.getColumnName(i));
//        }
//
//        Log.i("migration", ">> end");
//
//        Log.i("migration", ">> start Survey");
//
//        Table t1 = realm.getTable(Survey.class);
//        for (int i = 0; i < t1.getColumnCount(); i++) {
//            Log.i("migration", t1.getColumnName(i));
//        }
//
//        Log.i("migration", ">> end");

        if (version == 0) {
//            Table locationTable = realm.getTable(User.class);
//            locationTable.addColumn(ColumnType.STRING, "pushToken");
//
//            Table answerSetTable = realm.getTable(AnswerSet.class);
//            answerSetTable.addColumn(ColumnType.INTEGER, "firstReminderRequestCode");
//            answerSetTable.addColumn(ColumnType.INTEGER, "secondReminderRequestCode");
//            answerSetTable.addColumn(ColumnType.INTEGER, "firstReminderTimestamp");
//            answerSetTable.addColumn(ColumnType.INTEGER, "secondReminderTimestamp");

            version++;
        }

        if (version == 1) {
//            Table surveyTable = realm.getTable(Survey.class);
//            surveyTable.addColumn(ColumnType.BOOLEAN, "scheduleAllowMonday");
//            surveyTable.addColumn(ColumnType.BOOLEAN, "scheduleAllowTuesday");
//            surveyTable.addColumn(ColumnType.BOOLEAN, "scheduleAllowWednesday");
//            surveyTable.addColumn(ColumnType.BOOLEAN, "scheduleAllowThursday");
//            surveyTable.addColumn(ColumnType.BOOLEAN, "scheduleAllowFriday");
//            surveyTable.addColumn(ColumnType.BOOLEAN, "scheduleAllowSaturday");
//            surveyTable.addColumn(ColumnType.BOOLEAN, "scheduleAllowSunday");

            version++;
        }

        if (version == 2) {
//            Table answerTable = realm.getTable(Answer.class);
//            answerTable.addColumn(ColumnType.INTEGER, "reactionTimeMs");

            version++;
        }

        if (version == 3) {
            version++;
        }

        return version;
    }
}