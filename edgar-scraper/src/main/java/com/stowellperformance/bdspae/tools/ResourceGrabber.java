package com.stowellperformance.bdspae.tools;

import java.io.InputStream;

public class ResourceGrabber {

	public InputStream getResource(String resourceFilePath) {
		InputStream result = null;
		result = getClass().getClassLoader().getResourceAsStream(resourceFilePath);
		return result;
	}
}
