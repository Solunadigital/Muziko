package com.muziko.salut;

import java.util.HashMap;

public class SalutServiceData {

	HashMap<String, String> serviceData;

	public SalutServiceData(String serviceName, int port, String instanceName) {
		serviceData = new HashMap<>();
		serviceData.put("SERVICE_NAME", "_" + serviceName);
		serviceData.put("SERVICE_PORT", "" + port);
		serviceData.put("INSTANCE_NAME", instanceName);
	}
}
