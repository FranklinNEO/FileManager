package com.archermind.filemanager;

import android.app.Application;

public class FileManagerApp extends Application {
    public static final String TAG = "Application";
    private static FileManagerApp sMe = null;
    
	public FileManagerApp() {
		sMe = this;
	}
	
	public static FileManagerApp getInstance() {
		return sMe;		
	}
 
    public void onCreate() {
        super.onCreate();    
    }
    
    public void onTerminate() {
        super.onTerminate();  
    }
}





