package com.orygenapps.sema.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.orygenapps.sema.R;

import java.util.ArrayList;

/**
 * Created by ashemah on 16/02/2015.
 */
public class AnswerSetAdapter extends ArrayAdapter<AnswerSet> {

    private Context mContext;
    private ArrayList<AnswerSet> mItems;

    public AnswerSetAdapter(Context context, ArrayList<AnswerSet> items) {
        super(context, R.layout.list_item_survey, items);
        this.mContext = context;
        this.mItems = items;
    }

    public int getCount() {
        return mItems.size();
    }

    public AnswerSet getItem(int position) {
        return mItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_survey, null, true);

        // get the answer set and survey
        AnswerSet item = mItems.get(position);
        Survey survey = item.getSurvey();

        // get the views
        rowView.setTag(position);
        TextView rowText = (TextView) rowView.findViewById(R.id.survey_text);
        TextView subText = (TextView) rowView.findViewById(R.id.extra_text);

        if (survey != null) {

            // display program name
            Program program = survey.getProgram();
            rowText.setText(program.getDisplayName());

            if (item.getAnswerTriggerMode() == AnswerSetTriggerMode.SCHEDULE) {

                int expiryMins = TimeConverter.minutesUntilTimestamp(item.getExpiryTimestamp());
                String type;
                if (expiryMins == 1) {
                    type = "minute";
                } else {
                    type = "minutes";
                }

                subText.setText("#" + item.getIteration() + " - this survey expires in " + expiryMins + " " + type);

            }
            else if (item.getAnswerTriggerMode() == AnswerSetTriggerMode.ADHOC) {

                subText.setText("This survey can be launched anytime");

            }

        }

        return rowView;
    }
}
