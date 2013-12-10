package com.shinymetal.gradereport;

import java.util.ArrayList;
import java.util.HashMap;

import com.shinymetal.gradereport.objects.MarkRec;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class NotificationUtils {

	private static final int MARKS_MAX_COUNT = 3;

	private static NotificationUtils instance;

	private Context context;
	private NotificationManager manager; 
	private int mLastId = 0; 
							
	private HashMap<Integer, Notification> notifications; 

	private NotificationUtils(Context context) {
		this.context = context;
		manager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notifications = new HashMap<Integer, Notification>();
	}

	public static NotificationUtils getInstance(Context context) {
		if (instance == null) {
			instance = new NotificationUtils(context);
		} else {
			instance.context = context;
		}
		return instance;
	}
	
//	public int createGradeNotification(ArrayList<MarkRec> marks) {
//		
//		String message = context.getString(R.string.label_new_marks);
//		int count = 0;
//		
//		for (MarkRec rec : marks) {
//			
//			if (count <= MARKS_MAX_COUNT)
//				message += (count > 0 ? "; " : " ") + rec.getComment() + ": " + rec.getMarks();
//			else {
//				message += "; ...";
//				break;
//			}
//			
//			count++;
//		}
//
//		// TODO: we shall determine which activity to use
//		Intent notificationIntent = new Intent(context, DiaryActivity.class);
//
//		NotificationCompat.Builder nb = new NotificationCompat.Builder(context)
//				// NotificationCompat.Builder nb = new
//				// NotificationBuilder(context) //для версии Android > 3.0
//				.setSmallIcon(R.drawable.ic_launcher)
//				.setAutoCancel(true)
//				.setTicker(message)
//				.setContentText(message)
//				.setContentIntent(
//						PendingIntent.getActivity(context, 0,
//								notificationIntent,
//								PendingIntent.FLAG_CANCEL_CURRENT))
//				.setWhen(System.currentTimeMillis())
//				.setContentTitle(context.getString(R.string.app_name))
//				.setDefaults(Notification.DEFAULT_ALL);
//
//		Notification notification = nb.getNotification();
//		manager.notify(mLastId, notification);
//		notifications.put(mLastId, notification);
//
//		return mLastId++;
//	}
}