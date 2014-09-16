package org.mule.templates.integration;

import static junit.framework.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.templates.utils.Employee;

import com.mulesoft.module.batch.BatchTestHelper;
import com.workday.hr.EmployeeGetType;
import com.workday.hr.EmployeeReferenceType;
import com.workday.hr.ExternalIntegrationIDReferenceDataType;
import com.workday.hr.IDType;
import com.workday.staffing.EventClassificationSubcategoryObjectIDType;
import com.workday.staffing.EventClassificationSubcategoryObjectType;
import com.workday.staffing.TerminateEmployeeDataType;
import com.workday.staffing.TerminateEmployeeRequestType;
import com.workday.staffing.TerminateEventDataType;

/**
 * The objective of this class is to validate the correct behavior of the flows
 * for this Anypoint Tempalte that make calls to external systems.
 * 
 */
public class BusinessLogicIT extends AbstractTemplateTestCase {

	protected static final int TIMEOUT_SEC = 60;
	private static SubflowInterceptingChainLifecycleWrapper retrieveUserFlow;
	private BatchTestHelper helper;
	private String EXT_ID, EMAIL = "bwillis@gmailtest.com";
	private String SFDC_ID;

	@BeforeClass
	public static void init(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -3);
		System.setProperty("migration.startDate", "\"" + sdf.format(cal.getTime()) + "\"");
	}
	
	@Before
	public void setUp() throws Exception {
		helper = new BatchTestHelper(muleContext);

		retrieveUserFlow = getSubFlow("retrieveUserSFDC");
		retrieveUserFlow.initialise();

		createTestDataInSandBox();
	}

	@After
	public void tearDown() throws Exception {		
		deleteTestDataFromSandBox();
	}

	@Test
	public void testMainFlow() throws Exception {

		runFlow("mainFlow");

		// Wait for the batch job executed by the poll flow to finish
		helper.awaitJobTermination(TIMEOUT_SEC * 1000, 500);
		helper.assertJobWasSuccessful();

		Map<String, Object> user = new HashMap<String, Object>();
		user.put("Email", EMAIL);
		Map<String, Object> payload = invokeRetrieveFlow(retrieveUserFlow,
				user);
		SFDC_ID = payload.get("Id").toString();
		
		assertEquals("The user should have been sync",
				EMAIL, payload.get("Email"));
	}

	@SuppressWarnings("unchecked")
	private void createTestDataInSandBox() throws MuleException, Exception {
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("hireEmployee");
		flow.initialise();
		logger.info("creating a workday employee...");
		try {
			MuleEvent res = flow.process(getTestEvent(prepareNewHire(), MessageExchangePattern.REQUEST_RESPONSE));			
			System.out.println("res:" + res.getMessage().getPayloadAsString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<Object> prepareNewHire(){
		EXT_ID = "Bruce_" + System.currentTimeMillis();
		logger.info("employee name: " + EXT_ID);
		Employee ee = new Employee(EXT_ID, "Willis1", EMAIL, "650-232-2323", "999 Main St", "San Francisco", "CA", "94105", "US", "o7aHYfwG", 
				"2014-04-17-07:00", "2014-04-21-07:00", "QA Engineer", "San_Francisco_site", "Regular", "Full Time", "Salary", "USD", "140000", "Annual", "39905", "21440", EXT_ID);
		List<Object> list = new ArrayList<Object>();
		list.add(ee);
		return list;
	}
	
	private void deleteTestDataFromSandBox() throws MuleException, Exception {
		// Delete the created users in SFDC
		SubflowInterceptingChainLifecycleWrapper deleteUserFromAFlow = getSubFlow("deleteUserSFDC");
		deleteUserFromAFlow.initialise();

		List<String> idList = new ArrayList<String>();
		idList.add(SFDC_ID);
		deleteUserFromAFlow.process(getTestEvent(idList,
				MessageExchangePattern.REQUEST_RESPONSE));

		// Delete the created users in Workday
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("getWorkdayEmployee");
		flow.initialise();
		
		try {
			MuleEvent response = flow.process(getTestEvent(getEmployee(), MessageExchangePattern.REQUEST_RESPONSE));			
			flow = getSubFlow("terminateWorkdayEmployee");
			flow.initialise();
			response = flow.process(getTestEvent(prepareTerminate(response), MessageExchangePattern.REQUEST_RESPONSE));
			System.out.println("res: " + response.getMessage().getPayloadAsString());						
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private TerminateEmployeeRequestType prepareTerminate(MuleEvent response) throws DatatypeConfigurationException{
		TerminateEmployeeRequestType req = (TerminateEmployeeRequestType) response.getMessage().getPayload();
		TerminateEmployeeDataType eeData = req.getTerminateEmployeeData();		
		TerminateEventDataType event = new TerminateEventDataType();
		eeData.setTerminationDate(xmlDate(new Date()));
		EventClassificationSubcategoryObjectType prim = new EventClassificationSubcategoryObjectType();
		List<EventClassificationSubcategoryObjectIDType> list = new ArrayList<EventClassificationSubcategoryObjectIDType>();
		EventClassificationSubcategoryObjectIDType id = new EventClassificationSubcategoryObjectIDType();
		id.setType("WID");
		id.setValue("208082cd6b66443e801d95ffdc77461b");
		list.add(id);
		prim.setID(list);
		event.setPrimaryReasonReference(prim);
		eeData.setTerminateEventData(event );
		return req;		
	}
	
	private static XMLGregorianCalendar xmlDate(Date date) throws DatatypeConfigurationException {
		GregorianCalendar gregorianCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
		gregorianCalendar.setTime(date);
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
	}

	
	private EmployeeGetType getEmployee(){
		EmployeeGetType get = new EmployeeGetType();
		EmployeeReferenceType empRef = new EmployeeReferenceType();					
		ExternalIntegrationIDReferenceDataType value = new ExternalIntegrationIDReferenceDataType();
		IDType idType = new IDType();
		value.setID(idType);
		idType.setSystemID("Jobvite");
		idType.setValue(EXT_ID);			
		empRef.setIntegrationIDReference(value);
		get.setEmployeeReference(empRef);		
		return get;
	}


}
