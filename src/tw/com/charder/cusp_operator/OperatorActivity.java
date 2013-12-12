package tw.com.charder.cusp_operator;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

/*  
 * 	Operator Activity Functions:
 *  	1. A NDP client to get ADLC data 
 * 		2. A MPP client to send and receive data to Customer UI via MPP server
 * 		3. Basic PLU list loading
 */

public class OperatorActivity extends Activity {
	
	private boolean DebugMode = false; 		//Use to show debug message via logcat only. 
	/*
	 * Construct a ServiceConnection object myMppServiceConn to use MppService
	 */	
	private MppService myMppService = null;
	
	
	private PluItemsImageAdapter myPluDataAdapter = null;
	private GridView pluGridView = null;
	
	/*
	 *  Broadcast Receiver to get the unit price of a selected item.
	 */	
	//final static String OPERATOR_ACTION = "CUSP.OperatorActivity.ReceivePLU";
	private PluReceiver pluDataReceiver;
	private MppReceiver mppNotifyReceiver;

	// PLU information retrieved from xml file
	private static int pluNumberInt = 0;
	private static String pluNameStr = new String();
	private static float unitPriceFlo = 0;
	private static String calWayStr = new String();   
	private static float totalPriceFlo = 0;
	private static String productDescStr = new String();
	private static String productOrigStr = new String();
	private static int calCountInt = 0;
	private static int itemCountMode = GlobalVariables.COUNT_BY_WEIGHT_MODE;  //default value at beginning 
	
	/*
	 * The following are 5 items we want to update.
	 */
	private TextView textWeightLabel;
	private TextView pluNameDisplay;
	private TextView pluNumDisplay;
	private TextView weightDisplay;
	private TextView tareDisplay;
	private TextView unitPriceDisplay;
	private TextView totalPriceDisplay;
	//debug only
	private TextView zeroDisplay;
	private TextView maxCapacityDisplay;
	
	
	// TCP client for a connection to NDP server 
	private TcpClient ndpTcpClient;	
	final private static int ndpPacketSize = 25;			//size of per packet from NDP server		
		
	// Information gained from ADLC's data and display panes for ADLC information.
	final private static int STABLE_COUNT = 3;				//if there is 3 continuous stable flag are observed, we called it truly stable.  	
	private static int prevStableCounter = 0;				//to record how many consecutive stable states are in a series.
	private static double adlcWeightValue;
	private static double temperatureValue;
	private static double hLevelX;
	private static double hLevelY;
	private static String weightStr;		// String of weight to display in "weightDisplay" TextView 
	private static String packStr;			// String of the number of packs/sets to display in "weightDisplay" TextView
	//private static String setStr;			// String of the number of sets to display in "weightDisplay" TextView
	private static String temperatureStr;
	private static String hLevelXStr;
	private static String hLevelYStr;
	private static String totalPriceStr;
	private boolean isStable;
	private TextView stableDisplay;
	private TextView temperatureDisplay;
	//private TextView horzonXDisplay;
	//private TextView horzonYDisplay;
	
		
	/* 
	 * 2013.10.24 by Maurice Sun for soft keypad
	 *		 
	 */
	GridView mKeypadGrid;
	TextView userInputText;
	TextView mStackText;	
	KeypadAdapter mKeypadAdapter;	
	boolean resetInput = false;
	boolean hasFinalResult = false;
	String mDecimalSeperator;
	Stack<String> mInputStack;
	Stack<String> mOperationStack;
	
	 /* 2013.11.04 by Maurice for TARE and ZERO functions */
	private static final double startZero = 0; // Use software simulation, it should be retrieved from ADLC at startup.
	private static double referenceZero = startZero;
	private static double tareWeight = 0;		// For Tare function
	private static double prevTareWeight = 0;	
	private static double grossWeight = 0;		// For Zero function	
	private static double netWeight = 0;
	private static double maxCapacity = GlobalVariables.SCALE_CAPACITY;	// The max weight value can be shown. 	
	private boolean isTare = false;	

	/*
	 *  Variables for implementation of detecting network connection
	 */	
	// Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean ethernetConnected = false;
    // Whether the display should be refreshed.
    public static boolean refreshDisplay = true;
    // The BroadcastReceiver that tracks network connectivity changes.
    private NetworkReceiver networkReceiver = new NetworkReceiver();
        		
