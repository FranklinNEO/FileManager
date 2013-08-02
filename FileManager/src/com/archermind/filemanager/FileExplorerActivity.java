package com.archermind.filemanager;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.archermind.filemanager.util.FileUtils;
import com.archermind.filemanager.util.FileExplorer;
import com.archermind.filemanager.util.LogUtils;

@SuppressLint("HandlerLeak")
public class FileExplorerActivity extends Activity implements OnClickListener {
	private static final String TAG = "FileExplorerActivity";

	private static final int MENU_EXPLORER_LIST = Menu.FIRST;
	private static final int MENU_EXPLORER_ICON = Menu.FIRST + 1;
	private static final int MENU_CREATE_FOLDER = Menu.FIRST + 2;

	private static final int MENU_RENAME_FILE = Menu.FIRST + 3;
	private static final int MENU_DELETE_FILE = Menu.FIRST + 4;
	private static final int MENU_COPY_FILE = Menu.FIRST + 5;
	private static final int MENU_CUT_FILE = Menu.FIRST + 6;
	private static final int MENU_PASTE_FILE = Menu.FIRST + 7;
	private static final int MENU_EDIT_MODE = Menu.FIRST + 8;
	private static final int MENU_NORMAL_MODE = Menu.FIRST + 9;

	private static final int MSG_FILE_BEGIN = 0;
	private static final int MSG_FILE_END = 1;
	private static final int MSG_UPDATE_LIST = 2;

	public static int LIST_ITEM_ID = -1;
	public static String[] FilePath = null;
	public static View view;
	public final static LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(
			ViewGroup.LayoutParams.FILL_PARENT,
			ViewGroup.LayoutParams.FILL_PARENT);

	private FrameLayout mContentView = null;

	private LinearLayout mIconLayout = null;
	private GridView mGridView = null;
	private FileIconAdapter mFileIconAdapter = null;

	private LinearLayout mListLayout = null;
	private ListView mListView = null;
	private FileListAdapter mFileListAdapter = null;
	private EditAdapter mEditAdapter = null;

	private int mExplorerType = MENU_EXPLORER_LIST;
	private int mEditType = MENU_NORMAL_MODE;
	private int mActionState = mExplorerType;
	private String mActionFile = null;
	private boolean mRename = false;
	private String oldname = null;
	private boolean mContextMenu = false;
	private ProgressDialog mProgressDialog = null;

	public static void startExplorer(Activity activity, int reqCode) {
		if (!checkSDCard(activity))
			return;

		Intent intent = new Intent(activity, FileExplorerActivity.class);
		activity.startActivityForResult(intent, reqCode);
	}

