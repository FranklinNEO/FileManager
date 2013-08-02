package com.archermind.filemanager.util;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.Calendar;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Files;

import com.archermind.filemanager.FileManagerApp;
import com.archermind.filemanager.R;

public class FileExplorer {
	private static final String TAG = "FileExplorer";
	private static final String TOP = "/mnt/sdcard";
	private static Stack<String> mDirStack = new Stack<String>();
	private static String mFileArray[] = null;

	public static final int UNKNOWN = -1;
	public static final int DIRECTORY = 0;
	public static final int IMAGE = 1;
	public static final int AUDIO = 2;
	public static final int VIDEO = 3;
	public static final int APK = 4;
	public static final int OTHER = 10;
	public static boolean empty=false;

	public static boolean checkDir(String fileName) {
		File f = new File(fileName);

		return f.isDirectory();
	}

	public static String[] getFileArray() {
		return mFileArray;
	}

	public static String[] enterDirectory(String dir) {
		mFileArray = new String[0];
		List<String> fileList = new ArrayList<String>();

		if (TextUtils.isEmpty(dir))
			dir = TOP;

		File f = new File(dir);

		if (f.isFile()) {
			Log.e(TAG, dir + " is a file");
			return mFileArray;
		}

		if (!mDirStack.contains(dir))
			mDirStack.add(dir);

		File[] files = f.listFiles();

		if (null == files || 0 == files.length) {
			Log.e(TAG, dir + " is empty");
			return mFileArray;
		}

		for (int i = 0; i < files.length; i++) {
			if (filterFile(dir, files[i].getName()))
				continue;

			fileList.add(files[i].getPath());
		}

		if (0 == fileList.size())
			return mFileArray;

		Collator cmp = Collator.getInstance(java.util.Locale.CHINA);

		mFileArray = new String[fileList.size()];
		fileList.toArray(mFileArray);
		Arrays.sort(mFileArray, cmp);

		return mFileArray;
	}

	private static boolean filterFile(String dir, String fileName) {
		String[] ignoreDirs = FileManagerApp.getInstance().getResources()
				.getStringArray(R.array.ignore_dirs);

		if (dir.equals(TOP)) {
			for (int j = 0; j < ignoreDirs.length; j++) {
				if (ignoreDirs[j].equals(fileName)) {
					LogUtils.d(new Exception(), "ignoreDirs: " + fileName);
					return true;
				}
			}
		}

		if (0 == fileName.indexOf("."))
			return true;

		return false;
	}

	public static String[] exitDirectory(String dir) {
		mDirStack.pop();

		if (mDirStack.empty()) {
			Log.e(TAG, "We have in top directory, so we cannot exit");
			return null;
		}

		return enterDirectory(mDirStack.peek());
	}

	public static String getDirectory() {
		if (mDirStack.empty()) {
			Log.e(TAG, "Stack is empty, so we get direcotry");
			return null;
		}

		return mDirStack.peek();
	}

	public static boolean isTop() {
		return getDirectory().equals(TOP);
	}

	public static String getFileName(String strFilePath) {
		int nPos = strFilePath.lastIndexOf("/");

		if (nPos < 0)
			return null;

		String strFileName = strFilePath.substring(nPos + 1);
		return strFileName;
	}
	
	public static int getFileIcon(String fileName) {
		File f = new File(fileName);
		
		if (f.isDirectory())
			return DIRECTORY;

		String suffix = getSuffixName(fileName);

		if (TextUtils.isEmpty(suffix))
			return UNKNOWN;

		String typeFromExt = MimeTypeMap.getSingleton()
				.getMimeTypeFromExtension(suffix);

		if (TextUtils.isEmpty(typeFromExt))
			return UNKNOWN;

		if (typeFromExt.contains("image/"))
			return IMAGE;

		if (typeFromExt.contains("audio/"))
			return AUDIO;

		if (typeFromExt.contains("video/"))
			return VIDEO;
		
		if (suffix.equals("apk"))
			return APK;
		
		return OTHER;
	}