	private ServiceConnection myMppServiceConn = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d("OperatorActivity", "onServiceConnected " + name.getClassName());
			myMppService = ((MppService.LocalBinder)service).getService();
			Log.d("OperatorActivity", "onServiceConnected done!!!");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {			
			myMppService = null;
			Log.d("OperatorActivity", "onServiceDisconnected" + name.getClassName());			
		}
		
	};
	
	/*
	 *	Start MPP service 
	 */
	private void startMppService() 
	{
		boolean i;
		Intent it = new Intent(this, MppService.class);
		
		//Log.d("OperatorActivity", "startMppService");
		try{
			i= bindService(it, myMppServiceConn, BIND_AUTO_CREATE);		
			//Log.e("OperatorActivity", "bindServece retrun: "+i);
		} catch(SecurityException e)
		{
			Log.e("OperatorActivity", "find SecurityException...");
		}							
	}
	/*
	 * Stop MPP service
	 */	
	private void stopMppService()
	{
		Log.d("OperatorActivity", "stopMppService");
		unbindService(myMppServiceConn);
		
		if(myMppService != null)
		{
			myMppService.stopMppClient();
		}
		myMppService = null;
	}
				
	/*
	 *  Created:  	2013.09.18 by Maurice.
	 *  Function:	Transfer essential information for MPP service to send a packet to CUSTOMER UI via MPP server. 
	 *  Description: 	 
	 *  Revision:		
	 */
	private void CreateFormattedPacket()
	{         
    	Vector<String> saleInfo = new Vector<String>();
    	
    	if(totalPriceStr == null || pluNameStr == null || productDescStr == null || productOrigStr == null)
    		return;
    	
    	saleInfo.add(0, Float.toString(unitPriceFlo));
    	//Support COUNT by set and package and need to modify customer's receiver process code.
    	if( itemCountMode == GlobalVariables.COUNT_BY_WEIGHT_MODE)    	
    		saleInfo.add(1, Double.toString(netWeight));
    	else
    		saleInfo.add(1, packStr);
    	
    	saleInfo.add(2, totalPriceStr);
    	saleInfo.add(3, pluNameStr);
    	saleInfo.add(4, productDescStr);
    	saleInfo.add(5, productOrigStr);
    	//2013.11.11 by Maurice. To support COUNT by set and package mode.
    	saleInfo.add(6, Integer.toString(itemCountMode));
    	saleInfo.add(7, Double.toHexString(tareWeight));
    	
    	if(myMppService != null)
		{    		
    		myMppService.buildPacketForCustomer(saleInfo);    		
		}
    	else
    	{
    		Log.e("OperatorActivity", "CreateFormattedPacket cannot work since myMppService is null");
    	}
	}
	
	/*   
	 * 	 Created:  2013.08.27 by Maurice Sun
	 * 	 Function: Broadcast receiver of OperatorActivity to get unit price of a selected item
	 *   Description: 
	 *   		   Get to know which an item of product is selected so that we can retrieve the product information
	 *   		   such as PLU number, name, unit price,   
	 *   Revision: 
	 */
   	private class PluReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context ct, Intent it) {
			/* Handle display of PLU number field */  
			try {
				String sPLUnum = it.getStringExtra(GlobalVariables.KEY_ID);
				if(sPLUnum != "")			
					pluNumberInt = Integer.parseInt(sPLUnum);
				else//This shall not happen since the xml file must contain correct information.
					pluNumberInt = 0;								
															
				pluNumDisplay.setText(Float.toString(pluNumberInt));
				
			} catch (NumberFormatException nx) {
	            Log.e("PluReceiver", "Receive invalid integer: plu_num");
	        }
			
			/* Handle display of product name field */
			pluNameStr = it.getStringExtra(GlobalVariables.KEY_NAME);
			pluNameDisplay.setText(pluNameStr);
			
			/* Handle display of unit price field */			
			try {
				String sPrice = it.getStringExtra(GlobalVariables.KEY_UNIT_PRICE);
				if(sPrice != "")				
					unitPriceFlo = Float.parseFloat(sPrice);
				else//This shall not happen since the xml file must contain correct information.
					unitPriceFlo = 0;
				// Display unit price field												
				unitPriceDisplay.setText(Float.toString(unitPriceFlo));
				
			} catch (NumberFormatException nx) {
	            Log.e("PluReceiver", "Receive invalid float: unit_price");
	        }
			/* Catch the way to calculate the total price */
			calWayStr = it.getStringExtra(GlobalVariables.KEY_CAL_UNIT);
			
			/* Handle the total price according to the way to calculate */
			if(calWayStr!=null)
			{
				if(calWayStr.matches(GlobalVariables.CAL_BY_WEIGHT))
				{
					itemCountMode = GlobalVariables.COUNT_BY_WEIGHT_MODE;
					textWeightLabel.setText("重量  (g)");
					weightDisplay.setText(weightStr);
					
					userInputText.setText("0");
				}
				else if(calWayStr.matches(GlobalVariables.CAL_BY_PACK))
				{
					itemCountMode = GlobalVariables.COUNT_BY_PACK_MODE;
					textWeightLabel.setText("整包");
					
					packStr = "0";
					weightDisplay.setText(packStr);
					totalPriceDisplay.setText("0");
					
					/* Set the unit price so that the unit price would be add to operation stack after press MULTIPLY button */
					userInputText.setText(Float.toString(unitPriceFlo));
				}
				else if(calWayStr.matches(GlobalVariables.CAL_BY_SET))
				{	
					try{
						String count = it.getStringExtra(GlobalVariables.KEY_CAL_COUNT);
						itemCountMode = GlobalVariables.COUNT_BY_SET_MODE;
						
						packStr = "0";
						weightDisplay.setText(packStr);
						totalPriceDisplay.setText("0");
						
						if(count != "")
						{	
							calCountInt = Integer.parseInt(count);
							textWeightLabel.setText(count+"個/組");
							/* Set the unit price so that the unit price would be add to operation stack after press MULTIPLY button */
							userInputText.setText(Float.toString(unitPriceFlo));
						}
						else
						{	
							calCountInt = 0;
							Log.e("PluReceiver", "Calculate by a set with a zero unit is wrong!!!");
						}
					} catch (NumberFormatException nx) {
			            Log.e("PluReceiver", "Receive invalid float: cal_count");
			        }																
				}
				else
					Log.e("PluReceiver", "計算方式單位: "+calWayStr+" 不在規定範圍內!!!");
			}	
								
										
			//Toast.makeText(ct, "PLU receiver got: unitPrice - "+unitPrice+" -way- "+calculatedWay+" -count- "+calculatedCount, Toast.LENGTH_LONG).show();
			
			productDescStr = it.getStringExtra(GlobalVariables.KEY_DESC);
			productOrigStr = it.getStringExtra(GlobalVariables.KEY_ORIGIN);
		}   		
   	};
   	
   	/*
   	 * 	Created: 	2013.09.18 by Maurice.
   	 * 	Function:	Retrieve the notification from MPP service to manage to know the reaction. 
   	 * 	Description:
   	 * 				1. Reload PLU xml file after receiving update notification in MPP service.
   	 * 				2. Display vital instant system broadcast message.
   	 * 				3. ...
   	 * 	Revision:
   	 * 				2013.09.27 by Maurice -> Implement PLU configuration update 
   	 * 					
   	 */
	public class MppReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {			
			String action = intent.getAction();
			
			if(action.equals(GlobalVariables.XML_UPDATE_ACTION))
			{
				//Try to reload product.xml to refresh Grid image view list
				//Log.d("OPU_MPPService", "get plu data update action...");
				
				boolean do_update = intent.getBooleanExtra(GlobalVariables.DO_UPDATE_PLU, false);
				
				if(do_update)
				{
					//Log.e("OPU_MPPService", "start to do plu data update...");
					Toast.makeText(context, "PLU configuration is going to be updated...", Toast.LENGTH_LONG).show();
					
					try {
						myPluDataAdapter = new PluItemsImageAdapter(context);
					} catch (IOException e) {					
						e.printStackTrace();
					}
					pluGridView.setAdapter(myPluDataAdapter);
					((BaseAdapter) pluGridView.getAdapter()).notifyDataSetChanged();
				}
			}
			else if(action.equals(GlobalVariables.SYS_NOTICE_ACTION))
			{
				
			}
			else if(action.equals(GlobalVariables.SYS_MESSAGE_ACTION))
			{
				
			}
			else if(action.equals(GlobalVariables.SYS_ERROR_ACTION))
			{
				
			}
			else
			{
				Log.d("OPU_MPPService", "receive unsupported action...");
			}
		}
	}
	
	private void setSoftKeypadView()
	{
		// Create the stack
			mInputStack = new Stack<String>();
			mOperationStack = new Stack<String>();
		// For decimal mark "."	
			DecimalFormat currencyFormatter = (DecimalFormat) NumberFormat.getInstance();
			char decimalSeperator = currencyFormatter.getDecimalFormatSymbols().getDecimalSeparator();
			mDecimalSeperator = Character.toString(decimalSeperator);

		// Get reference to the keypad button GridView
			mKeypadGrid = (GridView) findViewById(R.id.grdKeypadButtons);

		// Get reference to the user input TextView
			userInputText = (TextView) findViewById(R.id.txtInput);
			userInputText.setText("0");
				

			mStackText = (TextView) findViewById(R.id.txtStack);

		// Create Keypad Adapter
			mKeypadAdapter = new KeypadAdapter(this);
		// Set adapter of the keypad grid
			mKeypadGrid.setAdapter(mKeypadAdapter);
			
		// Set button click listener of the keypad adapter
			mKeypadAdapter.setOnButtonClickListener(new OnClickListener() {
					@Override
				public void onClick(View v) {
					Button btn = (Button) v;
					// Get the KeypadButton value which is used to identify the
					// keypad button from the Button's tag
					KeypadButton keypadButton = (KeypadButton) btn.getTag();

					// Process keypad button
					ProcessKeypadInput(keypadButton);
				}
			});

			mKeypadGrid.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {

				}
			});
	}
	
	private void checkTare()
	{		
		if(adlcWeightValue <= (GlobalVariables.SCALE_CAPACITY + grossWeight) )
		{
			isTare = true;			
			tareWeight = adlcWeightValue - grossWeight;
			maxCapacity -= (tareWeight - prevTareWeight);
			prevTareWeight = tareWeight;
			maxCapacityDisplay.setText(Double.toString(maxCapacity));					
		}
		else
		{			
			AlertDialogManager tareBtnAlert = new AlertDialogManager();
			tareBtnAlert.showAlertDialog(this, "Can't TARE", "Exceeds the tolerence value: "+ GlobalVariables.GROSS_ZERO_MAX, false);
		}
	}
	
	private void KeypadTareBtnFunc()
	{
		/* Check if ADLC is in stable state, if not, TARE function is not allowed. */
		if(!isStable)
		{
			Toast.makeText(this, "Scale is not stable. Please check the condition first.!", Toast.LENGTH_LONG).show();
			return;
		}
		
		if(isTare == true)
		{
			if(adlcWeightValue == startZero)
			{
				isTare = false;				
				maxCapacity = GlobalVariables.SCALE_CAPACITY;
				tareWeight = 0;				
				maxCapacityDisplay.setText(Double.toString(maxCapacity));
				Toast.makeText(this, "Remove TARE weight!!!", Toast.LENGTH_LONG).show();
			}
			else
			{
				checkTare();
			}
		}
		else
		{
			checkTare();
		}
		tareDisplay.setText(Double.toString(tareWeight));
	}
	
	private void KeypadZeroBtnFunc()
	{
		AlertDialogManager zeroBtnAlert = new AlertDialogManager();
		
		/* Check if ADLC is in stable state, if not, ZERO function is not allowed. */
		if(!isStable)
		{
			Toast.makeText(this, "Scale is not stable. Please check the condition first.!", Toast.LENGTH_LONG).show();
			return;
		}
		
		if(!isTare)//Don't allow doing ZERO function when TARE function is enabled.
		{
			if( Math.abs(adlcWeightValue - startZero) <= GlobalVariables.GROSS_ZERO_MAX)
			{					
				grossWeight = adlcWeightValue - startZero;					
				maxCapacity += (grossWeight - referenceZero);
				
				zeroDisplay.setText(Double.toString(grossWeight));
				maxCapacityDisplay.setText(Double.toString(maxCapacity));
				//Force to update the total weigh info in GUI
				weightDisplay.setText(Double.toString(adlcWeightValue - grossWeight - tareWeight));
				referenceZero = grossWeight;//record the last zero point			
			}
			else
			{					
				zeroBtnAlert.showAlertDialog(this, "Can't USE ZERO", "Only ZERO up to tolerence value: "+ GlobalVariables.GROSS_ZERO_MAX, false);
			}
		}
		else 
		{				
			zeroBtnAlert.showAlertDialog(this, "Can't USE ZERO", "Disable TARE function first!!!", false);
		}
		
	}
	
	private void ProcessKeypadInput(KeypadButton keypadButton) {
		Toast.makeText(this, keypadButton.getText(), Toast.LENGTH_SHORT).show();
		String input_text = keypadButton.getText().toString();
		String currentInput = userInputText.getText().toString();	
		String evalResult = null;

		switch (keypadButton) {
		
		case CLEAR: // Handle clear input and stack					
			//if(itemCountMode == GlobalVariables.COUNT_BY_SET_MODE || itemCountMode == GlobalVariables.COUNT_BY_PACK_MODE)
			if(itemCountMode != GlobalVariables.COUNT_BY_WEIGHT_MODE)
			{
				/* Set the unit price so that the unit price would be add to operation stack after press MULTIPLY button */
				userInputText.setText(Float.toString(unitPriceFlo));
				weightDisplay.setText("0");
				totalPriceDisplay.setText("0");				
			}
			else
				userInputText.setText("0");
			clearStacks();	
			break;
		case DECIMAL_SEP: // Handle decimal remark
			if (hasFinalResult || resetInput) {
				userInputText.setText("0" + mDecimalSeperator);
				hasFinalResult = false;
				resetInput = false;
			} 
			else if (currentInput.contains("."))
				return;
			else
				userInputText.append(mDecimalSeperator);
			break;	
		case MULTIPLY: 
			/*
			 * For the sake of product items with a package			  
			 */
			/*
			if (resetInput) {				
				try
				{
					mInputStack.pop();
					mOperationStack.pop();
				}catch(EmptyStackException ese)
				{					
					break;
				}
			} 
			else 
			{*/
				//Log.e("ProcessKeypadInput","add ["+currentInput+"]");				
				mInputStack.add(currentInput);		
				mOperationStack.add(currentInput);
			//}
			//Log.e("ProcessKeypadInput","add ["+input_text+"]");	
			mInputStack.add(input_text);
			mOperationStack.add(input_text);

			dumpInputStack();
			
			
			evalResult = evaluateResult(false);
			if (evalResult != null)
			{
				//Log.e("ProcessKeypadInput", "In multiply, set result:"+evalResult+"!");
				userInputText.setText(evalResult);
			}
			
	
			resetInput = true;
			break;
		case CALCULATE:
			if (mOperationStack.size() == 0)
			{
				
				break;
			}
				
			//Log.e("CALCULATE", "OpStack add ["+currentInput+"]");
			mOperationStack.add(currentInput);
			// This part is used for calculator function.
			
			evalResult = evaluateResult(true);
			if (evalResult != null) {
				clearStacks();
				userInputText.setText(evalResult);
				//if(itemCountMode == GlobalVariables.COUNT_BY_SET_MODE || itemCountMode == GlobalVariables.COUNT_BY_PACK_MODE)
				if(itemCountMode != GlobalVariables.COUNT_BY_WEIGHT_MODE)
					totalPriceDisplay.setText(evalResult);
				resetInput = false;
				hasFinalResult = true;
			}
				
			break;
		/*	
		case ADD: 		
			break;
		*/
		case TARE: // Do TARE via software here instead of sending ADLC command.
			KeypadTareBtnFunc();
			//Force to update the total weigh info in GUI
			weightDisplay.setText(Double.toString(adlcWeightValue - grossWeight - tareWeight));
			break;	
		case RST_ZERO:
			KeypadZeroBtnFunc();
			break;
		default:
			// if it is a number
			if (Character.isDigit(input_text.charAt(0))) {
				if (currentInput.equals("0") || resetInput || hasFinalResult) {				
					Log.e("default(special)", "set userInputText:["+input_text+"]");
					userInputText.setText(input_text);
					resetInput = false;
					hasFinalResult = false;
				} 
				else 
				{
					Log.e("default", "set userInputText:["+input_text+"]");
					userInputText.append(input_text);
					resetInput = false;
				}
			}
			break;

		}

	}
	
	private void clearStacks() {
		mInputStack.clear();
		mOperationStack.clear();
		mStackText.setText("");
	}
	
	private void dumpInputStack() {
		Iterator<String> it = mInputStack.iterator();
		StringBuilder sb = new StringBuilder();

		while (it.hasNext()) {
			CharSequence iValue = it.next();
			sb.append(iValue);

		}

		mStackText.setText(sb.toString());
	}
	/* Transform a double value to a formated string. */
	static private String customFormat(String format, double value ) 
	{
	      DecimalFormat myFormatter = new DecimalFormat(format);
	      String output = myFormatter.format(value);	
	      return output;
	}
	
    //For calculator usage 
	private String evaluateResult(boolean requestedByUser) {
		String resultStr = new String();
		
		//Log.e("evaluateRes", "requestedByuser is:"+requestedByUser);
		
		if ((!requestedByUser && mOperationStack.size() != 4)
				|| (requestedByUser && mOperationStack.size() != 3))
		{			
			Log.e("evaluateRes", "OpStack size is:"+mOperationStack.size());
			return null;
		}
			
		String left = mOperationStack.get(0);
		String operator = mOperationStack.get(1);
		String right = mOperationStack.get(2);
		String tmp = null;
		
		/* In COUNT by PACK/SET mode, the value of left operand should equal to unit price. */
		if(itemCountMode != GlobalVariables.COUNT_BY_WEIGHT_MODE)
		{
			if(!left.equals(Float.toString(unitPriceFlo)))
			{
				AlertDialogManager wrongInputAlert = new AlertDialogManager();
				wrongInputAlert.showAlertDialog(this, "Wrong Input", "Please follow this example: * 12 =", false);
				
				clearStacks();
				/* Set the unit price so that the unit price would be add to operation stack after press MULTIPLY button */
				userInputText.setText(Float.toString(unitPriceFlo));
				weightDisplay.setText("0");
				totalPriceDisplay.setText("0");		
				return null;
			}			
		}
		
		if (!requestedByUser)
		{
			tmp = mOperationStack.get(3);
			//Log.e("evaluateRes", "tmp ="+tmp);
		}
			

		double leftVal = Double.parseDouble(left.toString());
		double rightVal = Double.parseDouble(right.toString());
		double result = Double.NaN;
			
		if (operator.equals(KeypadButton.MULTIPLY.getText())) 
		{
			
			if( itemCountMode == GlobalVariables.COUNT_BY_SET_MODE)
			{
				double tmpSetNum = rightVal/calCountInt;
				result = leftVal * tmpSetNum;
				//setStr = new String("");						
				packStr = Double.toString(tmpSetNum);
				packStr += "組";				
				weightDisplay.setText(packStr);
			}        	
			else
			{
				result = leftVal * rightVal;
				
				if(itemCountMode == GlobalVariables.COUNT_BY_PACK_MODE)
				{
					//packStr = new String("");
					packStr = Double.toString(rightVal);
					packStr += "包";
					weightDisplay.setText(packStr);
				}				
			}
		}
			
		resultStr = customFormat("$###,###.##", result);
		//resultStr = doubleToString(result);
			
		if (resultStr == null)
			return null;
		
		mOperationStack.clear();
			
		if (!requestedByUser) {
			//Log.e("evaluateRes","add opStack: ["+resultStr+"] and ["+tmp+"]");
			mOperationStack.add(resultStr);
			mOperationStack.add(tmp);
		}				
			
		return resultStr;
	}
	
	/*
	private String doubleToString(double value) {
		if (Double.isNaN(value))
			return null;

		long longVal = (long) value;
		if (longVal == value)
			return Long.toString(longVal);
		else
			return Double.toString(value);

	}
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_operator);
		// Show the Up button in the action bar.
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		textWeightLabel = (TextView)findViewById(R.id.textWeight);		
		
		pluNameDisplay = (TextView)findViewById(R.id.textPluNameDisplay);
		pluNumDisplay = (TextView)findViewById(R.id.textPluNumDisplay);
		weightDisplay = (TextView)findViewById(R.id.textWeightDisplay);
		tareDisplay = (TextView)findViewById(R.id.textTareWeightDisplay);
		unitPriceDisplay = (TextView)findViewById(R.id.textUnitPriceDisplay);
		totalPriceDisplay = (TextView)findViewById(R.id.textTotalPriceDisplay);
		//for demo information from ADLC only
		stableDisplay = (TextView)findViewById(R.id.textStableDisplay);
		temperatureDisplay = (TextView)findViewById(R.id.textTemperatureDisplay);		
		//horzonXDisplay = (TextView)findViewById(R.id.textHorzonXDisplay);
		//horzonYDisplay = (TextView)findViewById(R.id.textHorzonYDisplay);
		zeroDisplay = (TextView)findViewById(R.id.textGrossDisplay);
		maxCapacityDisplay = (TextView)findViewById(R.id.textMaxCapacityDisplay);
		
		maxCapacityDisplay.setText(Integer.toString(GlobalVariables.SCALE_CAPACITY));
		tareDisplay.setText("0");
		zeroDisplay.setText("0");
		
		setupPluGridView();			
		setSoftKeypadView();
	    
		// Connect to NDP server to retrieve ADLC data. 
		new GetWeightInfoTask().execute("");		
	}
	
	private void setupPluGridView()
	{		
		pluGridView = (GridView) findViewById(R.id.pluGridView);			
		//pluGridView.setAdapter(new PluItemsImageAdapter(this));			
		try {
			myPluDataAdapter = new PluItemsImageAdapter(this);
		} catch (IOException e) {			
			e.printStackTrace();
		}
		pluGridView.setAdapter(myPluDataAdapter);		
	}

		
	 @Override
	 public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	        case android.R.id.home:
	            NavUtils.navigateUpFromSameTask(this);
	            return true;
	        }
	        return super.onOptionsItemSelected(item);
	    }
	 		 	 
	@Override
	protected void onPause() { 
	        super.onPause(); 
	        
	        Log.d("OperatorActivity", "onPause()");	    
	  
	}

	@Override
	protected void onDestroy() {		
		super.onDestroy();
		
		Log.d("OperatorActivity", "onDestroy()");
		// disconnect the connection between to NDP server 
		ndpTcpClient.stopClient(); 
        ndpTcpClient = null; 
        
        stopMppService();
	}

	@Override
	protected void onResume() {		
		super.onResume();
		
		Log.d("OperatorActivity", "onResume()");
		
		startMppService();
		
		// Connect to NDP server to retrieve ADLC data. 
		new GetWeightInfoTask().execute(""); 
	}
	
	@Override
	protected void onStart() {
		//Log.d("OperatorActivity", "onStart()");
		super.onStart();
		
		/* Register Broadcast Receiver to attain PLU information of a selected item */
		pluDataReceiver = new PluReceiver();
		IntentFilter intentPluFilter = new IntentFilter();     
		intentPluFilter.addAction(GlobalVariables.PLU_ACTION);     
		registerReceiver(pluDataReceiver, intentPluFilter);
		/* Register Broadcast Receiver to get different type of messages from MPP server */
		mppNotifyReceiver = new MppReceiver();
		IntentFilter intentMppFilter = new IntentFilter();
		intentMppFilter.addAction(GlobalVariables.XML_UPDATE_ACTION);
		intentMppFilter.addAction(GlobalVariables.SYS_NOTICE_ACTION);
		intentMppFilter.addAction(GlobalVariables.SYS_MESSAGE_ACTION);
		registerReceiver(mppNotifyReceiver, intentMppFilter);
		
		 // Register Broadcast Receiver to track connection type and status.
        IntentFilter filterConnTypeFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        networkReceiver = new NetworkReceiver();
        this.registerReceiver(networkReceiver, filterConnTypeFilter);
                				
	}

	@Override
	protected void onStop() {
		Log.d("OperatorActivity", "onStop()");
		/* Unregister Broadcast Receiver */
		unregisterReceiver(pluDataReceiver);
		unregisterReceiver(mppNotifyReceiver);
		unregisterReceiver(networkReceiver);
		
		super.onStop();
	}
	
	@Override
	/* Saving data */
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		// TODO: have to save the necessary data here later so that the system can pause this activity after a certain period of idle time.
		//		 Otherwise, this activity is unable to be paused.
		/*
		 *  Called when:
		 *  	User rotates screen,
		 *  	changes language,
		 *  	APP is hidden and Android needs the memory.
		 *  Not called when user hits Back button.
		 */
		/* To save some data here */
		//outState.putAll(map);
	}
	
	/*
	 *  2013.08.09 by Maurice 
	 *  Description: Interpret the ADLC data from NDP server.
	 *  Data Format: To Be Defined...	
	 *  	1. Might need to study JAVA Regular expression to handle formation of received message.
	 *  	2. 
	 *  Revision History:
	 *  	1. 
	 *  	2. 	 
	 */
	int parsingAdlcData(int index, String data)
	{		    	   		
		if(data == null)
		{
			Log.e("OperatorActiviy", "pass null string in parsingAdlcData...");
			return 0;
		}
			
		switch (index)
		{		
			case 1://Parsing weight information				
				//TODO: Need to check the format of negative value of weight from ADLC 
        		adlcWeightValue = Integer.parseInt(data)/10;
        		netWeight = adlcWeightValue - grossWeight - tareWeight;
        		weightStr = Double.toString(netWeight);
        		weightStr += " g";        		
        		//Log.e("parsingAdlcData", adlcWeightValue+";"+ grossWeight +";" + netWeight);
        		//weightDisplay.setText(weightStr);
				break;
			case 2://Parsing temperature information				
				temperatureValue = Integer.parseInt(data)/10;
				temperatureStr = Double.toString(temperatureValue);							
				break;
			case 3:// Parsing horizontal level in X aid			
				hLevelX = Integer.parseInt(data)/10;
				hLevelXStr = Double.toString(hLevelX);							
				break;
			case 4:// Parsing horizontal level in Y aid				
				hLevelY = Integer.parseInt(data)/10;
				hLevelYStr = Double.toString(hLevelY);				//				
				break;			
			default:// Unexpected string
				Log.e("OperatorActivity", "unexpected string: "+data+"received");
				return 0;											
		}
		return 1;
	}
