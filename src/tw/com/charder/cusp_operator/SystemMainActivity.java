package tw.com.charder.cusp_operator;

//import java.io.BufferedInputStream;
import java.io.File;
//import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
/*
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
*/
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SystemMainActivity extends Activity {
	
	WebView mainWebView;
	
	/*
	 *  2013.08.05 Maurice Sun
	 *  Variables for login process 
	 */		
	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();
	// Session Manager Class
	SessionManage userSession;
	EditText edtUserName;
	EditText edtPassword;
	// Button login
	Button loginBtn;
	
	// User Session Data (Test Only)
	String [] UserInfo = {"", "", "", ""};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_system_main);
		
		setupViewComponent();
		//testSDcard();
		//readXmlFromSDcard();
	}
	
	private void setupViewComponent()
	{
		// Session class instance
		userSession = new SessionManage(this);
		// Username, Password input text
		edtUserName = (EditText) findViewById(R.id.edtUserName);
		edtPassword = (EditText) findViewById(R.id.edtPassword);
		// Login Button
		loginBtn = (Button)findViewById(R.id.btnLogin);
		
		
		// Retrieve the interface component from resource class R
		mainWebView = (WebView) findViewById(R.id.webView);
		String testPage = "<html><body>這是 <b>Charder</b> 高階秤的操作者使用介面主要測試頁面，開發中版號v0.1！</body></html>";
		//mainWebView.loadUrl("http://www.google.com");
		mainWebView.loadDataWithBaseURL(null, testPage, "text/html", "utf-8", null);														
	}
	
	public void loginHandler(View v)
	{			
		// Get username, password from EditText
		String username = edtUserName.getText().toString();
		String password = edtPassword.getText().toString();
		
		// Check if username and password are typed				
		if(username.trim().length() > 0 && password.trim().length() > 0)
		{			
			/*
			 * TODO: Verify via looking into a authentication file or a database 
			 */
			
			// For testing puspose username, password is checked with sample data
			// username = 123 (default group is "operator")
			// password = 123					
			if(username.contentEquals("123") && password.contentEquals("123"))
			{
				
				// Creating user login session
				// For testing i am stroing name, email as follow
				// Use user real data
				userSession.saveLoginSession("123", "operator");				
				//Log.e("Maurice", "Going to open OperatorActivity page ...");
				
				// Starting Operator Activity (Go to Operator mode UI)
				Intent operatorIntent = new Intent(this, OperatorActivity.class);				
				startActivity(operatorIntent);														
			}
			// username = admin, password = admin (default group is "administrator")\			
			else if(username.equals("admin") && password.equals("admin"))
			{
				userSession.saveLoginSession("admin", "administrator");
				Log.e("Maurice", "Going to open AdministratorActivity page ...");
				// Starting Administrator Activity (Go to Administrator mode UI)
				//Intent adminIntent = new Intent(this, AdminActivity.class);				
				//startActivity(adminIntent);						
			}
			// username = maintain (default group is "maintainer")
			// password = 12345
			else if(username.equals("maintainer") && password.equals("12345"))
			{
				userSession.saveLoginSession("maintainer", "personale");
				Log.e("Maurice", "Going to open Maintenance page ...");
				// Starting Maintenance Activity (Go to Maintainer mode UI)
				//Intent maintainIntent = new Intent(this, MaintainerActivity.class);				
				//startActivity(maintainIntent);
			}
			else{
				// username / password doesn't match
				alert.showAlertDialog(this, "Login failed..", "Username/Password is incorrect", false);
			}				
		}
		else
		{
			// user didn't entered username or password
			// Show alert to ask user to enter information
			alert.showAlertDialog(this, "Login failed..", "Please enter username and password", false);
		}
	}
	
	/*
	 *  Moved to PriceListUnitXMLParser.java
	 */
	/*
	protected void readXmlFromSDcard()
	{
		InputStream inStream = null;
		PriceListUnittXMLParser myParser = null;
		
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED) ){ 
			try { 
			     //取得SD卡路徑 (/mnt/sdcard)
			     File SDCardpath = Environment.getExternalStorageDirectory();
			     Log.e("SystemMain", "SD card path: ["+SDCardpath.toString()+"]");
			     File myFile = new File( SDCardpath.getAbsolutePath() + "/product.xml" ); 
			     inStream = new FileInputStream(myFile);
			     
			     //Read File
			     //inStream = openFileInput(myFilePath.toString());
			     
			     myParser = new PriceListUnittXMLParser();
			     
			     myParser.parse(inStream);
			     
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		else
			Log.e("SystemMain", "Cannot detect any external storage...");
		// To demo the data of each PLU item stored in the ArrayList
		for(int i=0; i < myParser.pluItemsInfo.size(); i++)
		{
			Log.e("SystemMainActivity", "Item: "+myParser.pluItemsInfo.get(i).get("name")+" price= "+ myParser.pluItemsInfo.get(i).get("unitPrice"));
		}
	}
	*/
	/*
	 *  Test function to practice create and write data to a new file in external SD card.
	 */
	protected void testSDcard()
	{
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED) ){ 
			try { 
			     //取得SD卡路徑 (/mnt/sdcard)
			     File SDCardpath = Environment.getExternalStorageDirectory();
			     Log.e("SystemMain", "SD card path: ["+SDCardpath.toString()+"]");
			     File myDataPath = new File( SDCardpath.getAbsolutePath() + "/myData" ); 
			     
			     if( !myDataPath.exists() ) 
			    	 myDataPath.mkdirs(); 
			     //將資料寫入到SD卡 
			     FileWriter myFile = new FileWriter( SDCardpath.getAbsolutePath() + "/myData/test.txt" ); 
			     myFile.write("This is a test."); 
			     myFile.close(); 
			} 
			catch (IOException e) { 
			    e.printStackTrace(); 
			} 
		}
		else
			Log.e("SystemMain", "Cannot detect any external storage...");
	} 			

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.system_main, menu);
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// This method is called once the menu is selected
		 switch (item.getItemId()) {
		 
	        case R.id.menu_settings:
	            Intent i = new Intent(this, SettingsActivity.class);
	            startActivityForResult(i, 1);
	            break;
	        //TODO: Expand more settings such as scale setup, account manager, etc.    
	 
		 }
	 
	        return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {		
		super.onActivityResult(requestCode, resultCode, data);
		 //According to startActivityForResult's 2nd parameter value, we can decide what the next task is.
		 switch (requestCode) {
	        case 1:
	            //TODO: Set IP here and pass down the PLU type setting to Operator Activity via some mean ...
	        	//Process process = Runtime.getRuntime().exec(prog);
	        	showSettings();
	        	break;
	        case 2:
	        	//TODO: Do something...	
	            break;
	 
	        }
	}
	
	 private void showSettings() {
	        SharedPreferences sharedPrefs = PreferenceManager
	                .getDefaultSharedPreferences(this);
	 
	        StringBuilder builder = new StringBuilder();
	 
	        builder.append("\n IP address: "
	                + sharedPrefs.getString("prefNetworkIP", "NULL"));
	        
	        builder.append("\n Network mask: "
	                + sharedPrefs.getString("prefNetworkMask", "NULL"));
	        
	        builder.append("\n Sync Frequency: "
	                + sharedPrefs.getString("prefPLUType", "NULL"));
	        
	        Toast.makeText(this, "System settings: "+ builder.toString(), Toast.LENGTH_LONG).show();
	    }

	protected void onPause(){
		super.onPause();	
	}
	
	protected void onRsume(){
		super.onResume();		
	}
	
	protected void onStop(){
		super.onStop();
		/*
		 *  TODO: Stop ADLC service running in the background.
		 */
	}
	
	/*
	protected void doDownload(final String urlLink, final String fileName) {
        Thread downloadFileTask = new Thread() {

            public void run() {
            	  File root = android.os.Environment.getExternalStorageDirectory();               
                  File dir = new File (root.getAbsolutePath() + "/PLU/"); 
                  if(dir.exists()==false) {
                          dir.mkdirs();
                     }
            	  //Save the path as a string value

                try {
                        URL url = new URL(urlLink);
                        Log.i("FILE_NAME", "File name is "+fileName);
                        Log.i("FILE_URLLINK", "File URL is "+url);
                        URLConnection connection = url.openConnection();
                        connection.connect();
                        // this will be useful so that we can show a typical 0-100% progress bar later
                        int fileLength = connection.getContentLength();

                        // download the file
                        InputStream input = new BufferedInputStream(url.openStream());
                        OutputStream output = new FileOutputStream(dir+"/"+fileName);

                        byte data[] = new byte[1024];
                        long total = 0;
                        int count;
                        while ((count = input.read(data)) != -1) {
                            total += count;

                            output.write(data, 0, count);
                        }

                        output.flush();
                        output.close();
                        input.close();
                    } catch (Exception e) {
                    	 e.printStackTrace();
                    	 Log.i("ERROR ON DOWNLOADING FILES", "ERROR IS" +e);
                    }
            }
        };
        downloadFileTask.start();      
    }
    */
}
