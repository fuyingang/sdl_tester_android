package com.livio.sdl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.livio.sdl.utils.SdlUtils;
import com.smartdevicelink.proxy.RPCMessage;
import com.smartdevicelink.proxy.RPCRequest;
import com.smartdevicelink.proxy.RPCResponse;

/**
 * The SdlLogMessage class holds applicable data to be used for logging an RPC message so that
 * we don't have to keep a strong reference to the large RPC message object sitting around just
 * to show a log message for it.
 *
 * @author Mike Burke
 *
 */
public class SdlLogMessage {
	
	public static final String REQUEST = "request";
	public static final String RESPONSE = "response";
	public static final String NOTIFICATION = "notification";
	
	private static final String DATE_FORMAT = "hh:mm:ss";
	private static final String POSITIVE_RESPONSE = "(positive response)";
	private static final String NEGATIVE_RESPONSE = "(negative response)";
	private static final String POSITIVE_REQUEST = "(request)";
	private static final String POSITIVE_NOTIFICATION = "(notification)";
	
	private String timeStamp;
	private String details;
	private String functionName;
	private boolean success = true;
	private String messageType;
	private String jsonData;
	private int correlationId = -1;
	
	public SdlLogMessage(RPCMessage rpcm) {
		setFields(rpcm);
	}
	
	// read information from the input RPC Message and fills in the appropriate fields for this class.
	private void setFields(RPCMessage rpcm){
		// get what type of message this is - request, response or notification
		messageType = rpcm.getMessageType();
		
		// apply a timestamp
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
		timeStamp = sdf.format(new Date());
		
		details = "";
		
		if(messageType.equals(RPCMessage.KEY_RESPONSE)){
			// input message was a response, which is slightly more detailed than requests or notifications, so we'll have a separate method for responses.
			setResponseFields((RPCResponse) rpcm);
		}
		else if(messageType.equals(RPCMessage.KEY_REQUEST)){
			// requests need to append "(request)" to the input function name.
			functionName = new StringBuilder().append(rpcm.getFunctionName()).append(" ").append(POSITIVE_REQUEST).toString();
			correlationId = ((RPCRequest) rpcm).getCorrelationID();
		}
		else if(messageType.equals(RPCMessage.KEY_NOTIFICATION)){
			// notifications need to append "(notification)" to the input function name.
			functionName = new StringBuilder().append(rpcm.getFunctionName()).append(" ").append(POSITIVE_NOTIFICATION).toString();
		}

		// set the JSON string
		jsonData = SdlUtils.getJsonString(rpcm);
	}
	
	// set data for a response type message
	private void setResponseFields(RPCResponse response){
		success = response.getSuccess();
		correlationId = response.getCorrelationID();
		
		if(success){
			// if the response was successful, set the details to success and show the function name as a positive response
			details = response.getResultCode().name();
			functionName = new StringBuilder().append(response.getFunctionName()).append(" ").append(POSITIVE_RESPONSE).toString();
		}
		else{
			// if the response was unsuccessful, show a detailed explanation in details and set function name as a negative response
			functionName = new StringBuilder().append(response.getFunctionName()).append(" ").append(NEGATIVE_RESPONSE).toString();
			
			// if the response has extra info, we'll append it after the negative response.
			String info = response.getInfo();
			StringBuilder builder = new StringBuilder().append(response.getResultCode().name());
			if(info != null){
				builder.append(": ").append(info);
			}
			details = builder.toString();
		}
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public String getDetails() {
		return details;
	}

	public String getFunctionName() {
		return functionName;
	}
	
	public boolean getSuccess(){
		return success;
	}
	
	public String getMessageType(){
		return messageType;
	}
	
	public String getJsonData(){
		return jsonData;
	}
	
	public int getCorrelationId(){
		return correlationId;
	}
}
