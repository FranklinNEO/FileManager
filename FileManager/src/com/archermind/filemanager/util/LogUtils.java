package com.archermind.filemanager.util;

public class LogUtils {
	public static final boolean mIsDebug = true;
	private static final String TAG = "FileManager";

	private LogUtils() {

	}

	public static void v(Exception e, String msg) {
		if (mIsDebug)
			android.util.Log.v(TAG, getInfo(e) + msg);
	}

	public static void v(Exception e, String msg, Throwable tr) {
		if (mIsDebug)
			android.util.Log.v(TAG, getInfo(e) + msg, tr);
	}

	public static void d(Exception e, String msg) {
		if (mIsDebug)
			android.util.Log.d(TAG, getInfo(e) + msg);
	}

	public static void d(Exception e, String msg, Throwable tr) {
		if (mIsDebug)
			android.util.Log.d(TAG, getInfo(e) + msg, tr);
	}

	public static void i(Exception e, String msg) {
		if (mIsDebug)
			android.util.Log.i(TAG, getInfo(e) + msg);
	}

	public static void i(Exception e, String msg, Throwable tr) {
		if (mIsDebug)
			android.util.Log.i(TAG, getInfo(e) + msg, tr);
	}

	public static void w(Exception e, String msg) {
		if (mIsDebug)
			android.util.Log.w(TAG, getInfo(e) + msg);
	}

	public static void w(Exception e, Throwable tr) {
		if (mIsDebug)
			android.util.Log.w(TAG, getInfo(e), tr);		
	}
	
	public static void w(Exception e, String msg, Throwable tr) {
		if (mIsDebug)
			android.util.Log.w(TAG, getInfo(e) + msg, tr);
	}


	public static void e(Exception e, String msg) {
		if (mIsDebug)
			android.util.Log.e(TAG, getInfo(e) + msg);
	}

	public static void e(Exception e, String msg, Throwable tr) {
		if (mIsDebug)
			android.util.Log.e(TAG, getInfo(e) + msg);
	}
	
	private static String getInfo(Exception e)
	{
		String fileName  = e.getStackTrace()[0].getFileName();
		int lineNum      = e.getStackTrace()[0].getLineNumber();
		/*		
		String className = e.getStackTrace()[0].getClassName();
		String funName   = e.getStackTrace()[0].getMethodName();
		
		final Calendar calendar = Calendar.getInstance();
		
		final int hour = calendar.get(Calendar.HOUR_OF_DAY);
		final int minutes = calendar.get(Calendar.MINUTE);		
		final int seconds = calendar.get(Calendar.SECOND);
		final int mseconds = calendar.get(Calendar.MILLISECOND);
		
		String time = String.format("%2s:%2s:%2s %3s", hour, minutes, seconds, mseconds);
		*/
		
		//return "[" + fileName + " " + lineNum + "](" + className + " " + funName + ") ";
		return "[" + fileName + " " + lineNum + "]";	
	}
}
