package com.stowellperformance.bdspae;

import java.net.URI;
import java.util.ArrayList;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.stowellperformance.bdspae.domain.DataPoint;
import com.stowellperformance.bdspae.toolkit.Props;

public class HistoryDownload {
	public static String getData(long startTime, long endTime, String symbol) {
		String result = null;
    	OkHttpClient client = new OkHttpClient();

    	System.out.println("Requesting data for starttime: "+startTime+" to endtime: "+endTime);
    	try {
    		String apiKey = Props.getProperty("credentials.properties", "api.key");
    		Request request = new Request.Builder()
    		.url("https://apidojo-yahoo-finance-v1.p.rapidapi.com/stock/get-histories?region=US&lang=en&symbol="+symbol+"&from="+startTime+"&to="+endTime+"&events=div&interval=1d")
    		.get()
    		.addHeader("x-rapidapi-host", "apidojo-yahoo-finance-v1.p.rapidapi.com")
    		.addHeader("x-rapidapi-key", apiKey)
    		.build();

			Response response = client.newCall(request).execute();
			ResponseBody rb = response.body();
			result = rb.string();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return result;
	}
	
	public static ArrayList<DataPoint> getMaxData(String symbol){
		long currentSeconds = System.currentTimeMillis() / 1000;
		String dataJSON = getData(1,currentSeconds, symbol);
		ArrayList<DataPoint> dataPoints = extractDataPoints(dataJSON);
		return dataPoints;
	}
	
	public static int postDataPointsCouchDB(String symbol, ArrayList<DataPoint> dataPoints) {
		HttpPut request;
		int statusCode = 0;
		JSONArray ja = new JSONArray();
		try {
	        DefaultHttpClient httpClient = new DefaultHttpClient();
	        JSONArray data = new JSONArray();
	        for(DataPoint p : dataPoints) {
	        	data.put(p.toJSON());
	        }
	        
			request = new HttpPut(new URI(Props.getProperty("credentials.properties", "db.url")+"/data/"+symbol));
	        StringEntity stringEntity = null;
	        try {
	        	String objectToPlace = "{\"_id\":\""+symbol+"\", \"data\": "+data.toString()+"}";
	        	
	            stringEntity = new StringEntity(objectToPlace);
	        } catch (Exception e){
	        	String message = e.getMessage();
	            e.printStackTrace();
	        }
	        stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	        request.setHeader("Content-type", "application/json");
	        request.setEntity(stringEntity);		        
	        CloseableHttpResponse response = httpClient.execute(request);
	        StatusLine status = response.getStatusLine();
	        statusCode = status.getStatusCode();
	        String msg = status.getReasonPhrase();
	        if(statusCode != 201) {
	        	System.out.println("Unable to POST: "+msg);
	        }
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return statusCode;
	}
	
	/**
	 * Gets all of the daily close data for a given symbol and POSTs it into CouchDB
	 * @param symbol
	 * @return
	 */
	public static int getAndSaveData(String symbol) {
		int statusCode = 0;
		try {
			ArrayList<DataPoint> dataPoints = HistoryDownload.getMaxData(symbol);
			statusCode = HistoryDownload.postDataPointsCouchDB(symbol, dataPoints);
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage() +"\nUnable to get data for: "+symbol);
		}
    	return statusCode;
	}
	
	public static ArrayList<DataPoint> extractDataPoints(String apiJSON){
		ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();
		
		JSONObject obj = new JSONObject(apiJSON);
		JSONObject chart= obj.getJSONObject("chart");
		JSONArray result = chart.getJSONArray("result");
		JSONObject resultObj = (JSONObject)result.get(0);
		JSONObject meta = resultObj.getJSONObject("meta");
		String timezone = meta.getString("timezone");
		JSONArray timestamps = resultObj.getJSONArray("timestamp");
		JSONObject indicators = resultObj.getJSONObject("indicators");
		JSONObject quote = (JSONObject)indicators.getJSONArray("quote").get(0);
		JSONObject adjclose = (JSONObject)indicators.getJSONArray("adjclose").get(0);
		
		JSONArray adjcloses = adjclose.getJSONArray("adjclose");
		JSONArray opens = quote.getJSONArray("open");
		JSONArray highs = quote.getJSONArray("high");
		JSONArray lows = quote.getJSONArray("low");
		JSONArray closes = quote.getJSONArray("close");
		JSONArray volumes = quote.getJSONArray("volume");
		
		int count = timestamps.length();
		
		for(int i = 0; i< count; i++) {		
			try {
				DataPoint p = new DataPoint(
						timestamps.get(i).toString(),
						timezone,
						opens.get(i).toString(), 
						highs.get(i).toString(), 
						lows.get(i).toString(), 
						closes.get(i).toString(), 
						adjcloses.get(i).toString(), 
						volumes.get(i).toString());
				
				dataPoints.add(p);
			}catch(Exception e) {
				System.out.println("Error on HistoryDownload at ExtractDataPoints: "+i+" d");
				e.printStackTrace();
				int j = 0;
			}
		}
		
		return dataPoints;
	}
}