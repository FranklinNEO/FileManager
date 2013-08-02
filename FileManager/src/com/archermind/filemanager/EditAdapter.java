package com.archermind.filemanager;

import java.io.File;

import com.archermind.filemanager.util.FileExplorer;
import com.archermind.filemanager.util.LogUtils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class EditAdapter extends BaseAdapter {
	private LayoutInflater mInflater = null;
	public String[] mFileArray = null;
	public boolean[] itemStatus;

	// Constructor
	public EditAdapter(Context context, String[] fileArray) {
		mFileArray = fileArray;
		mInflater = LayoutInflater.from(context);
		itemStatus = new boolean[mFileArray.length];
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

	public void toggle(int position) {
		if (itemStatus[position] == true) {
			itemStatus[position] = false;
		} else {
			itemStatus[position] = true;
		}
		this.notifyDataSetChanged();// date changed and we should refresh the
									// view
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public int[] getSelectedItemIndexes() {

		if (itemStatus == null || itemStatus.length == 0) {
			return new int[0];
		} else {
			int size = itemStatus.length;
			int counter = 0;
			// TODO how can we skip this iteration?
			for (int i = 0; i < size; i++) {
				if (itemStatus[i] == true)
					++counter;
			}
			int[] selectedIndexes = new int[counter];
			int index = 0;
			for (int i = 0; i < size; i++) {
				if (itemStatus[i] == true)
					selectedIndexes[index++] = i;
			}
			return selectedIndexes;
		}
	};

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder holder = null;

		if (convertView == null) {
			view = mInflater.inflate(R.layout.item_edit, parent, false);
			holder = new ViewHolder();
			holder.mIcon = (ImageView) view.findViewById(R.id.icon);
			holder.mFileName = (TextView) view.findViewById(R.id.file_name);
			holder.mCreateTime = (TextView) view.findViewById(R.id.create_time);
			holder.mFileSize = (TextView) view.findViewById(R.id.file_size);
			holder.checkBox = (CheckBox) view.findViewById(R.id.checkBoxEdit);
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
		Log.d("position", position + "");
		Log.d("position state", itemStatus[position] + "");
		holder.checkBox
				.setOnCheckedChangeListener(new MyCheckBoxChangedListener(
						position));
		holder.checkBox.setChecked(itemStatus[position]);
		if (file.isFile())
			holder.mFileSize.setText(FileExplorer.getFileSize(file.length()));
		else
			holder.mFileSize.setText("");

		return view;
	}

	class MyCheckBoxChangedListener implements OnCheckedChangeListener {
		int position;

		MyCheckBoxChangedListener(int position) {
			this.position = position;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked)
				itemStatus[position] = true;
			else
				itemStatus[position] = false;
		}
	}

	private class ViewHolder {
		ImageView mIcon;
		TextView mFileName;
		TextView mCreateTime;
		TextView mFileSize;
		CheckBox checkBox;
	}

}