package tw.com.charder.cusp_operator;

import java.security.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Transaction {
	
	private static DateFormat sdf = new SimpleDateFormat("yyyy/mm/dd hh:mm:ss", Locale.US);
	private static DateFormat stn = new SimpleDateFormat("yyyymmddhhmmss", Locale.US);

	int tNumber;
	int tPluID;
	String tPluName;
	double tWeightSold;
	float tUnitPrice;
	float tTotalPrice;
	String tTimeStamp;

	public Transaction() {
		// TODO Auto-generated constructor stub
	}
	public Transaction(int id, String name, double weight, float unitP, float totalP, Timestamp ts)
	{
		this.tNumber = Integer.parseInt(stn.format(ts)); //use time stamp as unique transaction number, we can add device id as prefix later.
		this.tPluID = id;
		this.tPluName = name;
		this.tWeightSold = weight;
		this.tUnitPrice = unitP;
		this.tTotalPrice = totalP;
		this.tTimeStamp = sdf.format(ts);		
	}
	
	public Transaction(int id, String name, double weight, float totalP, String tStamp)
	{		
		this.tPluID = id;
		this.tPluName = name;
		this.tWeightSold = weight;		
		this.tTotalPrice = totalP;	
		this.tTimeStamp = tStamp;
	}
	
	public int getTransactionNum()
	{
		return this.tNumber;		
	}
	
	public int getPluID()
	{
		return this.tPluID;		
	}
	
	public void setPluID(int id)
	{
		this.tPluID = id;
	}
	
	public String getPluName()
	{
		return this.tPluName;		
	}
	
	public void setPluName(String name)
	{
		this.tPluName = name;
	}
	
	public double getWeightSold()
	{
		return this.tWeightSold;		
	}
	
	public void setWeightSold(double weight)
	{
		this.tWeightSold = weight;
	}

	public float getUnitPrice()
	{
		return this.tUnitPrice;		
	}
	
	public void setUnitPrice(float uPrice)
	{
		this.tUnitPrice = uPrice;
	}
	
	public float getTotalPrice()
	{
		return this.tTotalPrice;		
	}
	
	public void setTotalPrice(float tPrice)
	{
		this.tTotalPrice = tPrice;
	}
	
	public String getTimeStamp()
	{
		return this.tTimeStamp;		
	}
	
	public void setTimeStamp(Timestamp ts)
	{
		this.tTimeStamp = sdf.format(ts);
	}
}