	private static String getSuffixName(String fileName) {
		int lastDotIndex = fileName.lastIndexOf(".");

		if (lastDotIndex < 0)
			return null;

		String suffix = fileName.substring(lastDotIndex + 1);

		Log.d(TAG, "suffixName " + suffix);
		return suffix;
	}

	public static String format(long callTime) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(callTime);
		String year = toStr(calendar.get(Calendar.YEAR));
		String month = toStr(calendar.get(Calendar.MONTH) + 1);
		String day = toStr(calendar.get(Calendar.DAY_OF_MONTH));
		String hour = toStr(calendar.get(Calendar.HOUR_OF_DAY));
		String minute = toStr(calendar.get(Calendar.MINUTE));
		String second = toStr(calendar.get(Calendar.SECOND));
		String CallTime = year + "-" + month + "-" + day + " " + hour + ":"
				+ minute + ":" + second;

		return CallTime;
	}

	private static String toStr(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

	public static String getFileSize(long size) {
		if (size < 1000) {
			return Long.toString(size) + "byte";
		}

		float displaySize = size / 1000;
		if (displaySize < 1000) {
			return Float.toString(displaySize) + "K";
		}

		displaySize = displaySize / 1000;
		if (displaySize < 1000) {
			return Float.toString(displaySize) + "M";
		}

		displaySize = displaySize / 1000;
		if (displaySize < 1000) {
			return Float.toString(displaySize) + "G";
		}

		return null;
	}

	@SuppressLint("NewApi")
	public static String getThumbnail(int type, String name) {
		if (type != FileExplorer.IMAGE && type != FileExplorer.VIDEO)
			return null;

		final String[] projection = new String[] { Files.FileColumns._ID, // 0
				Files.FileColumns.DATA, // 1
		};

		final int FILES_ID_COLUMN_INDEX = 0;
		final int FILES_PATH_COLUMN_INDEX = 1;

		String where = Files.FileColumns.DATA + "=?";
		String[] selectionArgs = new String[] { name };

		Uri uri = null;
		if (type == FileExplorer.IMAGE)
			uri = Images.Media.EXTERNAL_CONTENT_URI;
		else
			uri = Video.Media.EXTERNAL_CONTENT_URI;

		ContentResolver resolver = FileManagerApp.getInstance()
				.getContentResolver();
		Cursor c = resolver.query(uri, projection, where, selectionArgs, null);

		if (null == c)
			return null;

		if (1 != c.getCount()) {
			c.close();
			return null;
		}

		long rowId = -1;
		String path = null;
		try {
			c.moveToNext();
			rowId = c.getLong(FILES_ID_COLUMN_INDEX);
			// path = c.getString(FILES_PATH_COLUMN_INDEX);
			// LogUtils.d(new Exception(), "rowId: " + rowId + ", path: " +
			// path);
		} finally {
			c.close();
		}

		/*
		 * Get thumbnail path by image_id or video_id.
		 */
		final String[] thumbnail_projection = new String[] {
				type == FileExplorer.IMAGE ? Images.Thumbnails._ID
						: Video.Thumbnails._ID,
				type == FileExplorer.IMAGE ? Images.Thumbnails.DATA
						: Video.Thumbnails.DATA,
				type == FileExplorer.IMAGE ? Images.Thumbnails.IMAGE_ID
						: Video.Thumbnails.VIDEO_ID };

		String thumbnail_where = null;
		if (type == FileExplorer.IMAGE) {
			uri = Images.Thumbnails.EXTERNAL_CONTENT_URI;
			thumbnail_where = Images.Thumbnails.IMAGE_ID + "=" + rowId;
		} else {
			uri = Video.Thumbnails.EXTERNAL_CONTENT_URI;
			thumbnail_where = Video.Thumbnails.VIDEO_ID + "=" + rowId;
		}
		c = resolver.query(uri, thumbnail_projection, thumbnail_where, null,
				null);

		if (null == c)
			return null;

		if (1 != c.getCount()) {
			c.close();
			return null;
		}

		try {
			c.moveToNext();
			path = c.getString(FILES_PATH_COLUMN_INDEX);
			// LogUtils.d(new Exception(), "rowId: " + rowId + ", path: " +
			// path);
		} finally {
			c.close();
		}

		return path;
	}

}
