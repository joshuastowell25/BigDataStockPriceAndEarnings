package com.stowellperformance.bdspae.domain;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.stowellperformance.bdspae.toolkit.Props;

public class Report10Q {
	private int cik;
	private int month;
	private int day;
	private int year;
	private Float eps;
	private String reportType = "10-Q";
	
	public Report10Q(File reportXmlFile) {
		String epsForThisReport = null;
		String filename = reportXmlFile.getName();
		String startDateString = null;
		String endDateString = null;
		
		String[] tokens = filename.split("_");
		this.cik = Integer.parseInt(tokens[0]);
		
		String fileDateString = tokens[1].replaceAll(".xml", "");
		DateFormat dateStringFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date fileDate = null;
		try {
			fileDate = dateStringFormat.parse(fileDateString);
			String[] datetokens = fileDateString.split("-");
			this.year = Integer.parseInt(datetokens[0]);
			this.month = Integer.parseInt(datetokens[1]);
			this.day = Integer.parseInt(datetokens[2]);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
				
		try {
			Document d = Jsoup.parse(reportXmlFile, null);
			Elements epsElements = d.getElementsByTag("us-gaap:EarningsPerShareBasic");
			
			if(epsElements.isEmpty()) {
				epsElements = d.getElementsByTag("us-gaap:EarningsPerShareBasicAndDiluted");
			}
			
			Element pertinentEpsElement = null;
			
			//grab the contextref attributes off the elements
			for(Element epsElement : epsElements) {
				String contextref = epsElement.attr("contextref");
				//String contextref = "from-2014-02-03-to-2014-08-03*1*0*0*0*0*0*0*0";
				if(contextref != null) {
					Elements contexts = d.select("#"+contextref); //https://stackoverflow.com/questions/13559600/jsoup-selectorparseexception-when-colon-in-xml-tag
					Element context = contexts.get(0);
					
					Elements periods = context.getElementsByTag("xbrli:period");
					Element period = null;
					if(periods.size() == 0)
					{
						periods = context.getElementsByTag("period");
					}
					if(periods.size() != 0) {
						period = periods.get(0);
					}
					
					if(period != null) {
						Elements startDates = period.getElementsByTag("xbrli:startDate");
						if(startDates.size() == 0) {
							startDates = period.getElementsByTag("startDate");
						}
						
						if(startDates.size() != 0) {
							Element startDate = startDates.get(0);
							startDateString = startDate.text();	
						}
						
						Elements endDates = period.getElementsByTag("xbrli:endDate");
						if(endDates.size() == 0) {
							endDates = period.getElementsByTag("endDate");
						}
								
						if(endDates.size() != 0) {		
							Element endDate = endDates.get(0);
							endDateString = endDate.text();
						}
						
						
						Date startDate = null;
						try {
							startDate = dateStringFormat.parse(startDateString);
						}catch(Exception e1) {
							e1.printStackTrace();
						}
						
						Date endDate = null;
						try {
							endDate = dateStringFormat.parse(endDateString);
						}catch(Exception e1) {
							e1.printStackTrace();
						}
						
						DateRange dr = new DateRange(startDate, endDate);
						
						long days = dr.dayCount();
						long daysAfter = dr.daysAfter(fileDate); // companies are supposed to (don't often dont) file either 40 or 45 days after the quarter
						
						//11/09/2018
						if(dr.isValid() && days < 100 && days > 80 && daysAfter >= 0 && daysAfter < 50) {
							pertinentEpsElement = epsElement;
							break;
						}		
					}else {
						System.out.println("NO PERIODS");
					}
				}
			}
			
			if(pertinentEpsElement != null) {
				epsForThisReport = pertinentEpsElement.text();
				this.eps = Float.parseFloat(epsForThisReport);
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public String toString() {
		return "Report10Q [cik=" + cik + ", month=" + month + ", day=" + day + ", year=" + year + ", eps=" + eps
				+ ", reportType=" + reportType + "]";
	}

	public int getCik() {
		return cik;
	}

	public void setCik(int cik) {
		this.cik = cik;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public Float getEps() {
		return eps;
	}

	public void setEps(Float eps) {
		this.eps = eps;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	
	public JSONObject toJSON() {
		JSONObject jo = new JSONObject();
		jo.accumulate("cik", this.cik);
		jo.accumulate("year", this.year);
		jo.accumulate("month", this.month);
		jo.accumulate("day", this.day);
		jo.accumulate("type", this.reportType);
		
		if(this.eps != null)
			jo.accumulate("eps", this.eps);		
		
		return jo;
	}
	
	public int saveToDB() {
		int statusCode = 0;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String id = this.cik + "_"+this.year+"-"+this.month+"-"+this.day;
        try {
	        HttpPut request = new HttpPut(new URI(Props.getProperty("credentials.properties", "db.url")+"/reports/"+id));
	        StringEntity stringEntity = null;
	        stringEntity = new StringEntity(this.toJSON().toString());
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
        }catch(Exception e) {
        	e.printStackTrace();
        }
        return statusCode;
	}
}
