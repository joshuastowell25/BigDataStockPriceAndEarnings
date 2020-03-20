package com.stowellperformance.bdspae;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.stowellperformance.bdspae.HistoryDownload;
import com.stowellperformance.bdspae.domain.DataPoint;
import com.stowellperformance.bdspae.domain.DateRange;
import com.stowellperformance.bdspae.domain.Report10Q;
import com.stowellperformance.bdspae.toolkit.Props;
import com.stowellperformance.bdspae.tools.ResourceGrabber;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigorous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
    
    public void testGetResource() {
    	ResourceGrabber r1 = new ResourceGrabber();
    	InputStream i1 = r1.getResource("cik-lookup-data.txt");
    	BufferedReader b1 = new BufferedReader(new InputStreamReader(i1));
    	
    	try {
			String line1 = b1.readLine();
			System.out.println(line1);
			assertNotNull(line1);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
    }
    
    /**
     * http://typinggenius.com:5984/reports
     * http://typinggenius.com:5984/companies
     * http://typinggenius.com:5984/cik123456
     */
    public void testPut() {
    	HttpPut request;
		try {
			//HttpClient httpClient = wrapClient(host);
	        DefaultHttpClient httpClient = new DefaultHttpClient();

			request = new HttpPut(new URI("http://josh:1FastS10@typinggenius.com:5984/companies/320193"));
	        StringEntity stringEntity = null;
	        try {
	            stringEntity = new StringEntity("{\"_id\":\"0000320193\", \"name\":\"APPLE INC.\"}");
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        }
	        stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	        request.setHeader("Content-type", "application/json");
	        request.setEntity(stringEntity);		        
	        CloseableHttpResponse response = httpClient.execute(request);
	        StatusLine status = response.getStatusLine();
	        int statusCode = status.getStatusCode();
	        String msg = status.getReasonPhrase();
	        System.out.println(msg);
	        assertEquals(200, statusCode);
		} catch (Exception e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}
    }
    
    public void testGetMaxData() {
    	String symbol = "AAPL";
    	ArrayList<DataPoint> dataPoints = HistoryDownload.getMaxData(symbol);
    }
    
    public void testGetAndSaveData() {
    	String symbol = "AAPL";
    	ArrayList<DataPoint> dataPoints = HistoryDownload.getMaxData(symbol);
    	
    	int statusCode = HistoryDownload.postDataPointsCouchDB(symbol, dataPoints);
    	if(statusCode == 201) {
    		System.out.println("Successfully POSTed datapoints to couchDB");
    	}else {
    		System.out.println("Unable to POST datapoints to couchDB, status: "+statusCode); //400 = bad request
    	}//409: conflict
    	assertEquals(201, statusCode);
    }
    
    public void testExtractDataPoints() {
    	ArrayList<DataPoint> dataPoints = HistoryDownload.extractDataPoints("{\r\n" + 
    			"   \"chart\":{\r\n" + 
    			"      \"result\":[\r\n" + 
    			"         {\r\n" + 
    			"            \"meta\":{\r\n" + 
    			"               \"currency\":\"USD\",\r\n" + 
    			"               \"symbol\":\"AAPL\",\r\n" + 
    			"               \"exchangeName\":\"NMS\",\r\n" + 
    			"               \"instrumentType\":\"EQUITY\",\r\n" + 
    			"               \"firstTradeDate\":345459600,\r\n" + 
    			"               \"regularMarketTime\":1583956801,\r\n" + 
    			"               \"gmtoffset\":-14400,\r\n" + 
    			"               \"timezone\":\"EDT\",\r\n" + 
    			"               \"exchangeTimezoneName\":\"America/New_York\",\r\n" + 
    			"               \"regularMarketPrice\":275.43,\r\n" + 
    			"               \"chartPreviousClose\":1.259,\r\n" + 
    			"               \"priceHint\":2,\r\n" + 
    			"               \"currentTradingPeriod\":{\r\n" + 
    			"                  \"pre\":{\r\n" + 
    			"                     \"timezone\":\"EDT\",\r\n" + 
    			"                     \"start\":1583913600,\r\n" + 
    			"                     \"end\":1583933400,\r\n" + 
    			"                     \"gmtoffset\":-14400\r\n" + 
    			"                  },\r\n" + 
    			"                  \"regular\":{\r\n" + 
    			"                     \"timezone\":\"EDT\",\r\n" + 
    			"                     \"start\":1583933400,\r\n" + 
    			"                     \"end\":1583956800,\r\n" + 
    			"                     \"gmtoffset\":-14400\r\n" + 
    			"                  },\r\n" + 
    			"                  \"post\":{\r\n" + 
    			"                     \"timezone\":\"EDT\",\r\n" + 
    			"                     \"start\":1583956800,\r\n" + 
    			"                     \"end\":1583971200,\r\n" + 
    			"                     \"gmtoffset\":-14400\r\n" + 
    			"                  }\r\n" + 
    			"               },\r\n" + 
    			"               \"dataGranularity\":\"1d\",\r\n" + 
    			"               \"range\":\"\",\r\n" + 
    			"               \"validRanges\":[\r\n" + 
    			"                  \"1d\",\r\n" + 
    			"                  \"5d\",\r\n" + 
    			"                  \"1mo\",\r\n" + 
    			"                  \"3mo\",\r\n" + 
    			"                  \"6mo\",\r\n" + 
    			"                  \"1y\",\r\n" + 
    			"                  \"2y\",\r\n" + 
    			"                  \"5y\",\r\n" + 
    			"                  \"10y\",\r\n" + 
    			"                  \"ytd\",\r\n" + 
    			"                  \"max\"\r\n" + 
    			"               ]\r\n" + 
    			"            },\r\n" + 
    			"            \"timestamp\":[\r\n" + 
    			"               631290600,\r\n" + 
    			"               631377000,\r\n" + 
    			"               631463400,\r\n" + 
    			"               631549800\r\n" + 
    			"            ],\r\n" + 
    			"            \"indicators\":{\r\n" + 
    			"               \"quote\":[\r\n" + 
    			"                  {\r\n" + 
    			"                     \"high\":[\r\n" + 
    			"                        1.3392857313156128,\r\n" + 
    			"                        1.3571428060531616,\r\n" + 
    			"                        1.3839285373687744,\r\n" + 
    			"                        1.3660714626312256\r\n" + 
    			"                     ],\r\n" + 
    			"                     \"volume\":[\r\n" + 
    			"                        45799600,\r\n" + 
    			"                        51998800,\r\n" + 
    			"                        55378400,\r\n" + 
    			"                        30828000\r\n" + 
    			"                     ],\r\n" + 
    			"                     \"close\":[\r\n" + 
    			"                        1.3303571939468384,\r\n" + 
    			"                        1.3392857313156128,\r\n" + 
    			"                        1.34375,\r\n" + 
    			"                        1.3482142686843872\r\n" + 
    			"                     ],\r\n" + 
    			"                     \"open\":[\r\n" + 
    			"                        1.2589285373687744,\r\n" + 
    			"                        1.3571428060531616,\r\n" + 
    			"                        1.3660714626312256,\r\n" + 
    			"                        1.3482142686843872\r\n" + 
    			"                     ],\r\n" + 
    			"                     \"low\":[\r\n" + 
    			"                        1.25,\r\n" + 
    			"                        1.3392857313156128,\r\n" + 
    			"                        1.3303571939468384,\r\n" + 
    			"                        1.3214285373687744\r\n" + 
    			"                     ]\r\n" + 
    			"                  }\r\n" + 
    			"               ],\r\n" + 
    			"               \"adjclose\":[\r\n" + 
    			"                  {\r\n" + 
    			"                     \"adjclose\":[\r\n" + 
    			"                        1.0788694620132446,\r\n" + 
    			"                        1.0861105918884277,\r\n" + 
    			"                        1.0897300243377686,\r\n" + 
    			"                        1.0933504104614258\r\n" + 
    			"                     ]\r\n" + 
    			"                  }\r\n" + 
    			"               ]\r\n" + 
    			"            }\r\n" + 
    			"         }\r\n" + 
    			"      ],\r\n" + 
    			"      \"error\":null\r\n" + 
    			"   }\r\n" + 
    			"}");
    	for(DataPoint p : dataPoints) {
    		p.print();
    	}
    }
    
    public void testDataPointToJSON() {
    	String epochSeconds = "1584210374";
    	DataPoint p = new DataPoint(epochSeconds,"EDT","1100","1200","900","1000","1000","1000000");
    	JSONObject j = p.toJSON();
    	String date = j.get("date").toString();
    	System.out.println("date: "+date+": "+j.toString());
    }
    
    public void testPostDataPointsCouchDB() {
    	ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();
    	dataPoints.add(new DataPoint("1584123974","EDT","1100","1200","900","1000","1000","1000000"));
    	dataPoints.add(new DataPoint("1584300969","EDT","1100","1200","900","1000","1000","1000000"));
    	int statusCode = HistoryDownload.postDataPointsCouchDB("MYTEST", dataPoints);
    	if(statusCode == 201) {
    		System.out.println("Successfully POSTed datapoints to couchDB");
    	}else {
    		System.out.println("Unable to POST datapoints to couchDB, status: "+statusCode); //400 = bad request
    	}//409: 
    	assertEquals(201, statusCode);
    }
    
    public void testProperties() {
    	try {
    		System.out.println("Working Directory = " +
    	              System.getProperty("user.dir"));
    		
			String prop = Props.getProperty("credentials.properties", "db.url");
			assertNotNull(prop);
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void testGetEarningsPerShare1() {
    	File f = new File(ReportDownload.reportFilingLocation + File.separator + "98677_2018-11-09.xml");
    	Report10Q r = new Report10Q(f);
		Float eps = r.getEps();
		System.out.println(f.getName()+": "+eps);
    }
    
    public void testGetEarningsPerShare2() {
    	File f = new File(ReportDownload.reportFilingLocation + File.separator + "872912_2011-11-09.xml");
    	Report10Q r = new Report10Q(f);
		Float eps = r.getEps();
		System.out.println(f.getName()+": "+eps);
    }
    
    //1001039_2010-02-09.xml
    public void testGetEarningsPerShare3() {
    	File f = new File(ReportDownload.reportFilingLocation + File.separator + "1001039_2010-02-09.xml");
    	Report10Q r = new Report10Q(f);
		Float eps = r.getEps();
		System.out.println(f.getName()+": "+eps);
    }
    
    //1089063_2011-11-23.xml
    public void testGetEarningsPerShare4() {
    	File f = new File(ReportDownload.reportFilingLocation + File.separator + "1089063_2011-11-23.xml");
    	Report10Q r = new Report10Q(f);
		Float eps = r.getEps();
		System.out.println(f.getName()+": "+eps);
    }        
    
    public void testGetEarnings() {
    	List<Integer> cikNos = ReportDownload.getRelevantCikNos();
    	ArrayList<Report10Q> reportsICareAbout = ReportDownload.getRelevantEarningsReports(cikNos);
    	for(Report10Q r : reportsICareAbout) {
    		Float eps = r.getEps();
    		System.out.println(eps);
    	}
    }
    
    public void testDateRange() {
    	SimpleDateFormat dateformat = new SimpleDateFormat("MM-dd-yyyy");
    	String strdate1 = "02-04-2013";
    	String strdate2 = "02-08-2013";

    	try {
			Date date1 = dateformat.parse(strdate1);
			Date date2 = dateformat.parse(strdate2);
			DateRange dr = new DateRange(date1, date2);
			long dayCount = dr.dayCount();
			assertEquals((long)4, dayCount);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void testSaveReport() {
    	File f = new File(ReportDownload.reportFilingLocation + File.separator + "1089063_2011-11-23.xml");
    	Report10Q r = new Report10Q(f);
    	assertNotNull(r);
    	System.out.println(r.toString());
    }
    
    public void testProblemReport() {
    	File f = new File(ReportDownload.reportFilingLocation + File.separator + "1100270_2014-09-12.xml");
    	Report10Q r = new Report10Q(f);
    }
}
