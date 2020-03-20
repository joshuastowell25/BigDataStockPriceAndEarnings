package com.stowellperformance.bdspae.domain;

import java.time.temporal.Temporal;
import java.util.Date;

public class DateRange {
	private Date startDate;
	private Date endDate;
	
	public DateRange(Date startDate, Date endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public boolean inRange(Date d) {
		   return !(d.before(startDate) || d.after(endDate));
	}
	
	/**
	 * Tells how many days after this range the given date is
	 * @param d
	 * @return
	 */
	public long daysAfter(Date d) {
		if(d != null) {
			Temporal t1 = endDate.toInstant();
			Temporal t2 = d.toInstant();
			return java.time.temporal.ChronoUnit.DAYS.between(t1, t2);
		}else {
			return -1;
		}
	}
	
	public boolean isValid() {
		return startDate != null && endDate != null;
	}
	
	public long dayCount() {
		Temporal t1 = startDate.toInstant();
		Temporal t2 = endDate.toInstant();
		return java.time.temporal.ChronoUnit.DAYS.between(t1, t2);
	}
}
