package com.orygenapps.sema.data;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cengalabs.flatui.views.FlatRadioButton;
import com.cengalabs.flatui.views.FlatTextView;
import com.commonsware.cwac.anddown.AndDown;
import com.orygenapps.sema.R;

import java.util.ArrayList;

/**
 * Created by starehe on 19/02/15.
 */

public class QuestionChoiceRadioAdapter extends ArrayAdapter<QuestionChoice> {

    private Context mContext;
    private ArrayList<QuestionChoice> mItems;
    private Integer mSelectedItemIndex;

    public QuestionChoiceRadioAdapter(Context context, ArrayList<QuestionChoice> items) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.mContext = context;
        this.mItems = items;
        mSelectedItemIndex = null; // TODO: confirm this
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
        View rowView = inflater.inflate(R.layout.list_item_radio, null, true);

        QuestionChoice item = mItems.get(position);

        rowView.setTag(position);

        FlatRadioButton rowRadio = (FlatRadioButton)rowView.findViewById(R.id.radio_widget_listitem_radio);

        if (mSelectedItemIndex != null && mSelectedItemIndex == position) {
            rowRadio.setChecked(true);
        }

        TextView rowText = (TextView)rowView.findViewById(R.id.checkbox_widget_listitem_text);
        Spanned spannedText = MarkdownConverter.markdownToSpanned(item.getChoiceText());
        rowText.setText(MarkdownConverter.trimSpanned(spannedText));
        return rowView;
    }

    public void setSelectedIndex(Integer index) {
        mSelectedItemIndex = index;
    }

    public Integer getSelectedIndex() {
        return mSelectedItemIndex;
    }

    public Integer getSelectedItemIndex() {
        return mSelectedItemIndex;
    }
}