package applab.farmerlink.server;

import applab.server.ApplabConfiguration;
import applab.server.ApplabServlet;
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
	private static final String IMEI = "x-Imei";

    /**
     * Default constructor. 
     */
    public GetDistrictsAndCrops() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @throws Exception 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doApplabGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log("Reached get method for get districts and crops");
        doApplabPost(request, response);
	}

	/**
	 * @throws Exception 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doApplabPost(HttpServletRequest request, HttpServletResponse response) throws Exception {
		log("Reached method to get the configured districts and crops");
		String imei = request.getHeader(IMEI);
		// Make Sales force call
		String jsonResult = getDistrictsAndCropsFromSalesforce(imei);
		PrintWriter sendResponse = response.getWriter();
		sendResponse.println(jsonResult);
		log("Finished sending districts and crops");
	}

	private String getDistrictsAndCropsFromSalesforce(String imei) throws Exception {
		GetDistrictsAndCropsBindingStub serviceStub = setupSalesforceAuthentication();
		String[] districtsAndCrops = serviceStub.getDistrictsAndCrops();
		String jsonString = createJson(districtsAndCrops);
		//"{\"districts\": [\"Abim\", \"Pader\",\"Nwoya\",\"Kitgum\"], \"crops\":[\"Simsim\", \"Beans\",\"Bananas\",\"Maize\"]}";
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
    private GetDistrictsAndCropsBindingStub setupSalesforceAuthentication() throws Exception {

        GetDistrictsAndCropsServiceLocator getDistrictsAndCropsServiceLocator = new GetDistrictsAndCropsServiceLocator();
        GetDistrictsAndCropsBindingStub serviceStub = (GetDistrictsAndCropsBindingStub)getDistrictsAndCropsServiceLocator.getGetDistrictsAndCrops();

        // Use soap api to login and get session info
        SforceServiceLocator soapServiceLocator = new SforceServiceLocator();
        soapServiceLocator.setSoapEndpointAddress((String)ApplabConfiguration.getConfigParameter(WebAppId.global, "salesforceAddress", ""));
        SoapBindingStub binding = (SoapBindingStub)soapServiceLocator.getSoap();
        LoginResult loginResult = binding.login((String)ApplabConfiguration.getConfigParameter(WebAppId.global, "salesforceUsername", ""),
                (String)ApplabConfiguration.getConfigParameter(WebAppId.global, "salesforcePassword", "")
                        + (String)ApplabConfiguration.getConfigParameter(WebAppId.global, "salesforceToken", ""));
        SessionHeader sessionHeader = new SessionHeader(loginResult.getSessionId());

        // Share the session info with our webservice
        serviceStub.setHeader("http://soap.sforce.com/schemas/class/GetDistrictsAndCrops", "SessionHeader", sessionHeader);
        return serviceStub;
    }

}
