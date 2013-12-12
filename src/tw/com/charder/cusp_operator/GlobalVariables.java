package tw.com.charder.cusp_operator;

public class GlobalVariables {	
	//15 Kg fixed capacity.
	public static final int SCALE_CAPACITY = 15000; 	
	//300 g as the max range of total sum of all loads on scale <= 2% of scale's max weight capacity.
	public static final int GROSS_ZERO_MAX = 300;
	/*
	 * Define all the Global variables in this class so that we can maintain them 
	 * in this class without duplications in multiple class files. 		
	 */
	/* Formation of supported message from MPP server */
	public static final String PKT_SRC_OPERATOR= "OPU";
	public static final String PKT_DEST_OPERATOR= "OPU";
	public static final String PKT_DEST_CUSTOMER= "CUU";
	public static final String PKT_DEST_BROADCAST= "BST";
	public static final String PKT_DEST_NDPS= "NDP";
	public static final String PKT_DEST_MPPS= "MPP";
	public static final String PKT_SUFFIX = "****";
	public static final String PKT_DATA_DELIM = "&";
	public static final int PKT_SRC_OFFSET = 0;
	public static final int PKT_DEST_OFFSET = 3;	
	public static final int PKT_TYPE_OFFSET = 6;
	public static final int PKT_DATALEN_OFFSET = 8;
	public static final int PKT_DATA_OFFSET = 12;
	/* The list of legal packet type sent from MPP server. */
	public static final int PKT_TYPE_DEAL_INFO = 1;
	public static final int PKT_TYPE_UPDATE_XML = 11;
	public static final int PKT_TYPE_INSTANT_MSG = 32;
	
	/* COUNT mode */
	public static final int COUNT_BY_WEIGHT_MODE = 1;
	public static final int COUNT_BY_PACK_MODE = 2;
	public static final int COUNT_BY_SET_MODE = 3;
		
	/* product.xml key node's name */
	public static final String PLU_FILE_NAME = "product.xml";
	public static final String PLU_PIC_DIR_NAME = "pics";
	public static final String KEY_ITEM = "item"; 
	public static final String KEY_ID = "productID";
	public static final String KEY_NAME = "name";	
	public static final String KEY_DESC = "description";
	public static final String KEY_UNIT_PRICE = "unitPrice";	
	public static final String KEY_CAL_UNIT = "unit";
	public static final String KEY_CAL_COUNT = "count";
	public static final String KEY_ORIGIN = "origin";
	public static final String KEY_PICNAME = "picname";
	public static final String CAL_BY_WEIGHT = "Weight";
	public static final String CAL_BY_PACK = "Packed";
	public static final String CAL_BY_SET = "Set";
	
	/* For DataBaseHandler.java */
	/*
	public static final String TRANSAC_ID = "pluID";
    public static final String TRANSAC_NAME = "pluName";
    public static final String TRANSAC_WEIGHT = "weight_sold";
    public static final String TRANSAC_UNIT_PRICE = "unit_price";
    public static final String TRANSAC_TOTAL_PRICE = "total_price";
    public static final String TRANSAC_TIME_STAMP = "time_stamp";
    */
	
	/* For broadcast receiver action types */
	public static final String PLU_ACTION = "CUSP.OperatorActivity.PluReceive";
	public static final String XML_UPDATE_ACTION = "CUSP.OperatorActivity.UpdateXml";
	public static final String DO_UPDATE_PLU = "doUpdatePLU";
	public static final String SYS_NOTICE_ACTION = "CUSP.OperatorActivity.SysNoticfication";	
	public static final String SYS_MESSAGE_ACTION = "CUSP.OperatorActivity.SysMessage";
	public static final String SYS_ERROR_ACTION = "CUSP.OperatorActivity.SysError";
	
	/* Server port and IP address information */
	public static final String localServerIP = "127.0.0.1"; //ip address of NDP and MPP server
	public static final int ndpServerPort = 9020;			//port opened by NDP server
	public static final int mppServerPort = 9022;			//port opened by MPP server

	public GlobalVariables() {
		// TODO Auto-generated constructor stub
	}

}
