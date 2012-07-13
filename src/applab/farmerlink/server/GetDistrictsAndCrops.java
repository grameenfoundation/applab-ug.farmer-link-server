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
import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.rpc.ServiceException;

import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.enterprise.SessionHeader;
import com.sforce.soap.enterprise.SforceServiceLocator;
import com.sforce.soap.enterprise.SoapBindingStub;
import com.sforce.soap.enterprise.fault.InvalidIdFault;
import com.sforce.soap.enterprise.fault.LoginFault;
import com.sforce.soap.enterprise.fault.UnexpectedErrorFault;
import com.sforce.soap.schemas._class.GetDistrictsAndCrops.GetDistrictsAndCropsBindingStub;
import com.sforce.soap.schemas._class.GetDistrictsAndCrops.GetDistrictsAndCropsServiceLocator;

/**
 * Servlet implementation class GetDistrictsAndCrops
 */
public class GetDistrictsAndCrops extends ApplabServlet {
	private static final long serialVersionUID = 1L;

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
		// Make Sales force call
		String jsonResult = getDistrictsAndCropsFromSalesforce();
		PrintWriter sendResponse = response.getWriter();
		sendResponse.println(jsonResult);
	}

	private String getDistrictsAndCropsFromSalesforce() throws Exception {
		GetDistrictsAndCropsBindingStub serviceStub = setupSalesforceAuthentication();
		String[] districtsAndCrops = serviceStub.getDistrictsAndCrops();
		String jsonString = createJson(districtsAndCrops);
		return jsonString;
	}

	private String createJson(String[] districtsAndCrops) {
		StringBuffer districtsAndCropsJson = new StringBuffer();
		districtsAndCropsJson.append("{\"districts\" : ");
		districtsAndCropsJson.append(districtsAndCrops[0]);
		districtsAndCropsJson.append(",\"crops\" : ");
		districtsAndCropsJson.append(districtsAndCrops[1]);
		districtsAndCropsJson.append("}");
		return districtsAndCropsJson.toString();
	}

	/**
	 * This authenticates and sets up a service stub for webservice calls
	 * 
	 * @return GetDistrictsAndCrops service stub
	 * @throws ServiceException
	 * @throws RemoteException
	 * @throws InvalidIdFault
	 * @throws UnexpectedErrorFault
	 * @throws LoginFault
	 */
	private GetDistrictsAndCropsBindingStub setupSalesforceAuthentication()
			throws Exception {

		GetDistrictsAndCropsServiceLocator getDistrictsAndCropsServiceLocator = new GetDistrictsAndCropsServiceLocator();
		GetDistrictsAndCropsBindingStub serviceStub = (GetDistrictsAndCropsBindingStub) getDistrictsAndCropsServiceLocator
				.getGetDistrictsAndCrops();

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
		serviceStub.setHeader(
				"http://soap.sforce.com/schemas/class/GetDistrictsAndCrops",
				"SessionHeader", sessionHeader);
		return serviceStub;
	}

}
