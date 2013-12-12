package tw.com.charder.cusp_operator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHandler extends SQLiteOpenHelper {
	
	 // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "transactionManager";
 
    // Contacts table name
    private static final String TABLE_TRANS = "transactions";
 
    // Transactions Table Columns names
    private static final String TRANSAC_NUM = "number";
    private static final String TRANSAC_ID = "pluID";
    private static final String TRANSAC_NAME = "pluName";
    private static final String TRANSAC_WEIGHT = "weight_sold";
    private static final String TRANSAC_UNIT_PRICE = "unit_price";
    private static final String TRANSAC_TOTAL_PRICE = "total_price";
    private static final String TRANSAC_TIME_STAMP = "time_stamp";
   
			
	public DataBaseHandler(Context context, String name, CursorFactory factory,
			int version, DatabaseErrorHandler errorHandler) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, errorHandler);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		/* Table with column entries: Tansaction#, Tarnsaction ID, Item Name, Weight, Unit Price, Total Price, Deal time stamp */
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_TRANS + "("
                + TRANSAC_NUM + " INTEGER PRIMARY KEY," + TRANSAC_ID + " INTEGER," + TRANSAC_NAME + " TEXT,"
                + TRANSAC_WEIGHT + " REAL," + TRANSAC_UNIT_PRICE + " REAL," + TRANSAC_TOTAL_PRICE + " REAL," + TRANSAC_TIME_STAMP + " TEXT" +")";
        db.execSQL(CREATE_CONTACTS_TABLE);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		 // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANS);
 
        // Create tables again
        onCreate(db);
	}
	
	// Adding a new transaction 
	public void addTransaction(Transaction transac) {
	    SQLiteDatabase myTransacDb = this.getWritableDatabase();
	 
	    ContentValues values = new ContentValues();
	    
	    values.put(TRANSAC_NUM, transac.getTransactionNum()); 
	    values.put(TRANSAC_ID, transac.getPluID()); // Product PLU ID
	    values.put(TRANSAC_NAME, transac.getPluName()); // Product Name
	    values.put(TRANSAC_WEIGHT, transac.getWeightSold()); // How much weight of the item is sold in this transaction?
	    values.put(TRANSAC_UNIT_PRICE, transac.getUnitPrice());
	    values.put(TRANSAC_TOTAL_PRICE, transac.getTotalPrice());
	    values.put(TRANSAC_TIME_STAMP, transac.getTimeStamp());
	 
	    // Inserting Row
	    myTransacDb.insert(TABLE_TRANS, null, values);
	    myTransacDb.close(); // Closing database connection
	}
	
    // Getting single transaction
	public Transaction getTransaction(int transNum) {
		SQLiteDatabase myTransacDb = this.getReadableDatabase();
 
		Cursor cursor = myTransacDb.query(TABLE_TRANS, new String[] { TRANSAC_ID,
				TRANSAC_NAME, TRANSAC_WEIGHT, TRANSAC_TOTAL_PRICE, TRANSAC_TIME_STAMP}, TRANSAC_NUM + "=?",
            new String[] { String.valueOf(transNum) }, null, null, null, null);
		
		if (cursor != null)
			cursor.moveToFirst();
 
		Transaction query_result = new Transaction(Integer.parseInt(cursor.getString(0)),
				cursor.getString(1), Double.parseDouble(cursor.getString(2)), Float.parseFloat(cursor.getString(3)), cursor.getString(4));
		
		return query_result;
	}

}
