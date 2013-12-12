package tw.com.charder.cusp_operator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public class PriceListUnittXMLParser {
	 private static final String nameSpace = null;
	 	 	 
	// This class represents a single product item in the XML feed.
	// Parsing the file in "/mnt/sdcard/PLU/product.xml"
	
	private ArrayList<HashMap<String, String>> pluItemsInfo = new ArrayList<HashMap<String, String>>();
	
	
	public ArrayList<HashMap<String, String>> getProductData()
	{
		return pluItemsInfo;
	}
			
		
	public void parse(InputStream input) throws  XmlPullParserException, IOException 
	{
	    try {
	    		XmlPullParser parser = Xml.newPullParser();
	            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	            parser.setInput(input, null);
	            parser.nextTag();
	            readProduct(parser);
	    } finally {
	            input.close();
	    }
	}
	    
	/*
	 *  Looks for elements tagged "item" as a starting point for every recursively processing
	 *  the product.xml 
	 */
	    
	private void readProduct(XmlPullParser parser) throws XmlPullParserException, IOException 
	{	        
		parser.require(XmlPullParser.START_TAG, nameSpace, "product");
	        
		while (parser.next() != XmlPullParser.END_TAG) 
		{
	        	
			if (parser.getEventType() != XmlPullParser.START_TAG) 
			{
				continue;
	        }
	      
	        String name = parser.getName();
	        // Starts by looking for the entry tag
	        if (name.equals(GlobalVariables.KEY_ITEM)) {
	            	readEntry(parser);	               	            	
	        } 
	        else 
	        {
	        	skip(parser);
	            
	        }
		}//end of while	        
	}
	   
	    /*
	     *  Parses the contents of an item and store it in "pluItemsInfo".
	     *  TODO: May need to modify the function to traverse the nodes recursively 
	     *  to get different attribute node's value in variant depth. 
	     */
	    private void readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
	    	HashMap<String, String> map = new HashMap<String, String>();
	    	
	        parser.require(XmlPullParser.START_TAG, nameSpace, GlobalVariables.KEY_ITEM);
	        String value = null;	              
	        
	        while (parser.next() != XmlPullParser.END_TAG) 
	        {
	            if (parser.getEventType() != XmlPullParser.START_TAG) {
	                continue;
	            }
	            String name = parser.getName();
	            if (name.equals(GlobalVariables.KEY_ID))
	            {
	            	//Log.e("Parser.readEntry", "parse ID block");
	                value = readValue(parser, name);
	                map.put(GlobalVariables.KEY_ID, value);	                
	            }	         
	            else if (name.equals(GlobalVariables.KEY_NAME))
	            {
	            	//Log.e("Parser.readEntry", "parse NAME block");
	            	value = readValue(parser, name);
	            	map.put(GlobalVariables.KEY_NAME, value);	            	
	            }
	            else if (name.equals(GlobalVariables.KEY_DESC))
	            {
	            	//Log.e("Parser.readEntry", "parse DESC block");
	            	value = readValue(parser, name);
	            	map.put(GlobalVariables.KEY_DESC, value);
	            }
	            else if (name.equals(GlobalVariables.KEY_UNIT_PRICE))
	            {
	            	//Log.e("Parser.readEntry", "parse UNIT_PRICE block");
	            	if(value == "")	            		
	            		value = "0";	            		
	          
	            	value = readValue(parser, name);
	            	map.put(GlobalVariables.KEY_UNIT_PRICE, value);
	            }
	            else if (name.equals(GlobalVariables.KEY_CAL_UNIT))
	            {	            	
	            	//Log.e("Parser.readEntry", "parse GlobalVariables.KEY_CAL_UNIT block");
	            	value = readValue(parser, name);
	            	map.put(GlobalVariables.KEY_CAL_UNIT, value);         	
	            }
	            else if (name.equals(GlobalVariables.KEY_CAL_COUNT))
	            {	            	
	            	//Log.e("Parser.readEntry", "parse GlobalVariables.KEY_CAL_COUNT block");
	            	value = readValue(parser, name);
	            	if(value == "")	
	            		value = "0";
	            			            	
	            	map.put(GlobalVariables.KEY_CAL_COUNT, value);         	
	            }	        
	            else if (name.equals(GlobalVariables.KEY_ORIGIN)) 
	            {
	                
	            	//Log.e("Parser.readEntry", "parse GlobalVariables.KEY_ORIGIN block");
	            	value = readValue(parser, name);
	            	map.put(GlobalVariables.KEY_ORIGIN, value);
	            } 
	            else if (name.equals(GlobalVariables.KEY_PICNAME)) 
	            {
	                
	            	//Log.e("Parser.readEntry", "parse GlobalVariables.KEY_PICNAME block");
	            	value = readValue(parser, name);
	            	map.put(GlobalVariables.KEY_PICNAME, value);
	            } 
	            else //To trace if any tag is missed... ^^
	            {
	            	Log.e("Parser.readEntry", "unwanted tag: ["+ name+"]");
	                skip(parser);
	            }
	        }//end of while()
	        pluItemsInfo.add(map);	        	       
	    }
	    	   	    	    
	    // Processes "name" tags in the feed.	  
	    private String readValue(XmlPullParser parser, String nameTag) throws IOException, XmlPullParserException 
	    {
	        parser.require(XmlPullParser.START_TAG, nameSpace, nameTag);
	        String valueStr = readText(parser);
	        parser.require(XmlPullParser.END_TAG, nameSpace, nameTag);
	        //Log.e("Parser.readValue", "nameTag["+nameTag+"] value="+valueStr);
	        return valueStr;
	    }	       

	    // For the tags title and summary, extracts their text values.
	    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
	        String result = "";
	        if (parser.next() == XmlPullParser.TEXT) {
	            result = parser.getText();
	            parser.nextTag();
	        }	        
	        return result;
	    }

	    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
	    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
	    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
	    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException 
	    {
	        if (parser.getEventType() != XmlPullParser.START_TAG) 
	        {
	            throw new IllegalStateException();
	        }
	        int depth = 1;
	        while (depth != 0) 
	        {
	        	//Log.e("Parser_skip", "xml detpth: "+parser.getDepth()+"and get name:"+parser.getName());
	            switch (parser.next()) 
	            {
	            	case XmlPullParser.END_TAG:
	                    depth--;
	                    break;
	            	case XmlPullParser.START_TAG:
	                    depth++;
	                    break;
	            }
	        }
	    }
}
