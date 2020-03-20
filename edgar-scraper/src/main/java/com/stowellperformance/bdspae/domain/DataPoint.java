package com.stowellperformance.bdspae.domain;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.json.JSONObject;

public class DataPoint {
	private static DateFormat dateStringFormat = new SimpleDateFormat("MMddyyyy");

    private String dateString;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Double adjClose;
    private Integer volume;

    
	public DataPoint(String epochSeconds, String timezone, String open, String high, String low, String close, String adjClose, String volume) {
		super();
		Date date = new Date(Long.parseLong(epochSeconds)*1000);
		dateStringFormat.setTimeZone(TimeZone.getTimeZone(timezone));
		this.dateString = dateStringFormat.format(date);
		
		try {
			this.open = Double.parseDouble(open);
		}catch(Exception e) {
			this.open = null;
		}
		try {
			this.high = Double.parseDouble(high);
		}catch(Exception e) {
			this.high = null;
		}
		try {
			this.low = Double.parseDouble(low);
		}catch(Exception e) {
			this.low = null;
		}
		try {
			this.close = Double.parseDouble(close);
		}catch(Exception e) {
			this.close = null;
		}
		try {
			this.adjClose = Double.parseDouble(adjClose);
		}catch(Exception e) {
			this.adjClose = null;
		}
		try {
			this.volume = Integer.parseInt(volume);
		}catch(Exception e) {
			this.volume = null;
		}
	}

	public Double getOpen() {
		return open;
	}

	public void setOpen(Double open) {
		this.open = open;
	}

	public Double getHigh() {
		return high;
	}

	public void setHigh(Double high) {
		this.high = high;
	}

	public Double getLow() {
		return low;
	}

	public void setLow(Double low) {
		this.low = low;
	}

	public Double getClose() {
		return close;
	}

	public void setClose(Double close) {
		this.close = close;
	}

	public Double getAdjClose() {
		return adjClose;
	}

	public void setAdjClose(Double adjClose) {
		this.adjClose = adjClose;
	}

	public Integer getVolume() {
		return volume;
	}

	public void setVolume(Integer volume) {
		this.volume = volume;
	}
    
	public void print() {
		System.out.println(this.dateString+","+this.open+","+this.high+","+this.low+","+this.close+","+this.adjClose+","+this.volume);
	}
	
	public JSONObject toJSON() {
		JSONObject jo = new JSONObject();
		jo.accumulate("year", Integer.parseInt(this.dateString.substring(4, 8)));
		jo.accumulate("month", Integer.parseInt(this.dateString.substring(0,2)));
		jo.accumulate("day", Integer.parseInt(this.dateString.substring(2,4)));
		
		if(this.open != null)
			jo.accumulate("open", this.open);
		if(this.high != null)
			jo.accumulate("high", this.high);
		if(this.low != null)
			jo.accumulate("low", this.low);
		if(this.close != null)
			jo.accumulate("close", this.close);
		if(this.adjClose != null)
			jo.accumulate("adjclose",this.adjClose);
		if(this.volume != null)
			jo.accumulate("volume", this.volume);
		return jo;
	}

	public String getDateString() {
		return dateString;
	}

	public void setDateString(String dateString) {
		this.dateString = dateString;
	}
}