/*
	@Override
	public void startActivityForResult(Intent intent, int requestCode,
			Bundle options) {		
		super.startActivityForResult(intent, requestCode, options);
	} 
*/	
	
	/*
	 * 	2013.09.13 by Maurice.
     * 	Received Message Types:
     * 		(1). ADLC information
     *  	(2). NDP's status report
     *  	  	
     */
	private void HandleADLCpacket(String packet)
	{
		//delimiter used to distinguish all the useful ADLC field information
        final String delim = "\\s+"; //equivalent to [ \\t\\n\\x0B\\f\\r]
        
        String flagStr;
        String subStr;
        
		/* 
		 * 	TODO: Up to now, this is a tricky and temporary mean to parse ADLC data. It is supposed to be revised 
         *	      after we have claimed that the communication protocol is firmed. 
         */
        
        if(packet.length() == ndpPacketSize)
        {        	        
        	flagStr = packet.substring(0,3); //
        	//Log.e("onProgressUpdate", "flagStr's length="+flagStr.length()+" value = ["+flagStr+"]");
        	
        	/*
        	 * Parse 3 bytes stable flag section. "*.*" represents stable condition.
        	 */
        	if(flagStr != null)
        	{	        		
        		if(flagStr.contains("*.*") == true)
        		{
        			//adlcStableFlag = true;
        			prevStableCounter++;
        			isStable = true;
        			stableDisplay.setText("Yes");
        			//Log.e("OperatorActivity", "found stable flag is on!!!");
        		}
        		else
        		{
        			//adlcStableFlag = false;
        			prevStableCounter = 0;
        			isStable = false;
        			stableDisplay.setText("No");
        		}
        	}           
        	
        	/*
        	 *  Skip the flag section part and parse the weight/temperature/horizontal X & Y level information
        	 */        
        	subStr = packet.substring(3,packet.length());
        	//Log.e("OperatorActivity", "onProgressUpdate get sub data: ["+subStr+"]");
        
        
        	String [] items = subStr.split(delim);
        	//Log.e("OperatorActivity", "item counts:"+ items.length);
    	
        	for( int i=1; i < items.length; i++)
        	{
        		//Log.e("OperatorActivity", "Parsing token " +i +": " + items[i]);
        		//Always skip the first token when split() is used.
        		/*
        		if(i == 0)
        			continue;
        		else
        		*/
        		parsingAdlcData(i, items[i]);
        	} 
        	
        }
        else{
        	// Ignore it!!!
        	if(DebugMode == true)
        	{
        		Log.e("OperatorActivity", "HandleADLCpacket-unexpected packet: ["+packet+"]");
        	}        		
        }
        			
	}
	
	/*
	 * 	2013.08.30 by Maurice
	 * 	Description: 
	 * 		Only update display of ADLC information when the consecutive stable flag is below #:STABLE_COUNT. 	
	 */
	private void displayWeightModeInfo()
	{
		if(prevStableCounter <= STABLE_COUNT)
		{
			weightDisplay.setText(weightStr);			
			temperatureDisplay.setText(temperatureStr);
			//horzonXDisplay.setText(hLevelXStr);
			//horzonYDisplay.setText(hLevelYStr);
			//Log.e("DisplayInfo","refresh data...");
		}
		
		totalPriceDisplay.setText(totalPriceStr);
		
	}
	
	/*	 
	 * 2013.08.13 by Maurice
	 * 
	 * Implementation of AsyncTask used to create a TCP client to request NDP server to pass down the ADLC information 
	 * and update UI in accordance to the information retrieved.
	 * AsyncTask <TypeOfVarArgParams , ProgressValue , ResultValue>
	 * TypeOfVarArgParams is passed into the doInBackground() method as input, 
	 * ProgressValue is used for progress information and ResultValue must be returned from doInBackground() method 
	 * and is passed to onPostExecute() as a parameter.  
	 */
	public class GetWeightInfoTask extends AsyncTask<String, String, String> { 
		  
        @Override
		protected void onPostExecute(String result) {			
			super.onPostExecute(result);
			//Log.e("GetWeightInfoTask", "onPostExecute...");
						
		}

		@Override
        protected String doInBackground(String... message) {
        	   
            //we create a TCPClient object and 
            ndpTcpClient = new TcpClient(GlobalVariables.localServerIP, GlobalVariables.ndpServerPort, new TcpClient.DataReceived() { 
                @Override
                //here the messageReceived method is implemented 
                public void messageReceived(String message) {                               
                	//this method calls the onProgressUpdate 
                    publishProgress(message); 
                }//end of messageReceived() 
            }); 
            ndpTcpClient.run(); 
  
            return null; 
        }//end of doInBackground 
        
        @Override
        protected void onProgressUpdate(String... NDPdata) {
        	String adlcStr = null;
        	//String CustomerInfo = null;
        
            super.onProgressUpdate(NDPdata);            
            
            if(DebugMode == true)
            {
            	Log.e("OperatorActivity", "onProgressUpdate get NDP data length = "+NDPdata[0].length()+" :["+NDPdata[0]+"]");
            }
                                    	                                 
            adlcStr = NDPdata[0].toString();     
            //Log.e("OperatorActivity", "adlcStr = ["+adlcStr+"]");
            
            
            if( itemCountMode == GlobalVariables.COUNT_BY_WEIGHT_MODE)
        	{
            	HandleADLCpacket(adlcStr);
            
            	/* When any PLU item is pressed, evaluate the total */
            	if(unitPriceFlo != 0)
            	{               		            
            		// Update the price
            		if(netWeight <= 0) //TODO: what shall we display if the weight is negative when TARE function is enable? 
            			totalPriceFlo = 0;
            		else	
            			totalPriceFlo = (float) (netWeight * unitPriceFlo);
            		customFormat("$###,###.##", totalPriceFlo);
            		totalPriceStr = Float.toString(totalPriceFlo);
            	}
            	            	                        	            	            	
            	displayWeightModeInfo();  
			}           
            /* Sending Message to MPP server (port: 9022) */
        	CreateFormattedPacket();                                
        }//end of onProgressUpdate 
        
        
    }//end of "public class GetWeightInfoTask extends AsyncTask"
	
	 
	/*
	 *  2013.08.14 by Maurice 
	 *  TODO: Implementation of AsyncTask used to download PUL XML feed from Server: ???.
	 *        MPP may do this??? 
	 */
    @SuppressWarnings("unused")
	private class DownloadPluXmlTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
        	/*
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            }
            */
        	return null;
        }

        @Override
        protected void onPostExecute(String result) {
        	/*
            setContentView(R.layout.main);
            // Displays the HTML string in the UI via a WebView
            WebView myWebView = (WebView) findViewById(R.id.webview);
            myWebView.loadData(result, "text/html", null);
            */
        }
    }
    
    // Checks the network connection and sets the wifiConnected and ethernetConnected
    // variables accordingly.
    private void updateConnectedFlags() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = (activeInfo.getType() == ConnectivityManager.TYPE_WIFI);
            ethernetConnected = (activeInfo.getType() == ConnectivityManager.TYPE_ETHERNET);
        } else {
            wifiConnected = false;
            ethernetConnected = false;
        }
    }
    
    /**
    *
    * This BroadcastReceiver intercepts the android.net.ConnectivityManager.CONNECTIVITY_ACTION,
    * which indicates a connection change. It checks whether the type is TYPE_WIFI.
    * If it is, it checks whether Wi-Fi is connected and sets the wifiConnected flag in the
    * main activity accordingly.
    *
    */
   public class NetworkReceiver extends BroadcastReceiver {

       @Override
       public void onReceive(Context context, Intent intent) {
           ConnectivityManager connMgr =
                   (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
           NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

           // Checks the network connection. Based on the result, decides
           // whether to refresh the display or keep the current display.
           // Check to see if the device has a Wi-Fi connection.
           if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
               // If device has its Wi-Fi connection, sets refreshDisplay
               // to true. This causes the display to be refreshed when the user
               // returns to the app.
               //refreshDisplay = true;
               //Toast.makeText(context, R.string.wifi_connected, Toast.LENGTH_SHORT).show();
        	   wifiConnected = true;

               // If the setting is ANY network and there is a network connection
               // (which by process of elimination would be mobile), sets refreshDisplay to true.
           } else if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
               //refreshDisplay = true;
        	   ethernetConnected = true;

               // Otherwise, the app can't download content--either because there is no network
               // connection (mobile or Wi-Fi), or because the pref setting is WIFI, and there
               // is no Wi-Fi connection.
               // Sets refreshDisplay to false.
           } else {
               //refreshDisplay = false;
               //Toast.makeText(context, R.string.lost_connection, Toast.LENGTH_SHORT).show();
           }
       }
   }
}
