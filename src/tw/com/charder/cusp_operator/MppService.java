package tw.com.charder.cusp_operator;

import java.util.Vector;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MppService extends Service {
	
	/* 
	 * TCP client for a connection to MPP server
	 * MPP's message
	 *  	1. Instant message 
	 *  	2. PLU update
	 *  	3. System configuration update
	 *  	4. ...
	 */ 
	private TcpClient mppTcpClient;	
	
	Vector<String> prevInfo = new Vector<String>(); 		//To save current data information in order to check whether the next one is the same

	
	public MppService() {
		// TODO Auto-generated constructor stub
		//Log.e("MppService", "constructor...");
		
		mppTcpClient = new TcpClient(GlobalVariables.localServerIP, GlobalVariables.mppServerPort, new TcpClient.DataReceived(){ 		
            @Override
            //here the messageReceived method is implemented 
            public void messageReceived(String message) {            	
            	//TODO: Need to implement the parser of message from MPP server here and decide reactions in accordance with message type
            	String destStr;            	
            	destStr = message.substring(GlobalVariables.PKT_DEST_OFFSET, GlobalVariables.PKT_TYPE_OFFSET);            	            	
            	//Log.d("OPU_MppService", "messageReceived: ["+message+"]");            	
            	
            	//Check if we should handle this packet by checking destination part            
            	if(destStr.matches(GlobalVariables.PKT_DEST_OPERATOR) == true)
            	{
            		int pkt_type = Integer.parseInt(message.substring(GlobalVariables.PKT_TYPE_OFFSET, GlobalVariables.PKT_DATALEN_OFFSET));
            		//int data_length = Integer.parseInt(message.substring(GlobalVariables.PKT_DATALEN_OFFSET, GlobalVariables.PKT_DATA_OFFSET));
            		//String infoData = message.substring(GlobalVariables.PKT_DATA_OFFSET, GlobalVariables.PKT_DATA_OFFSET + data_length);
            		
            		//To check if its packet type is supported or not
            		if(pkt_type == GlobalVariables.PKT_TYPE_UPDATE_XML)
            		{            			
            			//Log.e("OPU_MPPService", "Get notification to update PLU database!!!");
            			handlePLUupdateNotify();            		
            		}
            		else if(pkt_type == GlobalVariables.PKT_TYPE_INSTANT_MSG)
            		{
            			Log.e("OPU_MPPService", "Retrieve instant message from MPP server!!!");
            		
            		}
            		else
            		{
            			Log.e("OPU_MPPService", "unexpected message type: %d"+pkt_type);
            		}
            	}  
                
            }//end of messageReceived() 
        }); 			
	}
	
	private void handlePLUupdateNotify()
	{
		Intent intent = new Intent(GlobalVariables.XML_UPDATE_ACTION);
		
		intent.putExtra(GlobalVariables.DO_UPDATE_PLU, true);
		
		sendBroadcast(intent);
	}
		
	public class LocalBinder  extends Binder {
		MppService getService() {
			return MppService.this;
		}
	}
	
	// This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder(); 
	
	private void startMppClient() {
		Log.d("MppService", "startMppClient()");
		
		HandleMppMsgThread mppClientThread = new HandleMppMsgThread();
		mppClientThread.start();
		
	}
	
	public void stopMppClient() {
		Log.d("MppService", "stopMppClient()");
		mppTcpClient.stopClient();
        mppTcpClient = null;
	}
	
	
	public int buildPacketForCustomer(Vector<String> Data)
	{
		// TODO: construct the formatted packet
		/*
		 * 	Example:
		 * 	  SRC+DST+Type+Data_Length+Data+Suffix
		 *    Data = unitPrice&Units&TotalPrice&Name&Description&Origin&COUNT_MODE&TareWeight 
		 *    "OPU"+"CUU"+"01"+"0024"+"&20.0&600.3&12006.0&Apple&This is a test!&Taiana&1&50.5"+"****"
		 */		
		final String PREFIX = GlobalVariables.PKT_SRC_OPERATOR+GlobalVariables.PKT_DEST_CUSTOMER; //Source: Operator UI; Destination: Customer UI 		
		String strBuffer = new String();
		String strTemp = new String();		 		
		int dataLength = 0;
		int i = 0;
		
		// To identify if it is necessary to update CUSTOMER_UI's information. 
		if(Data.equals(prevInfo) == true)
		{
			//Log.d("MppService", "No data change updates!!!");
			return 0;
		}
		
		strBuffer += PREFIX;
		strBuffer += String.format("%02d", GlobalVariables.PKT_TYPE_DEAL_INFO);		
						
		//Add major sale information for CUSTOMER UI
		for(i=0; i < Data.size(); i++)
		{
			strTemp += GlobalVariables.PKT_DATA_DELIM;
			dataLength += 1;
			strTemp += Data.get(i);			
			dataLength += Data.get(i).length();
		}
		//Log.e("MppService", "Data=["+strTemp+"]; Length = "+dataLength);
		strBuffer += String.format("%04d", dataLength);
		strBuffer += strTemp;
		strBuffer += GlobalVariables.PKT_SUFFIX;
		
		prevInfo = Data;
						
		sendCustomerInfo(strBuffer);
		
		return 1;		
		//Log.e("MppService", "packet content:["+strBuffer+"]");
	}
	
	
	/*
	 *  2013.09.17 by Maurice
	 * 		
	 */
	private void sendCustomerInfo(String info)
	{
		//Log.d("MppService", "snedCustomerInfo(): ["+info+"]");
		mppTcpClient.sendMessage(info);
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.d("MppService", "onCreate()");
		
		startMppClient();
		
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d("MppService", "onDestroy()");
		
		stopMppClient();
        
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {		
		Log.d("MppService", "onStartCommand()");
		/* Start a thread to run a TCP client being connected to the MPP server.  */		
		//startMppClient();
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		Log.d("MppService", "onUnbind()");
		return super.onUnbind(intent);
	}	

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.d("MppService", "onBind()");
		return mBinder;
	}
	
	private class HandleMppMsgThread extends Thread{
		@Override
		public void run()
		{
			Log.d("HandleMppMsgThread", "start to connect to MPP server ...");
			mppTcpClient.run(); 
		}
	}
	
	/*
	private Handler handler=new Handler(){
	    @Override
	    public void handleMessage(Message msg) {
	        super.handleMessage(msg);
	        //showAppNotification();
	    }
	};
	*/
}
