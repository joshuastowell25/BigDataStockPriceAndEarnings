package com.stowellperformance.bdspae.domain;

public class Company {
    private String cik;
    private String ticker;
    private String name;
    private String exchange;
    private String sic;
    private String business;
    private String incorporated;
    private String irs;
	public String getCik() {
		return cik;
	}
	public void setCik(String cik) {
		this.cik = cik;
	}
	public String getTicker() {
		return ticker;
	}
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getExchange() {
		return exchange;
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
	public String getSic() {
		return sic;
	}
	public void setSic(String sic) {
		this.sic = sic;
	}
	public String getBusiness() {
		return business;
	}
	public void setBusiness(String business) {
		this.business = business;
	}
	public String getIncorporated() {
		return incorporated;
	}
	public void setIncorporated(String incorporated) {
		this.incorporated = incorporated;
	}
	public String getIrs() {
		return irs;
	}
	public void setIrs(String irs) {
		this.irs = irs;
	}
	public Company(String cik, String ticker, String name, String exchange, String sic, String business,
			String incorporated, String irs) {
		super();
		this.cik = cik;
		this.ticker = ticker;
		this.name = name;
		this.exchange = exchange;
		this.sic = sic;
		this.business = business;
		this.incorporated = incorporated;
		this.irs = irs;
	}
    
}
