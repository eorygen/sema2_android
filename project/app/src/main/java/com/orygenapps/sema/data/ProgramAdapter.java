package com.orygenapps.sema.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

//import com.cengalabs.flatui.views.FlatTextView;
import com.orygenapps.sema.R;

import java.util.ArrayList;

import io.realm.RealmList;

/**
 * Created by ashemah on 16/02/2015.
 */
public class ProgramAdapter extends ArrayAdapter<Program> {

    private Context mContext;
    private ArrayList<Program> mItems;

    public ProgramAdapter(Context context, ArrayList<Program> items) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.mContext = context;
        this.mItems = items;
    }

    public int getCount() {
        return mItems.size();
    }

    public Program getItem(int position) {
        return mItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_program, null, true);

        Program item = mItems.get(position);

        rowView.setTag(position);
        TextView rowText = (TextView)rowView.findViewById(R.id.program_text);

        rowText.setText(item.getDisplayName());

        TextView subText = (TextView)rowView.findViewById(R.id.extra_text);

        long curTimestamp = TimeConverter.currentTimeToMsTimestamp();
        boolean programIsActive = SurveyDataManager.programIsActive(item, curTimestamp);

        RealmList<Survey> surveys = item.getSurveys();
        if (surveys.size() > 0) {

        boolean hasIterations = false;
        for (Survey survey : surveys) {
            int currentIteration = survey.getCurrentIteration();
            int maxIterations = survey.getMaxIterations();
            if (maxIterations == -1 || currentIteration < maxIterations) {
                hasIterations = true;
            }
        }
        if (hasIterations) {
            if (!programIsActive) {
                subText.setText("Is in quiet time.");
            } else {
                subText.setText("Active.");
            }
            } else {
                subText.setText("Completed.");
            }
        }
        else {
            subText.setText("Inactive - No surveys are assigned.");
        }

        return rowView;
    }
}