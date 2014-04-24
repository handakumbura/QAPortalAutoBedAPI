package com.qaportal;


import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class AutoTestResultsService {
private String testClassName;
private String buildN;
private String pVersion;
private String pName;
private int[] testCaseIds;
private int[] testResultIds;
private int autoResult;
private String status;
private Logger autoTestLogger;

/*
 * In: className(class name + method name), buildName(the name of the build), pVersion(product version), pName(product name), result(auto test result)
 * Out: status(whether the auto results were integrated to the database or not)
 *  
 */
public String setResult(String className, String buildName,String prodVersion, String prodName, int result)
{
	testClassName=className;
	buildN=buildName;
	autoResult=result;
	pVersion=prodVersion;
	pName=prodName;
	autoTestLogger = Logger.getLogger(AutoTestResultsService.class);
	BasicConfigurator.configure();
	
	/*
	 * IMPORTANT: it is assumed that the data services used here and this service is hosted in the same server.
	 * if they are hosted in different servers, the end point URL for each data service must be changed using the params below.
	 * 
	 */
	String GetTestcasesforAutoTestResultsServiceURL = "http://localhost:9763/services/WSO2_QAP_GetTestcasesforAutoTestResultsService.SOAP12Endpoint/";
	String TestCaseTestResultMappingURL = "http://localhost:9763/services/WSO2_QAP_TestCaseTestResultMapping.SOAP12Endpoint/";
	String InsertTestResultURL = "http://localhost:9763/services/WSO2_QAP_InsertTestResult.SOAP12Endpoint/";
	String InsertBuildMappingTestResultURL = "http://localhost:9763/services/WSO2_QAP_InsertBuildMappingTestResult.SOAP12Endpoint/";
	String UpdateResultsServiceURL = "http://localhost:9763/services/WSO2_QAP_UpdateResultsService.SOAP12Endpoint/";
	
	
	try
	{
		/*
		 * invokes the GetTestcasesforAutoTestResultsService,
		 * gets the test case IDs which are affected by the auto bed results. 
		 */
		GetTestcasesforAutoTestResultsServiceStub testcasesStub = new GetTestcasesforAutoTestResultsServiceStub(GetTestcasesforAutoTestResultsServiceURL);
		GetTestcasesforAutoTestResultsServiceStub.GetTestCases testcasesRequest = new GetTestcasesforAutoTestResultsServiceStub.GetTestCases();
		testcasesRequest.setWSO2_QAP_AUTO_TEST_CLASS_NAME(testClassName);
		testcasesRequest.setWSO2_QAP_BUILD_NAME(buildN);
		testcasesRequest.setWSO2_QAP_PRODUCT_VERSION(pVersion);
		testcasesRequest.setWSO2_QAP_PRODUCT_NAME(pName);
		
		autoTestLogger.info("Invoking GetTestcasesforAutoTestResults Service");		
		GetTestcasesforAutoTestResultsServiceStub.WSO2_QAP_TEST_CASE_CollectionE testCaseResponse = testcasesStub.getTestCases(testcasesRequest);
		GetTestcasesforAutoTestResultsServiceStub.WSO2_QAP_TEST_CASE_Collection testCaseCollection = testCaseResponse.getWSO2_QAP_TEST_CASE_Collection();
		
		/*
		 * if the GetTestcasesforAutoTestResultsService returns no test cases,
		 * the service returns a FAIL.
		 * the auto test result data passed is either incorrect or the test case data the auto test result belongs to have been removed from DB.    
		 */
		if(testCaseCollection.getWSO2_QAP_TEST_CASE()==null)
		{
			status ="FAIL";
			autoTestLogger.info("No Testcases found for params provided!. PARAMS: "+testClassName+" , "+buildN+" , "+pName+" , "+pVersion+" , "+autoResult);			
		}
		else
		{
			autoTestLogger.info("Testcases successfully retrieved");
			GetTestcasesforAutoTestResultsServiceStub.WSO2_QAP_TEST_CASE[] testcasesArray = testCaseCollection.getWSO2_QAP_TEST_CASE();
			
			testCaseIds = new int[testcasesArray.length];
		    for (int i=0; i < testCaseIds.length; i++)
		    {
		    	testCaseIds[i] = Integer.parseInt(testcasesArray[i].getWSO2_QAP_TEST_CASE_ID());
		    }
		    
		    /*
		     * the test result ids for the test cases are found using the TestCaseTestResultMapping service
		     * if the service returns no test result ids, new test results are entered, if not appropriate test results are updated.    
		     */
			TestCaseTestResultMappingStub testResultStub = new TestCaseTestResultMappingStub(TestCaseTestResultMappingURL);
			TestCaseTestResultMappingStub.GetTestCaseTestResultMapping testResultRequest = new TestCaseTestResultMappingStub.GetTestCaseTestResultMapping();
			
			testResultRequest.setWSO2_QAP_TEST_CASE_ID(testCaseIds);
			testResultRequest.setWSO2_QAP_BUILD_NAME(buildN);
			
			autoTestLogger.info("Invoking TestCaseTestResultMapping Service");
			TestCaseTestResultMappingStub.WSO2_QAP_TEST_RESULT_ID_CollectionE testResultResponse = testResultStub.getTestCaseTestResultMapping(testResultRequest);
			TestCaseTestResultMappingStub.WSO2_QAP_TEST_RESULT_ID_Collection testResultCollection = testResultResponse.getWSO2_QAP_TEST_RESULT_ID_Collection();
			
			
			if(testResultCollection.getWSO2_QAP_TEST_RESULT_ID()==null)
			{
				
				
				//inserting test results for each test case id.
				autoTestLogger.info("Inserting new test results to DB");
				for(int testcase : testCaseIds)
				{
					InsertTestResultStub insertResultStub = new InsertTestResultStub(InsertTestResultURL);
					InsertTestResultStub.InsertTestResult insertResultRequest = new InsertTestResultStub.InsertTestResult(); 
					
					insertResultRequest.setWSO2_QAP_TEST_CASE_ID(testcase);
					insertResultRequest.setWSO2_QAP_AUTOMATION_TEST_RESULT(autoResult);
					
					InsertTestResultStub.GeneratedKeysE insertResponse = insertResultStub.insertTestResult(insertResultRequest);
					InsertTestResultStub.GeneratedKeys generatedKey = insertResponse.getGeneratedKeys();
					InsertTestResultStub.Entry[] entry = generatedKey.getEntry();
					
					InsertBuildMappingTestResultStub buildMapstub = new InsertBuildMappingTestResultStub(InsertBuildMappingTestResultURL);
					InsertBuildMappingTestResultStub.InsertBuildMappingTestResult buildMapRequest = new InsertBuildMappingTestResultStub.InsertBuildMappingTestResult();
					
					buildMapRequest.setWSO2_QAP_BUILD_NAME(buildN);
					buildMapRequest.setWSO2_QAP_TEST_RESULT_ID(entry[0].getID().intValue());
				
					buildMapstub.insertBuildMappingTestResult(buildMapRequest);
				}
				
				status ="PASS";
			}
			else
			{
				TestCaseTestResultMappingStub.WSO2_QAP_TEST_RESULT_ID[] testResultArray = testResultCollection.getWSO2_QAP_TEST_RESULT_ID(); 
				
				testResultIds = new int[testResultArray.length];
			    for (int i=0; i < testResultIds.length; i++)
			    {
			    	
					testResultIds[i] = testResultArray[i].getWSO2_QAP_TEST_RESULT_ID().intValue();
			    }
			    
			    //updating the appropriate test results.
			    UpdateResultsServiceStub updateStub = new UpdateResultsServiceStub(UpdateResultsServiceURL);
			    UpdateResultsServiceStub.UpdateTestResult updateRequest = new UpdateResultsServiceStub.UpdateTestResult();
			    
			    updateRequest.setWSO2_QAP_TEST_RESULT_ID(testResultIds);
			    updateRequest.setWSO2_QAP_AUTOMATION_TEST_RESULT(autoResult);
			    autoTestLogger.info("Updating Test Results");
			    updateStub.updateTestResult(updateRequest);
			    
			    status="PASS";
			}
			
		}
		
		
	}
	catch(Exception ex)
	{
		status="FAIL";
		ex.printStackTrace();
	}
	
	return status;
}
}
