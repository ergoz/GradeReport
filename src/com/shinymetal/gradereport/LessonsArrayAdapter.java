package com.shinymetal.gradereport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.shinymetal.objects.Lesson;
import com.shinymetal.utils.GshisLoader;

public class LessonsArrayAdapter extends BaseExpandableListAdapter {

	private final DiaryActivity mActivity;
	private final SimpleDateFormat mFormat;
	private final Date mDay;
	
	private ArrayList<Lesson> mValues;

	public LessonsArrayAdapter(DiaryActivity activity, Date day) {

		mActivity = activity;
		mDay = day;
		
		mValues = GshisLoader.getInstance()
				.getLessonsByDate(mDay, false);
		
		if (mValues == null) {
			
			mValues = new ArrayList<Lesson> ();			
			mActivity.startUpdateTask ();
		}

		mFormat = new SimpleDateFormat("HH:mm ", Locale.ENGLISH);
	}
	
	public void onUpdateTaskComplete () {
		
		mValues = GshisLoader.getInstance()
				.getLessonsByDate(mDay, false);
		
		if (mValues == null) {
			
			mValues = new ArrayList<Lesson> ();			
		}
		
		notifyDataSetChanged();
	}

	@Override
	public int getGroupCount() {

		return mValues.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {

		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mValues.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {

		return mValues.get(groupPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.lessons_list, null);
		}

		if (isExpanded) {
			// �������� ���-������, ���� ������� Group ��������
		} else {
			// �������� ���-������, ���� ������� Group ������
		}

		TextView itemNameView = (TextView) convertView
				.findViewById(R.id.itemName);
		TextView itemDetailView = (TextView) convertView
				.findViewById(R.id.itemDetail);

		Lesson l = mValues.get(groupPosition);

		itemNameView.setText("" + l.getNumber() + ". " + l.getFormText());
		itemDetailView.setText(mFormat.format(l.getStart()) + l.getTeacher());

		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.lessons_detail, null);
		}

		TextView textTheme = (TextView) convertView
				.findViewById(R.id.itemTheme);
		String theme = mValues.get(groupPosition).getTheme();
		if (theme == null)
			theme = "";
		textTheme.setText(mActivity.getString(R.string.label_theme) + ": " + theme);

		TextView textHomework = (TextView) convertView
				.findViewById(R.id.itemHomework);
		String homework = mValues.get(groupPosition).getHomework();
		if (homework == null)
			homework = "";
		textHomework.setText(mActivity.getString(R.string.label_homework) + ": "
				+ homework);

		TextView textMarks = (TextView) convertView
				.findViewById(R.id.itemMarks);
		String marks = mValues.get(groupPosition).getMarks();
		if (marks == null)
			marks = "";
		textMarks.setText(mActivity.getString(R.string.label_marks) + ": " + marks);

		TextView textComment = (TextView) convertView
				.findViewById(R.id.itemComment);
		String comment = mValues.get(groupPosition).getComment();
		if (comment == null)
			comment = "";
		textComment.setText(mActivity.getString(R.string.label_comment) + ": " + comment);

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}