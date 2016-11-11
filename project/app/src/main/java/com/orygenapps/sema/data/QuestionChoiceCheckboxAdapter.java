package com.orygenapps.sema.data;

import android.content.Context;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cengalabs.flatui.views.FlatCheckBox;
import com.cengalabs.flatui.views.FlatTextView;
import com.orygenapps.sema.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by starehe on 18/02/15.
 */

public class QuestionChoiceCheckboxAdapter extends ArrayAdapter<QuestionChoice> {

    private Context mContext;
    private ArrayList<QuestionChoice> mItems;
    private Set<Integer> mCheckedItemIndexes;

    public QuestionChoiceCheckboxAdapter(Context context, ArrayList<QuestionChoice> items) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.mContext = context;
        this.mItems = items;
        mCheckedItemIndexes = new HashSet<Integer>();
    }

    public int getCount() {
        return mItems.size();
    }

    public QuestionChoice getItem(int position) {
        return mItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_checkbox, null, true);

        QuestionChoice item = mItems.get(position);

        rowView.setTag(position);

        FlatCheckBox rowCheckbox = (FlatCheckBox)rowView.findViewById(R.id.checkbox_widget_listitem_checkbox);
        boolean isChecked = mCheckedItemIndexes.contains(position);
        rowCheckbox.setChecked(isChecked);

        TextView rowText = (TextView)rowView.findViewById(R.id.checkbox_widget_listitem_text);
        Spanned spannedText = MarkdownConverter.markdownToSpanned(item.getChoiceText());
        rowText.setText(MarkdownConverter.trimSpanned(spannedText));

        return rowView;
    }

    public void addToChecked(int index) {
        mCheckedItemIndexes.add(index);
    }

    public void removeFromChecked(int index) {
        mCheckedItemIndexes.remove(index);
    }

    public int numChecked() {
        return mCheckedItemIndexes.size();
    }

    public Set<Integer> getCheckedItemIndexes() {
        return mCheckedItemIndexes;
    }
}