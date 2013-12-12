package tw.com.charder.cusp_operator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.SystemClock;
import android.util.Log;

public class TcpClient {
	
	private Socket socket;
	public String SERVER_IP;
	public int SERVER_PORT;
    // message to send to the server 
    private String mData; 
    
    // sends message received notifications 
    private DataReceived mDataListener = null; 
    
    // while this is true, the server will continue running 
    private boolean mRun = false; 
    // used to send messages 
    private PrintWriter mBufferOut; 
    // used to read messages from the server 
    private BufferedReader mBufferIn;
    
    //To parse the message received from NDP server
    //final String delim = "\\s+"; //equivalent to [ \\t\\n\\x0B\\f\\r]  
    
    private boolean DebugMode = false; 
  
    /** 
     * Constructor of the class. 
     */
    public TcpClient(String serverIp, int serverPort, DataReceived listener) {
    	SERVER_IP = serverIp;
    	SERVER_PORT = serverPort;
    	mDataListener = listener;
    	
    	if(DebugMode)
    		Log.e("Maurice", "Init with sever ip: "+serverIp+"port: "+ serverPort);
    } 
  
    /** 
     * Sends the message from client to the server 
     * 
     * @param message text from client 
     */
    public void sendMessage(String message) { 
    	
    	//Log.d("TcpClient", "going to send packet with content: ["+message+"]");
    	
        if (mBufferOut != null && !mBufferOut.checkError()) { 
            mBufferOut.println(message); 
            mBufferOut.flush(); 
        } 
    } 
  
    /** 
     * Close the connection and release the members 
     */
    public void stopClient() { 
  
        // send message that we are closing the connection, TBD... 
    	//sendMessage("Maurice: going to close the connection...");
  
        mRun = false; 
  
        if (mBufferOut != null) { 
            mBufferOut.flush(); 
            mBufferOut.close(); 
        } 
        mDataListener = null;   
        mBufferIn = null; 
        mBufferOut = null; 
        mData = null; 
    }    
  
    public void run() { 
  
    	InetAddress serverAddr;
        
    	/* 
    	 * Note: The system should keep the connection to NDP server by all means.
    	 * 		 It may not be complete to avoid missing connection with NDP server.
    	 * 		 Will check it out later.  
    	 */
        while(true)
        {	        	        	
        	try { 
        		//here you must put your computer's IP address. 
        		serverAddr = InetAddress.getByName(SERVER_IP); 
            
        		if(DebugMode)
        			Log.e("TCP Client", "C: Connecting to " + SERVER_IP); 
            
        		try {
        			//create a socket to make the connection with the server 
        			socket = new Socket(serverAddr, SERVER_PORT); 
        			socket.setKeepAlive(true);
        			//timeout after 5 seconds
        			//socket.setSoTimeout(5000);
        		}catch (UnknownHostException e) {
        			Log.e("TCP Client", "Create sock with UnknownHostException...");
        			e.printStackTrace();
        		}catch(IOException e){
        			Log.e("TCP Client", "Create sock with IOException...");
        			/*
        			 * Wait for 60 seconds to reconnect to the NDP server again.
        			 */
        			SystemClock.sleep(60000);
        			continue;
        		}
  
        		mRun = true; 
        		
        		try { 
        			int empty_counter = 1; //times of no receiving data from DNP server
  
        			//sends the message to the server 
        			mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true); 
  
        			//receives the message which the server sends back 
        			mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
                                   
        			//in this while the client listens for the messages sent by the server 
        			while (mRun) {         			
        				mData = mBufferIn.readLine(); 
  
        				if (mData != null ) {
                    	
        					if(DebugMode)
        						Log.e("TCP Client", "Received Message: [" + mData + "] length: "+mData.length());
                    	
        					if (mDataListener != null) 
        					{ 
        						/* To check if the first byte is a meaningful alphabet */
        						if(!Character.isLetter(mData.charAt(0)))
        						{
        							String newData = new String();
        							newData = mData.substring(1, mData.length());
        							//Log.e("TCP client", "Get intended data :"+newData);
        							mDataListener.messageReceived(newData); 
        						}
        						else
        						{        						
        							//call the method messageReceived from MyActivity class 
        							mDataListener.messageReceived(mData);
        						}
        					}                     	                                       	           	        	                    
        				}
        				else
        				{        					
        					/* 
        					 * After a minute (80Hz * 600 seconds) no data incoming, 
        					 * close the current socket and try to create a new connection.
        					 */ 
        					if(empty_counter % 48000 == 0)
        					{
        						Log.e("TCP Client", "close socket and going to reconnect NDP server...");
        						mRun = false;
        						socket.close();
        						break;
        					}
        					empty_counter++;
        				}        				
        			}//end of while(mRun) 
  
        			//Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mNDPdata + "'"); 
  
        		} catch (Exception e) { 
  
        			Log.e("TCP", "S: Error", e); 
  
        		} finally { 
        			//the socket must be closed. It is not possible to reconnect to this socket 
        			// 	after it is closed, which means a new socket instance has to be created. 
        			Log.e("TCP Client", "Close socket...");
        			socket.close(); 
        		}
        		
        	} catch (Exception e) {   
        		Log.e("TCP", "C: Error", e);   
        	}
        }//end of while(true)
  
    }//end of run 
    
    //Declare the interface. The method DataReceived(String message) must be implemented in the OperatorActivity 
    //class at on asynckTask doInBackground 
    public interface DataReceived { 
        public void messageReceived(String message); 
    } 
}
