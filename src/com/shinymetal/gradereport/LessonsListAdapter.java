package com.shinymetal.gradereport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.shinymetal.objects.Lesson;
import com.shinymetal.utils.GshisLoader;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LessonsListAdapter extends
		BaseAdapter implements UpdateableAdapter {

	private final DiaryActivity mActivity;
	private final SimpleDateFormat mFormat;
	private final Date mDay;

	private ArrayList<Lesson> mValues;

	public LessonsListAdapter(DiaryActivity activity, Date day) {

		mActivity = activity;
		mDay = day;

		mValues = GshisLoader.getInstance().getLessonsByDate(mDay, false);

		if (mValues == null) {

			mValues = new ArrayList<Lesson>();
			mActivity.startUpdateTask();
		}

		mFormat = new SimpleDateFormat("HH:mm ", Locale.ENGLISH);
	}

	public void onUpdateTaskComplete() {

		mValues = GshisLoader.getInstance().getLessonsByDate(mDay, false);

		if (mValues == null) {

			mValues = new ArrayList<Lesson>();
		}

		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		
		return mValues.size();
	}

	@Override
	public Object getItem(int position) {
		
		return mValues.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.lessons_list, null);
		}

		TextView itemNameView = (TextView) convertView
				.findViewById(R.id.itemName);
		TextView itemDetailView = (TextView) convertView
				.findViewById(R.id.itemDetail);

		Lesson l = mValues.get(position);

		itemNameView.setText(Html.fromHtml("" + l.getNumber() + ". " + l.getFormText()));
		itemDetailView.setText(Html.fromHtml(mFormat.format(l.getStart()) + l.getTeacher()));

		return convertView;
	}
}
