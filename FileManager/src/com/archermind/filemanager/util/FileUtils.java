package com.archermind.filemanager.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.archermind.filemanager.FileManagerApp;

public class FileUtils extends Thread {
	private final static String LOG_TAG = "DeleteThread";
	private WakeLock mWakeLock = null;
	private int mAction = 0;
	private String mSrcPath = null;
	private String mDstPath = null;

	private Handler mHandler = null;
	private int mBeginID = -1;
	private int mEndID = -1;

	public final static int ACTION_DELETE = 0;
	public final static int ACTION_COPY = 1;
	public final static int ACTION_MOVE = 2;

	public FileUtils(int action, String srcPath, String dstPath,
			Handler handler, int beginID, int endID) {
		PowerManager pm = (PowerManager) FileManagerApp.getInstance()
				.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, LOG_TAG);
		mAction = action;
		mSrcPath = srcPath;
		mDstPath = dstPath;

		mHandler = handler;
		mBeginID = beginID;
		mEndID = endID;
	}

	@Override
	public void run() {

		mWakeLock.acquire();

		boolean result = false;
		switch (mAction) {
		case ACTION_DELETE:
			result = deleteDir(new File(mSrcPath));
			break;

		case ACTION_COPY:
			copy(mSrcPath, mDstPath);
			break;

		case ACTION_MOVE:
			move(new File(mSrcPath), new File(mDstPath));
			break;
		}

		mWakeLock.release();
		mHandler.sendMessage(mHandler.obtainMessage(mEndID, result));

	}

	private boolean copy(String mSrcPath2, String mDstPath2) {
		File[] currentFiles;
		File sourceFile = new File(mSrcPath2);
		File root = new File(mSrcPath2);
		if (!root.exists()) {
			return false;
		}

		if (sourceFile.isDirectory()) {
			mSrcPath2 = mSrcPath2 + "/";
			mDstPath2 = mDstPath2 + "/" + root.getName() + "/";
			currentFiles = root.listFiles();
			File targetDir = new File(mDstPath2);

			if (!targetDir.exists()) {
				targetDir.mkdirs();
			}

			for (int i = 0; i < currentFiles.length; i++) {

				if (currentFiles[i].isDirectory()) {
					copy(currentFiles[i].getPath() + "/", mDstPath2
							+ currentFiles[i].getName() + "/");

				} else {
					LogUtils.d(new Exception(), "Copy: " + sourceFile.getPath());
					mHandler.sendMessage(mHandler.obtainMessage(mBeginID,
							sourceFile.getPath()));
					CopySdcardFile(currentFiles[i].getPath(), mDstPath2
							+ currentFiles[i].getName());
				}
			}
		} else {
			mDstPath2 = mDstPath2 + "/";
			CopyFile(mSrcPath2, mDstPath2);
		}

		return true;

	}

	private boolean CopyFile(String mSrcPath2, String mDstPath2) {
		try {
			File sourceFile = new File(mSrcPath2);
			String DirectPath = mDstPath2 + sourceFile.getName();
			Log.d("filename", sourceFile.getName());
			Log.d("DstPath", mDstPath2);
			File dir = new File(mDstPath2);

			if (!dir.exists()) {
				return false;
			}
			dir.mkdir();

			if (!(new File(DirectPath).exists())) {
				InputStream is = new FileInputStream(sourceFile);
				FileOutputStream fout = new FileOutputStream(DirectPath);
				byte[] buffer = new byte[1024];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fout.write(buffer, 0, count);
				}
				fout.close();
				is.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean CopySdcardFile(String path, String string) {
		try {
			InputStream fosfrom = new FileInputStream(path);
			OutputStream fosto = new FileOutputStream(string);
			byte bt[] = new byte[1024];
			int c;
			while ((c = fosfrom.read(bt)) > 0) {
				fosto.write(bt, 0, c);
			}
			fosfrom.close();
			fosto.close();
			return true;

		} catch (Exception ex) {
			return false;
		}
	}

	private boolean deleteDir(File srcFile) {
		if (srcFile.isDirectory()) {
			String[] children = srcFile.list();
			for (int i = 0; i < children.length; i++) {
				if (!deleteDir(new File(srcFile, children[i]))) {
					return false;
				}
			}
		}

		// When srcFile is directory or file, it will be deleted.
		LogUtils.d(new Exception(), "Delete: " + srcFile.getPath());
		mHandler.sendMessage(mHandler.obtainMessage(mBeginID, srcFile.getPath()));
		return srcFile.delete();
	}

	private boolean move(File sourceFile, File targetFile) {
		File tarpath = new File(targetFile, sourceFile.getName());
		if (sourceFile.isDirectory()) {
			tarpath.mkdir();
			File[] item = sourceFile.listFiles();
			for (int i = 0; i < item.length; i++) {
				if (!move(item[i], tarpath))
					return false;
			}
		} else {
			LogUtils.d(new Exception(), "Move: " + sourceFile.getPath());
			if (!sourceFile.renameTo(tarpath)) {
				if (!copyFile(sourceFile, tarpath)) {
					return false;
				} else {
					if (!sourceFile.delete()) {
						tarpath.delete();
						return false;
					}
				}
			}
		}

		return true;
	}

	public static boolean sync(FileOutputStream stream) {
		try {
			if (stream != null) {
				stream.getFD().sync();
			}
			return true;
		} catch (IOException e) {
		}
		return false;
	}

	/**
	 * Copy a file from srcFile to destFile, return true if succeed, return
	 * false if fail
	 */
	public static boolean copyFile(File srcFile, File destFile) {
		boolean result = false;
		try {
			InputStream in = new FileInputStream(srcFile);
			try {
				result = copyToFile(in, destFile);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			result = false;
		}
		return result;
	}

	/**
	 * Copy data from a source stream to destFile. Return true if succeed,
	 * return false if failed.
	 */
	public static boolean copyToFile(InputStream inputStream, File destFile) {
		try {
			if (destFile.exists()) {

				Log.d("destFileName", destFile.getName());
				destFile.delete();
			}

			FileOutputStream out = new FileOutputStream(destFile);
			try {
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) >= 0) {
					out.write(buffer, 0, bytesRead);
				}
			} finally {
				out.flush();
				try {
					out.getFD().sync();
				} catch (IOException e) {
				}
				out.close();
			}

			return true;
		} catch (IOException e) {
			return false;
		}
	}

}
