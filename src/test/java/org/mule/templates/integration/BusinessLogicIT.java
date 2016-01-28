/**
 * Mule Anypoint Template
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;



import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleException;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;

import com.mulesoft.module.batch.BatchTestHelper;

/**
 * The objective of this class is to validate the correct behavior of the flows
 * for this Anypoint Template that make calls to external systems.
 * 
 */
public class BusinessLogicIT extends AbstractTemplateTestCase {

	private static final Logger log = LogManager.getLogger(BusinessLogicIT.class);
	private static final String TEMPLATE_PREFFIX = "wday2sfdc-worker-migration";
	protected static final int TIMEOUT_SEC = 300;
	private static SubflowInterceptingChainLifecycleWrapper retrieveUserFlow;
	private static SubflowInterceptingChainLifecycleWrapper updateWorkerNameFlow;
	private static final String PATH_TO_TEST_PROPERTIES = "./src/test/resources/mule.test.properties";
	private BatchTestHelper helper;
	private String EMAIL = "bwillis@gmailtest.com";
	private Map<String, Object> testEmployee;

	@BeforeClass
	public static void init(){
		DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");	
		System.setProperty("migration.startDate", df.print(new Date().getTime()));
	}
	
	@Before
	public void setUp() throws Exception {
		helper = new BatchTestHelper(muleContext);

		retrieveUserFlow = getSubFlow("retrieveUserSFDC");
		retrieveUserFlow.initialise();
		updateWorkerNameFlow = getSubFlow("updateWorkdayEmployee");
		updateWorkerNameFlow.initialise();
		
		final Properties props = new Properties();
    	try {
    		props.load(new FileInputStream(PATH_TO_TEST_PROPERTIES));
    	} catch (Exception e) {
    		log.error("Error occured while reading mule.test.properties", e);
    	} 
    	
    	createTestDataInSandBox();
	}

	@Test
	public void testMainFlow() throws Exception {
		Thread.sleep(20000);
		runFlow("mainFlow");

		// Wait for the batch job executed by the poll flow to finish
		helper.awaitJobTermination(TIMEOUT_SEC * 1000, 500);
		helper.assertJobWasSuccessful();

		Map<String, Object> payload = invokeRetrieveFlow(retrieveUserFlow, testEmployee);
		
		Assert.assertEquals("The user first name should have been sync", testEmployee.get("givenName"), payload.get("FirstName"));
		Assert.assertEquals("The user last name should have been sync", testEmployee.get("familyName"), payload.get("LastName"));
	}

	private void createTestDataInSandBox() throws MuleException, Exception {
		log.info("updating a workday employee...");
		testEmployee = buildTestEmployee();
		try {
			updateWorkerNameFlow.process(getTestEvent(testEmployee, MessageExchangePattern.REQUEST_RESPONSE));						
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Map<String,Object> buildTestEmployee(){
		String name = TEMPLATE_PREFFIX + System.currentTimeMillis();
		log.info("employee name: " + name);
		Map<String,Object> employee = new HashMap<>();
		employee = new HashMap<String, Object>();
		employee.put("givenName", name);
		employee.put("familyName", name);
		employee.put("email", EMAIL);
		return testEmployee;
	}		
	
}
