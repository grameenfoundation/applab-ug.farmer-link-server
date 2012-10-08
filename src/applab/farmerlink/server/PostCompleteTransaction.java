package applab.farmerlink.server;

import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.rpc.ServiceException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.enterprise.SessionHeader;
import com.sforce.soap.enterprise.SforceServiceLocator;
import com.sforce.soap.enterprise.SoapBindingStub;
import com.sforce.soap.enterprise.fault.InvalidIdFault;
import com.sforce.soap.enterprise.fault.LoginFault;
import com.sforce.soap.enterprise.fault.UnexpectedErrorFault;
import com.sforce.soap.schemas._class.CreateMarketLinkTransaction.CreateMarketLinkTransactionBindingStub;
import com.sforce.soap.schemas._class.CreateMarketLinkTransaction.CreateMarketLinkTransactionServiceLocator;
import com.sforce.soap.schemas._class.CreateMarketLinkTransaction.FarmerTransaction;
import com.sforce.soap.schemas._class.CreateMarketLinkTransaction.MarketLinkTransaction;

import applab.server.ApplabConfiguration;
import applab.server.ApplabServlet;
import applab.server.ServletRequestContext;
import applab.server.WebAppId;

/**
 * Servlet implementation class PostCompleteTransaction
 */
public class PostCompleteTransaction extends ApplabServlet {
    
    private static final long serialVersionUID = 1L;
    private final static String IMEI = "imei";    
    
    // Transaction details section
    private final static String TRANSACTION_DETAILS = "transactionDetails";
    private final static String CROP = "crop";
    private final static String DISTRICT = "district";
    private final static String DATE = "transactionDate";
    private final static String TYPE = "transactionType";
    private final static String NAME = "name";
    private final static String TOTAL_QUANTITY = "quantity";
    private final static String UNIT_PRICE = "unitPrice";
    private final static String TOTAL_INCOME = "revenue";
    private final static String TOTAL_TRANSPORT_COST = "transportFee";
    private final static String TOTAL_TRANSACTION_FEE = "transactionFee";
    private final static String COMPLETED = "completed";
    
    // Farmer details section
    private final static String FARMER = "farmer";
    private final static String FARMER_NAME = "farmerName";
    private final static String FARMER_ID = "farmerId";
    private final static String FARMER_QUANTITY = "quantity";
    private final static String FARMER_INCOME = "farmerRevenue";
    private final static String FARMER_TRANSPORT_COST = "transportFee";
    private final static String FARMER_TRANSACTION_FEE = "transactionFee";

    @Override
    protected void doApplabGet(HttpServletRequest request, HttpServletResponse response, ServletRequestContext context)
            throws Exception {
        log("Reached PostCompleteTransaction get method");
        doApplabPost(request, response, context);
    }
    
    @Override
    protected void doApplabPost(HttpServletRequest request, HttpServletResponse response, ServletRequestContext context)
            throws Exception {
        PrintWriter out = response.getWriter();
        try {
            log("Reached PostCompleteTransaction post method");
            
            Document requestXml = context.getRequestBodyAsXml();
            
            // get IMEI from header
            String imei = requestXml.getElementsByTagName(IMEI).item(0).getTextContent();
            
            // pick transaction data from XML
            MarketLinkTransaction marketLinkTransaction  = getTrasactionDetails(requestXml);
            marketLinkTransaction.setImei(imei);
            
            // pick Farmer details
            List<FarmerTransaction> fTransactions = getFarmerTransactions(requestXml);
            FarmerTransaction[] farmerTransactions = new FarmerTransaction[fTransactions.size()];
            
            // setup webservice call and make SF call
            CreateMarketLinkTransactionBindingStub stub = setupSalesforceAuthentication();
            String[] results = stub.createTransaction(marketLinkTransaction, fTransactions.toArray(farmerTransactions));
            log("Success: " + results[0]);
            log("Error Message" + results[1]);
            log("Message" + results[2]);
            out.println(results[0]);
                    
        }
        catch(Exception e) {
            e.printStackTrace();
            log(e.getMessage());
        }
    }

