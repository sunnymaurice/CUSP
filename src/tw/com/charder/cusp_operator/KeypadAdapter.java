package tw.com.charder.cusp_operator;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;

public class KeypadAdapter extends BaseAdapter{

	private Context mContext;
	
	// Declare button click listener variable
	private OnClickListener mOnButtonClick;
	
	public KeypadAdapter(Context c) {
		mContext = c;
	}

	// Method to set button click listener variable
	public void setOnButtonClickListener(OnClickListener listener) {
		mOnButtonClick = listener;
	}

	public int getCount() {
		return mButtons.length;
	}

	public Object getItem(int position) {
		return mButtons[position];
	}

	public long getItemId(int position) {
		return 0;
	}

	// create a new ButtonView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		Button btn;
		if (convertView == null) { // if it's not recycled, initialize some
									// attributes

			btn = new Button(mContext);			
			KeypadButton keypadButton = mButtons[position];
			
			
			switch(keypadButton.mCategory)
			{			
				case CLEAR:
					btn.setBackgroundResource(R.drawable.keypadclear);
					break;				
				case NUMBER:
					btn.setBackgroundResource(R.drawable.keypadnum);
					break;
				case OPERATOR:				
					btn.setBackgroundResource(R.drawable.keypadop);
					break;
				case SCALE_FUNC:
					btn.setBackgroundResource(R.drawable.keypadfunc);
					break;
				case RESULT:
					btn.setBackgroundResource(R.drawable.keypadresult);
					break;
				case DUMMY:
					btn.setBackgroundResource(R.drawable.keypaddummy);
					break;
				default:
					btn.setBackgroundResource(R.drawable.keypaddummy);
					break;
			}
		
			// Set OnClickListener of the button to mOnButtonClick
			if(keypadButton != KeypadButton.DUMMY)
				btn.setOnClickListener(mOnButtonClick);
			else
				btn.setClickable(false);
			// Set CalculatorButton enumeration as tag of the button so that we
			// will use this information from our main view to identify what to do
			btn.setTag(keypadButton);
		} 
		else {
			btn = (Button) convertView;
		}

		btn.setText(mButtons[position].getText());
		return btn;
	}

	// Create and populate keypad buttons array with KeypadButton enum values
	private KeypadButton[] mButtons = { 			
			KeypadButton.SEVEN,KeypadButton.EIGHT, KeypadButton.NINE, KeypadButton.CLEAR,			
			KeypadButton.FOUR, KeypadButton.FIVE,KeypadButton.SIX, KeypadButton.TARE, 			
			KeypadButton.ONE, KeypadButton.TWO, KeypadButton.THREE,KeypadButton.RST_ZERO, 
			KeypadButton.ZERO, KeypadButton.CALCULATE, KeypadButton.DECIMAL_SEP, KeypadButton.MULTIPLY};

}
