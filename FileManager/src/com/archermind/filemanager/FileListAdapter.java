package com.archermind.filemanager;

import java.io.File;
import android.content.Context;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.archermind.filemanager.util.FileExplorer;
import com.archermind.filemanager.util.LogUtils;

public class FileListAdapter extends BaseAdapter {

	private LayoutInflater mInflater = null;;
	public String[] mFileArray = null;

	// Constructor
	public FileListAdapter(Context context, String[] fileArray) {
		mFileArray = fileArray;
		mInflater = LayoutInflater.from(context);
	}

	public void setFileArray(String[] fileArray) {
		mFileArray = fileArray;
	}

	@Override
	public int getCount() {
		return mFileArray.length;
	}

	@Override
	public Object getItem(int position) {
		return mFileArray[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder holder = null;

		if (convertView == null) {
			view = mInflater.inflate(R.layout.file_list_item, parent, false);
			holder = new ViewHolder();
			holder.mIcon = (ImageView) view.findViewById(R.id.icon);
			holder.mFileName = (TextView) view.findViewById(R.id.file_name);
			holder.mCreateTime = (TextView) view.findViewById(R.id.create_time);
			holder.mFileSize = (TextView) view.findViewById(R.id.file_size);

			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}

		String filename = mFileArray[position];
		LogUtils.d(new Exception(), "file name is: " + filename);

		int iconId = -1;
		String thumbnail = null;
		switch (FileExplorer.getFileIcon(filename)) {
		case FileExplorer.DIRECTORY:
			thumbnail = null;
			iconId = R.drawable.common_dir;
			break;

		case FileExplorer.IMAGE:
			thumbnail = FileExplorer.getThumbnail(FileExplorer.IMAGE, filename);
			iconId = R.drawable.mypicture;
			break;

		case FileExplorer.AUDIO:
			thumbnail = null;
			iconId = R.drawable.music_choose;
			break;

		case FileExplorer.VIDEO:
			thumbnail = FileExplorer.getThumbnail(FileExplorer.VIDEO, filename);
			iconId = R.drawable.video_choose;
			break;

		case FileExplorer.APK:
			thumbnail = null;
			iconId = R.drawable.ic_launcher;
			break;

		case FileExplorer.OTHER:
		default:
			thumbnail = null;
			iconId = R.drawable.other_file;
			break;
		}

		if (null != thumbnail)
			holder.mIcon.setImageURI(Uri.fromFile(new File(thumbnail)));
		else
			holder.mIcon.setImageResource(iconId);

		holder.mFileName.setText(FileExplorer.getFileName(filename));

		File file = new File(filename);

		holder.mCreateTime.setText(FileExplorer.format(file.lastModified()));

		if (file.isFile())
			holder.mFileSize.setText(FileExplorer.getFileSize(file.length()));
		else
			holder.mFileSize.setText("");

		return view;
	}

	private class ViewHolder {
		ImageView mIcon;
		TextView mFileName;
		TextView mCreateTime;
		TextView mFileSize;
	}

}