    /**
     * Get famer transaction details from XML
     * @param requestXml
     * 
     * @return List<FarmerTransaction> - matching salesforce adapter object
     */
    private List<FarmerTransaction> getFarmerTransactions(Document requestXml) {
        List<FarmerTransaction> farmerTransactions = new ArrayList<FarmerTransaction>();
        NodeList farmersNodeList = requestXml.getElementsByTagName(FARMER);
        
        for (int index = 0; index < farmersNodeList.getLength(); index++) {
            Node farmerNode = farmersNodeList.item(index);
            NamedNodeMap attributes = farmerNode.getAttributes();
            FarmerTransaction farmerTransaction = new FarmerTransaction();
            
            farmerTransaction.setName(attributes.getNamedItem(FARMER_NAME).getTextContent());
            farmerTransaction.setFarmerId(attributes.getNamedItem(FARMER_ID).getTextContent());
            farmerTransaction.setQuantity(Double.valueOf(attributes.getNamedItem(FARMER_QUANTITY).getTextContent()));
            farmerTransaction.setIncome(Double.valueOf(attributes.getNamedItem(FARMER_INCOME).getTextContent()));
            farmerTransaction.setTransportCost(Double.valueOf(attributes.getNamedItem(FARMER_TRANSPORT_COST).getTextContent()));
            farmerTransaction.setTransactionFee(Double.valueOf(attributes.getNamedItem(FARMER_TRANSACTION_FEE).getTextContent()));
            
            farmerTransactions.add(farmerTransaction);
        }
        return farmerTransactions;
    }

    /**
     * Get trasaction details from XML
     * @param requestXml
     * 
     * @return MarketLinkTransaction - matching salesforce adapter object
     */
    private MarketLinkTransaction getTrasactionDetails(Document requestXml) {
        
        MarketLinkTransaction transaction = new MarketLinkTransaction();
        NodeList transactionNodeList = requestXml.getElementsByTagName(TRANSACTION_DETAILS);
        Node transactionNode = transactionNodeList.item(0);
        NamedNodeMap attributes = transactionNode.getAttributes();
        
        transaction.setName(attributes.getNamedItem(NAME).getTextContent());
        transaction.setCompleted(attributes.getNamedItem(COMPLETED).getTextContent() == "true" ? true : false);
        transaction.setCrop(attributes.getNamedItem(CROP).getTextContent());
        transaction.setDistrict(attributes.getNamedItem(DISTRICT).getTextContent());
        transaction.setTransactionDate(attributes.getNamedItem(DATE).getTextContent());
        transaction.setTransactionType(attributes.getNamedItem(TYPE).getTextContent());
        transaction.setTotalQuantity(Double.valueOf(attributes.getNamedItem(TOTAL_QUANTITY).getTextContent()));
        transaction.setUnitPrice(Double.valueOf(attributes.getNamedItem(UNIT_PRICE).getTextContent()));
        transaction.setTotalIncome(Double.valueOf(attributes.getNamedItem(TOTAL_INCOME).getTextContent()));
        transaction.setTotalTransportCost(Double.valueOf(attributes.getNamedItem(TOTAL_TRANSPORT_COST).getTextContent()));
        transaction.setTotalTransactionFee(Double.valueOf(attributes.getNamedItem(TOTAL_TRANSACTION_FEE).getTextContent()));
        return transaction;
    }
    
    /**
     * This authenticates and sets up a service stub for webservice calls
     * 
     * @return CreateMarketLinkTransactionBindingStub service stub
     * @throws ServiceException
     * @throws RemoteException
     * @throws InvalidIdFault
     * @throws UnexpectedErrorFault
     * @throws LoginFault
     */
    private CreateMarketLinkTransactionBindingStub setupSalesforceAuthentication() throws ServiceException, RemoteException, InvalidIdFault,
            UnexpectedErrorFault, LoginFault {

        CreateMarketLinkTransactionServiceLocator serviceLocator = new CreateMarketLinkTransactionServiceLocator();
        CreateMarketLinkTransactionBindingStub serviceStub = (CreateMarketLinkTransactionBindingStub)serviceLocator.getCreateMarketLinkTransaction();

        // Use soap api to login and get session info
        SforceServiceLocator soapServiceLocator = new SforceServiceLocator();
        soapServiceLocator.setSoapEndpointAddress((String)ApplabConfiguration.getConfigParameter(WebAppId.global, "salesforceAddress", ""));
        SoapBindingStub binding = (SoapBindingStub)soapServiceLocator.getSoap();
        LoginResult loginResult = binding.login((String)ApplabConfiguration.getConfigParameter(WebAppId.global, "salesforceUsername", ""),
                (String)ApplabConfiguration.getConfigParameter(WebAppId.global, "salesforcePassword", "")
                        + (String)ApplabConfiguration.getConfigParameter(WebAppId.global, "salesforceToken", ""));
        SessionHeader sessionHeader = new SessionHeader(loginResult.getSessionId());

        // Share the session info with our webservice
        serviceStub.setHeader("http://soap.sforce.com/schemas/class/CreateMarketLinkTransaction", "SessionHeader", sessionHeader);
        return serviceStub;
    }
}