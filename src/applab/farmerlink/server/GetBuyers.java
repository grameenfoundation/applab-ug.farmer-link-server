package applab.farmerlink.server;

import applab.server.ApplabConfiguration;
import applab.server.ApplabServlet;
import applab.server.WebAppId;

import java.io.PrintWriter;
import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.enterprise.SessionHeader;
import com.sforce.soap.enterprise.SforceServiceLocator;
import com.sforce.soap.enterprise.SoapBindingStub;
import com.sforce.soap.enterprise.fault.LoginFault;
import com.sforce.soap.schemas._class.GetBuyers.GetBuyersBindingStub;
import com.sforce.soap.schemas._class.GetBuyers.GetBuyersServiceLocator;

/**
 * Servlet implementation class GetBuyers
 */
public class GetBuyers extends ApplabServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public GetBuyers() {
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
		String jsonResult = getBuyersFromSalesforce(selectedDistrict, selectedCrop);
		PrintWriter sendResponse = response.getWriter();
		sendResponse.println(jsonResult);
	}

	private String getBuyersFromSalesforce(String selectedDistrict,
			String selectedCrop) throws Exception {
		GetBuyersBindingStub serviceStub = setupSalesforceAuthentication();
		String buyers = serviceStub.getBuyers(selectedDistrict, selectedCrop);
		return buyers;
	}

	private GetBuyersBindingStub setupSalesforceAuthentication() throws Exception, Exception, LoginFault, RemoteException {
        GetBuyersServiceLocator getBuyersServiceLocator = new GetBuyersServiceLocator();
        GetBuyersBindingStub serviceStub = (GetBuyersBindingStub)getBuyersServiceLocator.getGetBuyers();

        // Use soap api to login and get session info
        SforceServiceLocator soapServiceLocator = new SforceServiceLocator();
        soapServiceLocator.setSoapEndpointAddress((String)ApplabConfiguration.getConfigParameter(WebAppId.global, "salesforceAddress", ""));
        SoapBindingStub binding = (SoapBindingStub)soapServiceLocator.getSoap();
        LoginResult loginResult = binding.login((String)ApplabConfiguration.getConfigParameter(WebAppId.global, "salesforceUsername", ""),
                (String)ApplabConfiguration.getConfigParameter(WebAppId.global, "salesforcePassword", "")
                        + (String)ApplabConfiguration.getConfigParameter(WebAppId.global, "salesforceToken", ""));
        SessionHeader sessionHeader = new SessionHeader(loginResult.getSessionId());

        // Share the session info with our webservice
        serviceStub.setHeader("http://soap.sforce.com/schemas/class/GetBuyers", "SessionHeader", sessionHeader);
        return serviceStub;
	}

}
