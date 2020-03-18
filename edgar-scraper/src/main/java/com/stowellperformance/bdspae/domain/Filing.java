package com.stowellperformance.bdspae.domain;

import java.util.GregorianCalendar;

public class Filing {
	private static String secUrlPrefix = "https://www.sec.gov/Archives/";

	private String formType;
	private String companyName;
	private int cikNumber;
	private String dateString;
	private GregorianCalendar filedDate;
	private String indexUrl; //edgar/data/768899/0000768899-19-000116.txt ->edgar/data/768899/0000768899-19-000116-index.html
	
	
	//10-Q        MINNESOTA MINING & MANUFACTURING CO                           66740       1993-08-13  edgar/data/66740/0000066740-94-000015.txt  
	//0-11        12-73                                                         74-85       86-97       98+
	public Filing(String indexFileLine) {
		this.formType = indexFileLine.substring(0, 11).trim();
		this.companyName = indexFileLine.substring(12, 73).trim();
		this.cikNumber = Integer.valueOf(indexFileLine.substring(74, 85).trim());
		this.dateString = indexFileLine.substring(86, 97).trim();
		String[] dateParts = indexFileLine.substring(86, 97).trim().split("-");
		this.filedDate = new GregorianCalendar(Integer.valueOf(dateParts[0]), Integer.valueOf(dateParts[1]), Integer.valueOf(dateParts[2]));
		this.indexUrl = secUrlPrefix+indexFileLine.substring(98).trim().replaceAll(".txt", "-index.html");
	}
	
	public String toString() {
		return this.indexUrl;
	}

	public String getFormType() {
		return formType;
	}

	public void setFormType(String formType) {
		this.formType = formType;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public int getCikNumber() {
		return cikNumber;
	}

	public void setCikNumber(int cikNumber) {
		this.cikNumber = cikNumber;
	}

	public String getDateString() {
		return dateString;
	}

	public void setDateString(String dateString) {
		this.dateString = dateString;
	}

	public GregorianCalendar getFiledDate() {
		return filedDate;
	}

	public void setFiledDate(GregorianCalendar filedDate) {
		this.filedDate = filedDate;
	}

	public String getIndexUrl() {
		return indexUrl;
	}

	public void setIndexUrl(String indexUrl) {
		this.indexUrl = indexUrl;
	}
	
	
}
