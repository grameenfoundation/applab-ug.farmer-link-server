package applab.farmerlink.server;

import applab.server.ApplabConfiguration;
import applab.server.ApplabServlet;
import applab.server.WebAppId;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    /**
     * Default constructor. 
     */
    public GetFarmersAndMarketPrices() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @throws Exception 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doApplabGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
		doApplabPost(request, response);
	}

	/**
	 * @throws Exception 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doApplabPost(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String selectedDistrict = request.getHeader("district");
		String selectedCrop = request.getHeader("crop");
		String jsonResult = getFarmersAndMarketPricesFromSalesforce(selectedDistrict, selectedCrop);
		PrintWriter sendResponse = response.getWriter();
		sendResponse.println(jsonResult);
	}

	private String getFarmersAndMarketPricesFromSalesforce(
			String selectedDistrict, String selectedCrop) throws Exception {
		GetFarmersAndMarketPricesBindingStub serviceStub = setupSalesforceAuthentication();
		String[] farmersAndMarketPrices = serviceStub.getFarmersAndMarketPrices(selectedDistrict, selectedCrop);
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

	private GetFarmersAndMarketPricesBindingStub setupSalesforceAuthentication() throws Exception {
        GetFarmersAndMarketPricesServiceLocator getFarmersAndMarketPricesServiceLocator = new GetFarmersAndMarketPricesServiceLocator();
        GetFarmersAndMarketPricesBindingStub serviceStub = (GetFarmersAndMarketPricesBindingStub)getFarmersAndMarketPricesServiceLocator.getGetFarmersAndMarketPrices();

        // Use soap api to login and get session info
        SforceServiceLocator soapServiceLocator = new SforceServiceLocator();
        soapServiceLocator.setSoapEndpointAddress((String)ApplabConfiguration.getConfigParameter(WebAppId.global, "salesforceAddress", ""));
        SoapBindingStub binding = (SoapBindingStub)soapServiceLocator.getSoap();
        LoginResult loginResult = binding.login((String)ApplabConfiguration.getConfigParameter(WebAppId.global, "salesforceUsername", ""),
                (String)ApplabConfiguration.getConfigParameter(WebAppId.global, "salesforcePassword", "")
                        + (String)ApplabConfiguration.getConfigParameter(WebAppId.global, "salesforceToken", ""));
        SessionHeader sessionHeader = new SessionHeader(loginResult.getSessionId());

        // Share the session info with our webservice
        serviceStub.setHeader("http://soap.sforce.com/schemas/class/GetFarmersAndMarketPrices", "SessionHeader", sessionHeader);
        return serviceStub;
	}

}
