/**
 * Copyright (C) 2012 Grameen Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package applab.farmerlink.server;

import applab.server.ApplabConfiguration;
import applab.server.ApplabServlet;
import applab.server.ServletRequestContext;
import applab.server.WebAppId;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.enterprise.SessionHeader;
import com.sforce.soap.enterprise.SforceServiceLocator;
import com.sforce.soap.enterprise.SoapBindingStub;
import com.sforce.soap.schemas._class.GetFarmersAndMarketPrices.GetFarmersAndMarketPricesBindingStub;
import com.sforce.soap.schemas._class.GetFarmersAndMarketPrices.GetFarmersAndMarketPricesServiceLocator;

/**
 * Servlet implementation class GetFarmersAndMarketPrices
 */
public class GetFarmersAndMarketPrices extends ApplabServlet {
	private static final long serialVersionUID = 1L;
	private static final String DISTRICT_TAG = "district";
	private static final String CROP_TAG = "crop";

	/**
	 * @throws Exception
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doApplabGet(HttpServletRequest request,
			HttpServletResponse response, ServletRequestContext context)
			throws Exception {
		doApplabPost(request, response, context);
	}

	/**
	 * @throws Exception
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doApplabPost(HttpServletRequest request,
			HttpServletResponse response, ServletRequestContext context)
			throws Exception {

		Document requestXml = context.getRequestBodyAsXml();
		NodeList districtNodeList = requestXml
				.getElementsByTagName(DISTRICT_TAG);
		NodeList cropNodeList = requestXml.getElementsByTagName(CROP_TAG);
		String selectedDistrict = districtNodeList.item(0).getTextContent();
		String selectedCrop = cropNodeList.item(0).getTextContent();

		String jsonResult = getFarmersAndMarketPricesFromSalesforce(
				selectedDistrict, selectedCrop);
		PrintWriter sendResponse = response.getWriter();
		sendResponse.println(jsonResult);
	}

	private String getFarmersAndMarketPricesFromSalesforce(
			String selectedDistrict, String selectedCrop) throws Exception {
		GetFarmersAndMarketPricesBindingStub serviceStub = setupSalesforceAuthentication();
		String[] farmersAndMarketPrices = serviceStub
				.getFarmersAndMarketPrices(selectedDistrict, selectedCrop);
		String jsonString = createJson(farmersAndMarketPrices);
		return jsonString;
	}

	private String createJson(String[] farmersAndMarketPrices) {
		StringBuffer farmersAndMarketPricesJson = new StringBuffer();
		farmersAndMarketPricesJson.append("{\"farmers\" : ");
		farmersAndMarketPricesJson.append(farmersAndMarketPrices[0]);
		farmersAndMarketPricesJson.append(",\"marketprices\" : ");
		farmersAndMarketPricesJson.append(farmersAndMarketPrices[1]);
		farmersAndMarketPricesJson.append("}");
		return farmersAndMarketPricesJson.toString();
	}

	private GetFarmersAndMarketPricesBindingStub setupSalesforceAuthentication()
			throws Exception {
		GetFarmersAndMarketPricesServiceLocator getFarmersAndMarketPricesServiceLocator = new GetFarmersAndMarketPricesServiceLocator();
		GetFarmersAndMarketPricesBindingStub serviceStub = (GetFarmersAndMarketPricesBindingStub) getFarmersAndMarketPricesServiceLocator
				.getGetFarmersAndMarketPrices();

		// Use soap api to login and get session info
		SforceServiceLocator soapServiceLocator = new SforceServiceLocator();
		soapServiceLocator.setSoapEndpointAddress((String) ApplabConfiguration
				.getConfigParameter(WebAppId.global, "salesforceAddress", ""));
		SoapBindingStub binding = (SoapBindingStub) soapServiceLocator
				.getSoap();
		LoginResult loginResult = binding.login(
				(String) ApplabConfiguration.getConfigParameter(
						WebAppId.global, "salesforceUsername", ""),
				(String) ApplabConfiguration.getConfigParameter(
						WebAppId.global, "salesforcePassword", "")
						+ (String) ApplabConfiguration.getConfigParameter(
								WebAppId.global, "salesforceToken", ""));
		SessionHeader sessionHeader = new SessionHeader(
				loginResult.getSessionId());

		// Share the session info with our webservice
		serviceStub
				.setHeader(
						"http://soap.sforce.com/schemas/class/GetFarmersAndMarketPrices",
						"SessionHeader", sessionHeader);
		return serviceStub;
	}

}
