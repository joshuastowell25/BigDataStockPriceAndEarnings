# BigDataStockPriceAndEarnings Project

The purpose of this project is to collect data used to do a big data analysis in CouchDB.

The data sources used are the RapidAPI Yahoo Finance API and the SEC EDGAR system. The goal is to be able to analyze in CouchDB whether quarterly earnings reports have an effect on the price of the stock.

This project was done on a tight time schedule, so I apologize for the lack of completeness.

Here is an outline of the main 3 classes and their responsibilities:

## CompanyDownload.java

CompanyDownload is responsible for parsing a CSV file in the src/main/resources folder called cik_ticker.csv. This class also handles placing the data into CouchDB. The version of the file you see here is a very watered down version of the one that can be found here: http://rankandfiled.com/#/data/tickers

I removed thousands of companies and kept only those I was familiar with.

## HistoryDownload.java

HistoryDownload is responsible for downloading daily stock price data from the RapidAPI Yahoo Finance API. They have a very reasonable plan, even one that is free for fewer than 500 API calls per month. This class also handles placing the data into CouchDB.

## ReportDownload.java

ReportDownload is responsible for downloading index files (which list all reports for a given quarter) and then from those index files, downloading the quarterly earnings reports (10-Q). Beware, at the time of this writing there is 65GB+ of quarterly earnings reports available, and it took my machine an entire day to get them because they each require an individual HTTP request.