	/*
	 * Check to see if we have an SDCard Check to see if the SDCard is busy
	 */
	private static boolean checkSDCard(Activity activity) {
		String status = Environment.getExternalStorageState();

		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			int title;
			String msg;

			if (status.equals(Environment.MEDIA_SHARED)) {
				msg = activity.getResources().getString(
						R.string.download_sdcard_busy_dlg_msg);
				title = R.string.download_sdcard_busy_dlg_title;
			} else {
				msg = activity.getResources().getString(
						R.string.download_no_sdcard_dlg_msg);
				title = R.string.download_no_sdcard_dlg_title;
			}

			Builder builder = new Builder(activity);

			builder.setTitle(title);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setMessage(msg);
			builder.setPositiveButton(R.string.ok, null);
			builder.show();

			return false;
		}

		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.file_explorer);
		mContentView = (FrameLayout) findViewById(R.id.file_explorer);

		String[] fileArray = FileExplorer.enterDirectory(null);
		initIconLayout(fileArray);
		initListLayout(fileArray);
		mIconLayout.setVisibility(View.INVISIBLE);

		setTitle(FileExplorer.getDirectory());
		registerForContextMenu(mListView);
		registerForContextMenu(mGridView);
	}

	private void initIconLayout(String[] fileArray) {
		mIconLayout = (LinearLayout) getLayoutInflater().inflate(
				R.layout.file_icon, null);
		mContentView.addView(mIconLayout, COVER_SCREEN_PARAMS);
		mGridView = (GridView) mIconLayout.findViewById(R.id.file_grid);
		mFileIconAdapter = new FileIconAdapter(this, fileArray);
		mGridView.setAdapter(mFileIconAdapter);
		mGridView.setOnItemClickListener(mIconClickListener);
	}

	private void initListLayout(String[] fileArray) {
		mListLayout = (LinearLayout) getLayoutInflater().inflate(
				R.layout.file_list, null);
		mContentView.addView(mListLayout, COVER_SCREEN_PARAMS);
		mListView = (ListView) mListLayout.findViewById(R.id.file_list);
		if (mEditType != MENU_EDIT_MODE) {
			toNormal(fileArray);
		} else {
			toEdit(fileArray);
		}
	}

	public void toEdit(String[] fileArray) {
		mListView = (ListView) mListLayout.findViewById(R.id.file_list);
		mEditAdapter = new EditAdapter(this, fileArray);
		mListView.setAdapter(mEditAdapter);
		mListView.setOnItemClickListener(mListClickListener);
		View emptyView = findViewById(R.id.empty);
		mListView.setEmptyView(emptyView);
	}

	public void toNormal(String[] fileArray) {
		mListView = (ListView) mListLayout.findViewById(R.id.file_list);
		mFileListAdapter = new FileListAdapter(this, fileArray);
		mListView.setAdapter(mFileListAdapter);
		mListView.setOnItemClickListener(mListClickListener);
		View emptyView = findViewById(R.id.empty);
		mListView.setEmptyView(emptyView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_EXPLORER_LIST, 0, R.string.explorer_list);
		menu.add(0, MENU_EXPLORER_ICON, 0, R.string.explorer_icon);
		menu.add(0, MENU_CREATE_FOLDER, 0, R.string.create_folder);
		menu.add(0, MENU_DELETE_FILE, 0, R.string.delete_file);
		menu.add(0, MENU_COPY_FILE, 0, R.string.copy_file);
		menu.add(0, MENU_CUT_FILE, 0, R.string.cut_file);
		menu.add(0, MENU_PASTE_FILE, 0, R.string.paste_file);
		menu.add(0, MENU_EDIT_MODE, 0, R.string.edit_mode);
		menu.add(0, MENU_NORMAL_MODE, 0, R.string.normal_mode);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MENU_EXPLORER_LIST).setVisible(
				MENU_EXPLORER_LIST != mExplorerType);
		menu.findItem(MENU_EXPLORER_ICON).setVisible(
				MENU_EXPLORER_ICON != mExplorerType);
		menu.findItem(MENU_EDIT_MODE).setVisible(MENU_EDIT_MODE != mEditType);
		menu.findItem(MENU_NORMAL_MODE).setVisible(
				MENU_NORMAL_MODE != mEditType);

		switch (mActionState) {
		case MENU_EXPLORER_LIST:
		case MENU_EXPLORER_ICON:
			menu.findItem(MENU_COPY_FILE).setVisible(
					mEditType == MENU_EDIT_MODE);
			menu.findItem(MENU_CUT_FILE)
					.setVisible(mEditType == MENU_EDIT_MODE);
			menu.findItem(MENU_DELETE_FILE).setVisible(
					mEditType == MENU_EDIT_MODE);
			menu.findItem(MENU_PASTE_FILE).setVisible(false);
			break;

		case MENU_COPY_FILE:
		case MENU_CUT_FILE:
			if (FilePath != null) {
				menu.findItem(MENU_PASTE_FILE).setVisible(true);
				menu.findItem(MENU_CUT_FILE).setVisible(false);
				menu.findItem(MENU_COPY_FILE).setVisible(false);
				menu.findItem(MENU_DELETE_FILE).setVisible(false);
			}
			break;

		case MENU_EDIT_MODE:
			menu.findItem(MENU_NORMAL_MODE).setVisible(false);
			menu.findItem(MENU_COPY_FILE).setVisible(true);
			menu.findItem(MENU_DELETE_FILE).setVisible(true);
			mEditType = MENU_EDIT_MODE;
			break;

		case MENU_NORMAL_MODE:
			menu.findItem(MENU_EDIT_MODE).setVisible(false);
			mEditType = MENU_NORMAL_MODE;
			break;

		default:
			break;
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_EXPLORER_LIST:
			mExplorerType = item.getItemId();
			updateFileList(FileExplorer.getFileArray(), true);
			return true;

		case MENU_EXPLORER_ICON:
			mExplorerType = item.getItemId();
			updateFileList(FileExplorer.getFileArray(), false);
			return true;

		case MENU_CREATE_FOLDER:
			mRename = false;
			createInputDialog();
			return true;

		case MENU_PASTE_FILE:
			if (MENU_COPY_FILE == mActionState)
				createCopyDialog();
			else if (MENU_CUT_FILE == mActionState)
				createCutDialog();

			mEditType = MENU_NORMAL_MODE;
			toNormal(FileExplorer.getFileArray());
			return true;

		case MENU_DELETE_FILE:
			int[] Index = mEditAdapter.getSelectedItemIndexes();
			for (int i = 0; i < Index.length; i++) {
				int ItemId = Index[i];
				mActionFile = FileExplorer.getFileArray()[ItemId];
				LogUtils.e(new Exception(), "Delete file: " + mActionFile);
				FileUtils deleteThread = new FileUtils(FileUtils.ACTION_DELETE,
						mActionFile, null, mHandler, MSG_FILE_BEGIN,
						MSG_FILE_END);
				deleteThread.start();
			}
			createProgressDialog("Delete file...");
			mEditType = MENU_NORMAL_MODE;
			toNormal(FileExplorer.getFileArray());
			return true;

		case MENU_COPY_FILE:
		case MENU_CUT_FILE:
			mActionState = item.getItemId();
			int[] id = mEditAdapter.getSelectedItemIndexes();
			FilePath = new String[id.length];
			for (int i = 0; i < id.length; i++) {
				int ItemId = id[i];
				mActionFile = FileExplorer.getFileArray()[ItemId];
				FilePath[i] = mActionFile;
			}
			toNormal(FileExplorer.getFileArray());
			// mActionFile = setActionSrcFile();
			return true;

		case MENU_EDIT_MODE:
			mEditType = MENU_EDIT_MODE;
			toEdit(FileExplorer.getFileArray());
			return true;

		case MENU_NORMAL_MODE:
			mEditType = MENU_NORMAL_MODE;
			toNormal(FileExplorer.getFileArray());
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("File Edit");
		menu.add(0, MENU_RENAME_FILE, 0, R.string.rename_file);
		menu.add(0, MENU_DELETE_FILE, 0, R.string.delete_file);
		menu.add(0, MENU_COPY_FILE, 0, R.string.copy_file);
		menu.add(0, MENU_CUT_FILE, 0, R.string.cut_file);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		mContextMenu = true;
		LIST_ITEM_ID = menuInfo.position;
		switch (item.getItemId()) {
		case MENU_RENAME_FILE:
			mRename = true;
			oldname = FileExplorer.getFileName((String) mListView
					.getItemAtPosition(LIST_ITEM_ID));
			createInputDialog();
			return true;
		case MENU_DELETE_FILE:
			mActionFile = setActionSrcFile();
			createDeleteDialog();
			return true;
		case MENU_COPY_FILE:
		case MENU_CUT_FILE:
			FilePath = new String[1];
			mActionState = item.getItemId();
			mActionFile = setActionSrcFile();
			FilePath[0] = mActionFile;
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private AdapterView.OnItemClickListener mListClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			String item = (String) parent.getAdapter().getItem(position);
			Log.d(TAG, "we select: " + item);

			File file = new File(item);
			if (file.isDirectory()) {
				updateFileList(FileExplorer.enterDirectory(item), true);
			} else if (file.isFile()) {
				resolveApp(file);
			} else {
				setResult(RESULT_OK, (new Intent()).setAction(item));
				finish();
			}
		}
	};

	private GridView.OnItemClickListener mIconClickListener = new GridView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			String item = (String) parent.getAdapter().getItem(position);
			Log.d(TAG, "we select: " + item);

			File file = new File(item);
			if (file.isDirectory()) {
				updateFileList(FileExplorer.enterDirectory(item), false);
			} else if (file.isFile()) {
				resolveApp(file);
			} else {
				setResult(RESULT_OK, (new Intent()).setAction(item));
				finish();
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (!FileExplorer.isTop()) {
				onKeyBack();
			} else {
				setResult(RESULT_CANCELED, null);
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void onKeyBack() {
		mHandler.sendMessage(mHandler
				.obtainMessage(MSG_UPDATE_LIST, (Object) FileExplorer
						.exitDirectory(FileExplorer.getDirectory())));
		setTitle(FileExplorer.getDirectory());
	}

	private void updateFileList(String[] fileArray, boolean bList) {
		if (bList) {
			mFileListAdapter.setFileArray(fileArray);
			mFileListAdapter.notifyDataSetChanged();
		} else {
			mFileIconAdapter.setFileArray(fileArray);
			mFileIconAdapter.notifyDataSetChanged();
		}

		mIconLayout.setVisibility(bList ? View.INVISIBLE : View.VISIBLE);
		mListLayout.setVisibility(bList ? View.VISIBLE : View.INVISIBLE);
		setTitle(FileExplorer.getDirectory());
	}

	private void resolveApp(File file) {
		int icon = FileExplorer.getFileIcon(file.getName());
		Intent i = new Intent(Intent.ACTION_VIEW);

		switch (icon) {
		case FileExplorer.IMAGE:
			i.setDataAndType(Uri.fromFile(file), "image/*");
			break;

		case FileExplorer.AUDIO:
			i.setDataAndType(Uri.fromFile(file), "audio/*");
			break;

		case FileExplorer.VIDEO:
			i.setDataAndType(Uri.fromFile(file), "video/*");
			break;

		case FileExplorer.APK:
			i.setDataAndType(Uri.fromFile(file),
					"application/vnd.android.package-archive");
			break;

		default:
			i.setDataAndType(Uri.fromFile(file), "*/*");
			break;
		}
		startActivity(i);
	}

	private void createInputDialog() {
		// This example shows how to add a custom layout to an AlertDialog
		LayoutInflater factory = LayoutInflater.from(this);

		final View textEntryView = factory.inflate(
				R.layout.alert_dialog_text_entry, null);
		EditText editText = (EditText) textEntryView
				.findViewById(R.id.folder_name);
		editText.setText(oldname);
		view = textEntryView;
		AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);

		// dlgBuilder.setIcon(android.R.drawable);
		dlgBuilder.setTitle(R.string.input_dialog_title);
		dlgBuilder.setView(textEntryView);
		dlgBuilder.setPositiveButton(R.string.ok, this);
		dlgBuilder.setNegativeButton(R.string.cancel, this);
		dlgBuilder.setCancelable(true).setOnCancelListener(
				new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
						oldname = null;
					}
				});

		dlgBuilder.create().show();
	}

	private String setActionSrcFile() {
		int position = LIST_ITEM_ID;

		switch (mExplorerType) {
		case MENU_EXPLORER_LIST:
			if (!mContextMenu) {
				position = mListView.getSelectedItemPosition();
			}
			break;

		case MENU_EXPLORER_ICON:
			if (!mContextMenu) {
				position = mGridView.getSelectedItemPosition();
			}
			break;
		default:
			Log.e(TAG, "Invalide explorer type: " + mExplorerType);
			return null;
		}

		if (position < 0)
			return null;

		return FileExplorer.getFileArray()[position];
	}

	private void createDeleteDialog() {
		LogUtils.e(new Exception(), "Delete file: " + mActionFile);
		FileUtils deleteThread = new FileUtils(FileUtils.ACTION_DELETE,
				mActionFile, null, mHandler, MSG_FILE_BEGIN, MSG_FILE_END);
		deleteThread.start();
		createProgressDialog("Delete file...");
	}

	private void createCopyDialog() {
		for (int i = 0; i < FilePath.length; i++) {
			mActionFile = FilePath[i];
			Log.d("SourceFile", mActionFile);
			Log.d("DirFile", FileExplorer.getDirectory());
			FileUtils copyThread = new FileUtils(FileUtils.ACTION_COPY,
					mActionFile, FileExplorer.getDirectory(), mHandler,
					MSG_FILE_BEGIN, MSG_FILE_END);
			copyThread.start();
		}
		createProgressDialog("Copy file...");
	}

	private void createCutDialog() {
		for (int i = 0; i < FilePath.length; i++) {
			mActionFile = FilePath[i];
			FileUtils copyThread = new FileUtils(FileUtils.ACTION_MOVE,
					mActionFile, FileExplorer.getDirectory(), mHandler,
					MSG_FILE_BEGIN, MSG_FILE_END);
			copyThread.start();
		}
		createProgressDialog("Move file...");
	}

	private void createProgressDialog(String title) {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setButton2(getResources().getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				});

		mProgressDialog.setCancelable(true);
		mProgressDialog
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
					}
				});

		mProgressDialog.setMessage(title);
		mProgressDialog.show();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MSG_FILE_BEGIN:
				break;

			case MSG_FILE_END:
				mActionState = mExplorerType;
				mProgressDialog.dismiss();
				// This code is not a bug, it need to fall through case
				// MSG_UPDATE_LIST
				sendMessage(obtainMessage(MSG_UPDATE_LIST,
						(Object) FileExplorer.enterDirectory(FileExplorer
								.getDirectory())));
				break;

			case MSG_UPDATE_LIST:
				updateFileList(msg);
				break;

			default:
				break;
			}
		}

		private void updateFileList(Message msg) {
			switch (mExplorerType) {
			case MENU_EXPLORER_LIST:
				mFileListAdapter.setFileArray((String[]) msg.obj);
				mFileListAdapter.notifyDataSetChanged();
				break;

			case MENU_EXPLORER_ICON:
				mFileIconAdapter.setFileArray((String[]) msg.obj);
				mFileIconAdapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
		}
	};

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			EditText editText = (EditText) view.findViewById(R.id.folder_name);
			String folderName = editText.getText().toString();
			Resources res = FileExplorerActivity.this.getResources();
			LogUtils.d(new Exception(), "folderName: " + folderName);

			if (mRename) {
				int position = LIST_ITEM_ID;
				// String oldname = null;

				switch (mExplorerType) {
				case MENU_EXPLORER_LIST:
					if (!mContextMenu) {
						position = mListView.getSelectedItemPosition();
					}
					oldname = (String) mListView.getItemAtPosition(position);
					break;

				case MENU_EXPLORER_ICON:
					if (!mContextMenu) {
						position = mGridView.getSelectedItemPosition();
					}
					oldname = (String) mGridView.getItemAtPosition(position);
					break;

				default:
					return;
				}

				File newFile = new File(FileExplorer.getDirectory(), folderName);

				if (!newFile.exists()) {
					new File(oldname).renameTo(newFile);
				} else {
					String tip = res.getString(R.string.rename_folder_error)
							+ " " + oldname;
					Toast.makeText(FileExplorerActivity.this, tip,
							Toast.LENGTH_LONG).show();
				}

			} else if (!new File(FileExplorer.getDirectory(), folderName)
					.mkdir()) {
				String tip = res.getString(R.string.create_folder_error) + " "
						+ folderName;
				Toast.makeText(FileExplorerActivity.this, tip,
						Toast.LENGTH_LONG).show();
			}

			dialog.dismiss();
			mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_LIST,
					(Object) FileExplorer.enterDirectory(FileExplorer
							.getDirectory())));

			break;

		case DialogInterface.BUTTON_NEGATIVE:
			dialog.dismiss();
			break;

		case DialogInterface.BUTTON_NEUTRAL:
			dialog.dismiss();
			break;

		default:
			break;
		}
		oldname = null;
	}
}
