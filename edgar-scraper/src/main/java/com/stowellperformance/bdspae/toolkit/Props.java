package com.stowellperformance.bdspae.toolkit;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Props {

	public static String getProperty(String propFile, String propName) throws IOException{
		InputStream input = new FileInputStream("src/main/resources/"+propFile);
        Properties p = new Properties();
        p.load(input);
        String prop = p.getProperty(propName);
        return prop;
	}
}