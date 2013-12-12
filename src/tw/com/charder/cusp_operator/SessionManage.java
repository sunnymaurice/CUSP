package tw.com.charder.cusp_operator;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionManage {	
	// Shared Preferences
	SharedPreferences loginInfoPref;
	
	// Editor for Shared preferences
	Editor editor;
	
	// Context
	Context myContext;
	
	// Shared pref mode
	int PRIVATE_MODE = 0;
	
	// Sharedpref file name
	private static final String PREF_FNAME = "UserSessionPref";
	
	// Status about whether someone has logged in (make it public to be accessible from outside)
	public static final String IS_LOGIN = "IsLoggedIn";
	
	// User name (make it public to be accessible from outside)
	public static final String KEY_NAME = "name";
	
	// Group privilege (make variable public to access from outside)
	public static final String KEY_GROUP = "group";
	
	// User login time
	private static final String KEY_LOGIN_TIME = "loginTime"; 
					
	// Constructor
	public SessionManage(Context context){
		myContext = context;
		loginInfoPref = myContext.getSharedPreferences(PREF_FNAME, PRIVATE_MODE);
		editor = loginInfoPref.edit();
	}
			
	private String get_sysTime(){
		
		Calendar hostCal;
		SimpleDateFormat timeFmt;
		String formattedDate;

		hostCal = Calendar.getInstance();

		timeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		formattedDate = timeFmt.format(hostCal.getTime());
		
		return formattedDate;
	}
	

	/**
	 * Save login session
	 * */
	public void saveLoginSession(String name, String group){
		// Storing login value as TRUE
		editor.putBoolean(IS_LOGIN, true);
		
		// Store user name in PREF_FNAME file
		editor.putString(KEY_NAME, name);
		
		// Store user group in PREF_FNAME file
		editor.putString(KEY_GROUP, group);
		
		// Store login time in PREF_FNAME file
		String time = get_sysTime();
		editor.putString(KEY_LOGIN_TIME, time);
		
		// commit changes
		editor.commit();
	}		 
			
	/**
	 * Get stored session data
	 * */
	public void getLoginInfo(String [] sessionData){
		
		sessionData[0] = loginInfoPref.getString(KEY_NAME, null);
		sessionData[1] = loginInfoPref.getString(KEY_GROUP, null);
		sessionData[2] = loginInfoPref.getString(KEY_LOGIN_TIME, null);			
	}
	
	/**
	 * Clear session details
	 * */
	public void clearLoginInfo(){
		// Clearing all data from Shared Preferences
		editor.clear();
		editor.commit();			
	}
	
	/**
	 * Quick check for login status
	 * **/
	// Get Login State
	public boolean isLoggedIn(){
		return loginInfoPref.getBoolean(IS_LOGIN, false);
	}
	
	/**
	 * Check which privileged user has logged in!
	 * **/
	public int groupType(){		
		return Integer.parseInt(loginInfoPref.getString(KEY_GROUP, null));
	}
}
