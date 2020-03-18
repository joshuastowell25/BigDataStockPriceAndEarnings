package com.stowellperformance.bdspae;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.stowellperformance.bdspae.domain.Filing;

public class ReportDownload 
{
	private static String formIndexUrl = "https://www.sec.gov/Archives/edgar/full-index/year/QTRquarter/form.idx";
	private static String secDotGovPrefix = "https://www.sec.gov";
	private static String indexFilingLocation = "."+File.separator+"IndexFiles";
	private static String instanceFilingLocation = "."+File.separator+"EarningsReports";
	private static ArrayList<File> formIndices = new ArrayList<File>();
	private static ArrayList<Filing> _10qFilings = new ArrayList<Filing>();
	private static int DESC = 1;
	private static int DOCNAME = 2;
	private static int TYPE = 3;
	private static int SIZE = 4;
	
	public static void main(String[] args) {
		downloadFormIndices(); //1993 q-1 to 2020 q-1
		System.out.println("Got Indices. Getting Filings. Please Wait...");
		get10qFilings();
		System.out.println("There are this many 10-q filings: "+_10qFilings.size()+". Grabbing instance files. Please Wait...");
		grabInstanceFiles();
		System.out.println("Done.");
		
		/*
		//https://www.sec.gov/Archives/edgar/data/18366/0000018366-94-000007-index.html
		grabInstanceFile(new Filing("10-Q        CBS INC                                                       18366       1994-05-02  edgar/data/18366/0000018366-94-000007.txt           "));
		
		//https://www.sec.gov/Archives/edgar/data/66740/0001558370-19-006397-index.html
		grabInstanceFile(new Filing("10-Q        3M CO                                                         66740       2019-07-26  edgar/data/66740/0001558370-19-006397.txt           "));	
		*/
	}
	
	public static void grabInstanceFiles() {
		for(int i = 0; i< _10qFilings.size(); i++) {
			Filing f = _10qFilings.get(i);
			System.out.println((i+1)+". "+f.toString());
			grabInstanceFile(f);
		}
	}
	
	public static void print10qFilings() {
		for(Filing f : _10qFilings) {
			System.out.println(f.toString());
		}
	}
	
	/*
	 * Grab the lines pertaining the 10Q from the index file
	 */
	public static ArrayList<Filing> get10qFilings(){
		FileReader fileReader = null;
		BufferedReader reader = null;
		
		for(int i = 0; i<formIndices.size(); i++) {
			try {
				fileReader = new FileReader(formIndices.get(i));
				reader = new BufferedReader(fileReader);
				
				String line = reader.readLine();
				while(line != null) {
					
					String[] tokens = line.trim().split("\\s+");
					if(tokens[0].equals("10-Q")) {
						_10qFilings.add(new Filing(line));
					}
					line = reader.readLine();
				}
					
				reader.close();
				fileReader.close();
			}catch(Exception e) {
				e.printStackTrace();
				//System.out.println(e.getMessage());
				try {
					reader.close();
				}catch(Exception e1) {
					
				}
				
				try {
					fileReader.close();
				}catch(Exception e2) {
					
				}
			}
		}
		return _10qFilings;
	}

	/*
	 * Loop through 4 quarters for each year since 1993 and download the form index
	 */
	public static void downloadFormIndices() {
		formIndices.clear();
		for(int i = 1993; i< 2020; i++) {
			for(int j = 1; j< 5; j++) {
				File formIndex = grabFormIndex(i, j);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				formIndices.add(formIndex);
			}
		}
		grabFormIndex(2020, 1);
	}
	
