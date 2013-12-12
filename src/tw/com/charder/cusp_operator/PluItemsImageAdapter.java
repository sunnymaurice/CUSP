/**
 *  Description: 
 *  	Customize a gridview by creating an adapter that extends "BaseAdapter" for PLU items
 *  	displaying as image buttons.
 */

package tw.com.charder.cusp_operator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;

/**
 * @author: Maurice
 * @Date  : 2013.06.20
 * @ToDo  : Dynamically determine the elements of preset of PLU items via reading product.xml
 * 			from server.
 * @Revision: 
 * 			2013.08.27 Support displaying products with image buttons dynamically via reading product.xml
 * 			2013.09.18 TODO: Have to support a notification of product.xml updating event from MPP server.  
 */
public class PluItemsImageAdapter extends BaseAdapter {
	
	private Context myContext;
	
	/*
	private Integer[] myPluItems = {
		R.drawable.bannar, R.drawable.grape, R.drawable.kiwi, R.drawable.papaya,
		R.drawable.pineapple, R.drawable.hamimelon, R.drawable.grapefruit, R.drawable.orange,
		R.drawable.peach, R.drawable.strawberry, R.drawable.watermello, R.drawable.starfruit					
	};
	*/
	
	//private int btnClicked = 0; // To record which PLU button is pressed to get the correct product related information such as name/unit price of the selected item.
	//final static String OPERATOR_ACTION = "CUSP.OperatorActivity.ReceivePLU";
	private PriceListUnittXMLParser myParser = null;	
	private ArrayList<HashMap<String, String>> pluItems = new ArrayList<HashMap<String, String>>();		
	
	protected void readXmlFromSDcard() throws IOException
	{
		InputStream inStream = null;
		//PriceListUnittXMLParser myParser = null;
		
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED) ){ 
			try { 
			     //取得SD卡路徑 (/mnt/sdcard)
			     File SDCardpath = Environment.getExternalStorageDirectory();
			     Log.e("SystemMain", "SD card path: ["+SDCardpath.toString()+"]");
			     File myFile = new File( SDCardpath.getAbsolutePath() + "/" + GlobalVariables.PLU_FILE_NAME); 
			     inStream = new FileInputStream(myFile);			     			   
			     
			     myParser = new PriceListUnittXMLParser();
			     
			     myParser.parse(inStream);			     
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			finally{
				if(inStream != null)
				{
					inStream.close();
				}					
			}
		}
		else
			Log.e("SystemMain", "Cannot detect any external storage...");
		
		// To demo the data of each PLU item stored in the ArrayList
		/*
		for(int i=0; i < myParser.pluItemsInfo.size(); i++)
		{
			Log.e("SystemMainActivity", "Item: "+myParser.pluItemsInfo.get(i).get("name")+" price= "+ myParser.pluItemsInfo.get(i).get("unitPrice"));
		}
		*/
	}
	
	
	public PluItemsImageAdapter(Context c) throws IOException{
		myContext = c;
		try{
			readXmlFromSDcard();
		}catch(IOException e)
		{
			e.printStackTrace();
		}
		pluItems = myParser.getProductData();
		
		// To demo the data of each PLU item stored in the ArrayList
		/*
		for(int i=0; i < pluItems.size(); i++)
		{
			Log.e("PluItemsImageAdapter", "Item ("+pluItems.get(i).get("productID")+"): "+pluItems.get(i).get("name")+" unit price= "+ pluItems.get(i).get("unitPrice"));
		}
		*/
	}
	
	/*
	public ArrayList<HashMap<String, String>> getProductItems()
	{
		//pluItems = myParser.getProductData();
		return pluItems;
	}
	*/

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		//return 0;
		//return myPluItems.length;
		return pluItems.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub		
		//return null;
		//return myPluItems[position];
		return pluItems.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		//return 0;
		return arg0;
	}
	
	/*
	 *  Draw a picture from open the related file in external SD card.
	 */
	private void displayItemPic(int index, ImageButton imgButton)
	{
		BufferedInputStream buf = null;
		FileInputStream fInputStream = null;
		
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED) ){ 
			try { 			     
			     File SDCardpath = Environment.getExternalStorageDirectory();
			     //Log.e("displayItemPic", "open file: "+SDCardpath.getAbsolutePath() + "/pics/"+pluItems.get(index).get("picname"));
			     fInputStream = new FileInputStream(SDCardpath.getAbsolutePath() + "/"+GlobalVariables.PLU_PIC_DIR_NAME+"/"+pluItems.get(index).get(GlobalVariables.KEY_PICNAME));
			     
			     buf = new BufferedInputStream(fInputStream);
			     
			     Bitmap myBmap = BitmapFactory.decodeStream(buf);
			     
			     if(buf != null)
			    	 buf.close();
			     if(fInputStream != null)
			    	 fInputStream.close();
			     
			     imgButton.setImageBitmap(myBmap);			     			     
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		else
			Log.e("SystemMain", "Cannot detect any external storage...");
	}

	@Override
	public View getView(final int pos, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		//View gridView;
		ImageButton imgButton;
		
		if(convertView == null){
			imgButton = new ImageButton(myContext);
			imgButton.setLayoutParams(new GridView.LayoutParams(90, 90));
			imgButton.setScaleType(ImageButton.ScaleType.CENTER_CROP);
			imgButton.setPadding(8, 8, 8, 8);
		}
		else{
			imgButton = (ImageButton) convertView;
		}
				
		//imgButton.setImageResource(myPluItems[pos]);
		//Log.e("Maurice", "select product: "+pluItems.get(pos).get("name")+" and its count:"+pluItems.get(pos).get("count"));
		displayItemPic(pos, imgButton);
		
		
		imgButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View myView) {
				// TODO: Broadcast the unit price and the way to count the total price of this product
				Intent it = new Intent(GlobalVariables.PLU_ACTION);
				//Bundle bundle = new Bundle();
				//bundle.putDouble(key, value);
				it.putExtra(GlobalVariables.KEY_ID, pluItems.get(pos).get(GlobalVariables.KEY_ID));
				it.putExtra(GlobalVariables.KEY_NAME, pluItems.get(pos).get(GlobalVariables.KEY_NAME));
				it.putExtra(GlobalVariables.KEY_UNIT_PRICE, pluItems.get(pos).get(GlobalVariables.KEY_UNIT_PRICE));  
				it.putExtra(GlobalVariables.KEY_CAL_UNIT, pluItems.get(pos).get(GlobalVariables.KEY_CAL_UNIT));
				it.putExtra(GlobalVariables.KEY_CAL_COUNT, pluItems.get(pos).get(GlobalVariables.KEY_CAL_COUNT));
				/* 
				 * TODO:
				 * 	Extend these two types of information later in order that we can display them in Customer UI later. 
				 */
				it.putExtra(GlobalVariables.KEY_DESC, pluItems.get(pos).get(GlobalVariables.KEY_DESC));
				it.putExtra(GlobalVariables.KEY_ORIGIN, pluItems.get(pos).get(GlobalVariables.KEY_ORIGIN));
				myContext.sendBroadcast(it);
				
				//Toast.makeText(myView.getContext(), "Item postion: " + pos + "; ID:" + myPluItems[pos], Toast.LENGTH_SHORT).show();
				//Toast.makeText(myView.getContext(), "Item postion: " + pos, Toast.LENGTH_SHORT).show();
				
			}
		});
		
			
		//return null;
		return imgButton;
	}
}
