package com.stowellperformance.bdspae;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import com.stowellperformance.bdspae.domain.Company;
import com.stowellperformance.bdspae.toolkit.Props;
import com.stowellperformance.bdspae.tools.ResourceGrabber;

public class CompanyDownload {

	public static void main(String[] args) {
		ArrayList<Company> companies = getCompanies();
		postCompaniesToCouchDB(companies);
		downloadCompanyHistoricalData(companies);
	}
	
	/**
	 * Gets the companies from the src/main/resources/cik_ticker.csv file
	 * @return
	 */
	public static ArrayList<Company> getCompanies() {
		ArrayList<Company> companies = new ArrayList();
		
		ResourceGrabber r1 = new ResourceGrabber();
    	InputStream ir1 = r1.getResource("cik_ticker.csv");
    	InputStreamReader isr1 = new InputStreamReader(ir1);
    	BufferedReader b1 = new BufferedReader(isr1);
    	
    	try {	
			String line = b1.readLine(); 
			line = b1.readLine();
		    //CIK|Ticker|Name|Exchange|SIC|Business|Incorporated|IRS
		    String cik;
		    String ticker;
		    String name;
		    String exchange;
		    String sic;
		    String business;
		    String incorporated;
		    String irs;
		    int index = 0;
		    
			while(line != null) {
				index++;
				String[] tokens = line.split(",");
				while(tokens.length < 8) {
					line = line + " ,";
					tokens = line.split(",");
				}
				tokens = line.split(",");
				try {
					cik = tokens[0];
					ticker = tokens[1];
					name = tokens[2];
					exchange = tokens[3];
					sic = tokens[4];
					business = tokens[5];
					incorporated = tokens[6];
					irs = tokens[7];
					
					Company c = new Company(cik, ticker, name, exchange, sic, business, incorporated, irs);
					companies.add(c);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(line);
				}
				
				line = b1.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return companies;
	}
	
	/**
	 * POSTs the given companies from the src/main/resources/cik_ticker.csv file into CouchDB
	 */
	public static void postCompaniesToCouchDB(ArrayList<Company> companies) {
		for(Company c : companies) {
			postCompanyToCouchDB(c);
		}
	}
	
	/**
	 * Downloads the Max limit of daily historical data for the given companies
	 */
	public static void downloadCompanyHistoricalData(ArrayList<Company> companies) {
		int count = 0;
		
		for(Company c : companies) {
			if(!c.getTicker().equals(null) && !c.getTicker().equals("")) {
				System.out.println((count++)+". Gathering data for "+c.getTicker());
				HistoryDownload.getAndSaveData(c.getTicker());
			}
			postCompanyToCouchDB(c);
		}
	}

	/**
	 * POSTs a Company object to the company database in the CouchDB pointed at by src/main/resources/credentials.properties
	 * @param c The company object to POST
	 */
	public static void postCompanyToCouchDB(Company c) {
		
		HttpPut request;
		try {
	        DefaultHttpClient httpClient = new DefaultHttpClient();
	        //db.url format: http://user:pw@address:port/database
			request = new HttpPut(new URI(Props.getProperty("credentials.properties", "db.url")+"/company/"+c.getCik()));
	        StringEntity stringEntity = null;
	        String jsonString = null;
	        try {
	        	JSONObject j = new JSONObject();
	        	j.put("_id", c.getCik());
	        	j.put("cik", c.getCik());
	        	j.put("ticker", c.getTicker());
	        	j.put("name", c.getName());
	        	j.put("exchange", c.getExchange());
	        	j.put("sic", c.getSic());
	        	j.put("business", c.getBusiness());
	        	j.put("incorporated", c.getIncorporated());
	        	j.put("irs", c.getIrs());
	        	jsonString = j.toString();
	            stringEntity = new StringEntity(jsonString);
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        }
	        stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	        request.setHeader("Content-type", "application/json");
	        request.setEntity(stringEntity);		        
	        System.out.println("Posting: "+jsonString);
	        CloseableHttpResponse response = httpClient.execute(request);
	        StatusLine status = response.getStatusLine();
	        int statusCode = status.getStatusCode();
	        String msg = status.getReasonPhrase();
	        if(statusCode != 201) {
	        	System.out.println(statusCode+": "+msg);
	        }
	        
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