	/*
	 * Download a single quarter's form index
	 */
	public static File grabFormIndex(int year, int quarter) {
		System.out.println("Getting "+year+"_Q"+quarter+".txt");
		File result = new File(indexFilingLocation+File.separator+"temp_"+year+"_Q"+quarter+".txt");
		try {
			String urlString = formIndexUrl.replaceAll("year", ""+year).replaceAll("quarter",""+quarter);
			URL url = new URL(urlString);
			if(!result.exists()) { 
				FileUtils.copyURLToFile(url, result);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static File grabInstanceFile(Filing f) {
		//f = new Filing("10-Q        3M CO                                                         66740       2019-07-26  edgar/data/66740/0001558370-19-006397.txt           ");
		File result = null;		
		String indexUrl = f.getIndexUrl();
		
		try {
			//https://www.sec.gov/Archives/edgar/data/66740/0001558370-19-006397-index.html
			URL url = new URL(indexUrl);
			Document doc = Jsoup.parse(url, 3000);
			Elements dataFileTable = doc.select("table[summary=Data Files]");			
			String reportUrl = null;
			
			if(dataFileTable.size() == 0) {
				Elements documentTable = doc.select("table[summary=Document Format Files]");				
				reportUrl = grabInstanceFileUrlFromDocumentTable(documentTable);
			}else {
				//grab the file from the row where type = "EX-101.INS"
				reportUrl = grabInstanceFileUrlFromDataFileTable(dataFileTable);
			}
			
			if(reportUrl != null) {
				try {
					String tokens[] = reportUrl.split("\\.");
					String extension = tokens[tokens.length - 1];
					result = new File(instanceFilingLocation + File.separator + f.getCikNumber()+"_"+f.getDateString()+"."+extension);
					if(!result.exists()) { 
						FileUtils.copyURLToFile(new URL(reportUrl), result);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else {
				System.out.println("Unable to get filing for: "+indexUrl);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public static String grabInstanceFileUrlFromDocumentTable(Elements table) {
		//first try getting the file from row where type = "10-Q"
		//else try getting the file from the row where type = "EX-101.INS"
		//else grab the file from the row where description = "complete submission text file"
		String result = null;
		int rowIndex = 0;
		Elements rows = table.select("tr");
		
		if(rowIndex == 0) {
			for(Element row : rows) {
				Elements tds = row.select("td");
				if(tds.size() >= (DESC+1) && tds.get(DESC).text().toLowerCase().contains("complete submission text file")) {
					rowIndex = rows.indexOf(row);
					System.out.println("Grabbing from Document table: desc=complete submission text file");
					break;
				}
			}
		}
		
		if(rowIndex == 0)
		{
			for(Element row : rows) {
				Elements tds = row.select("td");
				if(tds.size() >= (TYPE+1) && tds.get(TYPE).text().toLowerCase().equals("10-q")) {
					rowIndex = rows.indexOf(row);
					System.out.println("Grabbing from Document table: type=10-q");
					break;
				}
			}
		}
		
		
		
		if(rowIndex == 0) {
			for(Element row : rows) {
				Elements tds = row.select("td");
				if(tds.size() >= (TYPE+1) && tds.get(TYPE).text().toLowerCase().equals("ex-101.ins")) {
					rowIndex = rows.indexOf(row);
					System.out.println("Grabbing from Document table: type=ex-101.inc");
					break;
				}
			}
		}
		
		if(rowIndex == 0) {
			System.out.println("couldn't get instance file url");
			return null;
		}else {
			try {
				String filingUrl = secDotGovPrefix + rows.get(rowIndex).selectFirst("a[href]").attr("href");
				System.out.println("Instance file url: "+filingUrl);
				result = filingUrl;
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	public static String grabInstanceFileUrlFromDataFileTable(Elements table) {
		String result = null;
		int rowIndex = 0;
		Elements rows = table.select("tr");
		
		if(rowIndex == 0) {
			for(Element row : rows) {
				Elements tds = row.select("td");
				if(tds.size() >= (TYPE+1) && tds.get(TYPE).text().toLowerCase().equals("ex-101.ins")) {
					rowIndex = rows.indexOf(row);
					System.out.println("Grabbing from Data Files table: type=ex-101.inc");
					break;
				}
			}
		}
		
		if(rowIndex == 0) {
			for(Element row : rows) {
				Elements tds = row.select("td");
				if(tds.size() >= (DESC+1)) {
					String val = tds.get(DESC).text().toLowerCase();
					if(val.contains("instance document")) {	
						rowIndex = rows.indexOf(row);
						System.out.println("Grabbing from Data Files table: desc=instance document");
						break;
					}
				}
			}
		}
		
		if(rowIndex == 0) {
			System.out.println("couldn't get instance file url");
			return null;
		}else {
			try {
				String filingUrl = secDotGovPrefix + rows.get(rowIndex).selectFirst("a[href]").attr("href");
				System.out.println("Instance file url: "+filingUrl);
				result = filingUrl;
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
}
