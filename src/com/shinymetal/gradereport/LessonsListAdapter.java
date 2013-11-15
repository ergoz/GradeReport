package com.shinymetal.gradereport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.shinymetal.gradereport.objects.Lesson;
import com.shinymetal.gradereport.utils.GshisLoader;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LessonsListAdapter extends
		BaseAdapter implements UpdateableAdapter {

	private final DiaryActivity mActivity;
	private static final SimpleDateFormat mFormat = new SimpleDateFormat(
			"HH:mm ", Locale.ENGLISH);

	private final Date mDay;
	private Cursor mCursor;

	public LessonsListAdapter(DiaryActivity activity, Date day) {

		mActivity = activity;
		mDay = day;
		
		mCursor = GshisLoader.getInstance().getCursorLessonsByDate(mDay);
	}

	public void onUpdateTaskComplete() {
		
		if (mCursor != null && !mCursor.isClosed())
			mCursor.close();

		mCursor = GshisLoader.getInstance().getCursorLessonsByDate(mDay);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		
		if (mCursor == null || mCursor.isClosed())
			return 0;
		
		return mCursor.getCount();
	}

	@Override
	public Object getItem(int position) {
		
		if (mCursor == null || mCursor.isClosed())
			return null;
		
		mCursor.moveToPosition(position);
		
		if (!mCursor.isAfterLast())		
			return Lesson.getFromCursor(mCursor);
		
		return null;		
	}

	@Override
	public long getItemId(int position) {

		return position;
	}
	
	@Override
	public boolean hasStableIds() {
		
		return false;
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

		Lesson l = (Lesson) getItem(position);

		if (l != null) {
			itemNameView.setText(Html.fromHtml("" + l.getNumber() + ". " + l.getFormText()));
			itemDetailView.setText(Html.fromHtml(mFormat.format(l.getStart()) + l.getTeacher()));
		}

		return convertView;
	}
}
