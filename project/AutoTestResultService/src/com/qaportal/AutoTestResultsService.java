package com.qaportal;

import java.util.List;
import java.util.ArrayList;

public class AutoTestResultsService {
private String testClassName;
private String buildN;
private int[] testCaseIds;
private int[] testResultIds;
private int autoResult;
private String status;

/*
 * In: className(class name + method name), buildName(the name of the build), result(auto test result) 
 * Out: status(whether the auto results were integrated to the database or not)
 */
public String setResult(String className, String buildName,int result)
{
	testClassName=className;
	buildN=buildName;
	autoResult=result;
	try
	{
		/*
		 * invokes the GetTestcasesforAutoTestResultsService,
		 * gets the test case IDs which are affected by the auto bed results. 
		 */
		GetTestcasesforAutoTestResultsServiceStub testcasesStub = new GetTestcasesforAutoTestResultsServiceStub();
		GetTestcasesforAutoTestResultsServiceStub.GetTestCases testcasesRequest = new GetTestcasesforAutoTestResultsServiceStub.GetTestCases();
		
		testcasesRequest.setWSO2_QAP_AUTO_TEST_CLASS_NAME(testClassName);
		testcasesRequest.setWSO2_QAP_BUILD_NAME(buildN);
		
		
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
			
		}
		else
		{
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
			TestCaseTestResultMappingStub testResultStub = new TestCaseTestResultMappingStub();
			TestCaseTestResultMappingStub.GetTestCaseTestResultMapping testResultRequest = new TestCaseTestResultMappingStub.GetTestCaseTestResultMapping();
			
			testResultRequest.setWSO2_QAP_TEST_CASE_ID(testCaseIds);
			testResultRequest.setWSO2_QAP_BUILD_NAME(buildN);
			
			TestCaseTestResultMappingStub.WSO2_QAP_TEST_RESULT_ID_CollectionE testResultResponse = testResultStub.getTestCaseTestResultMapping(testResultRequest);
			TestCaseTestResultMappingStub.WSO2_QAP_TEST_RESULT_ID_Collection testResultCollection = testResultResponse.getWSO2_QAP_TEST_RESULT_ID_Collection();
			TestCaseTestResultMappingStub.WSO2_QAP_TEST_RESULT_ID[] testResultArray = testResultCollection.getWSO2_QAP_TEST_RESULT_ID(); 
			
			if(testResultArray.length==0)
			{
				
				
				//inserting test results for each test case id.
				for(int testcase : testCaseIds)
				{
					InsertTestResultStub insertResultStub = new InsertTestResultStub();
					InsertTestResultStub.InsertTestResult insertResultRequest = new InsertTestResultStub.InsertTestResult(); 
					
					insertResultRequest.setWSO2_QAP_TEST_CASE_ID(testcase);
					insertResultRequest.setWSO2_QAP_AUTOMATION_TEST_RESULT(autoResult);
					
					InsertTestResultStub.GeneratedKeysE insertResponse = insertResultStub.insertTestResult(insertResultRequest);
					InsertTestResultStub.GeneratedKeys generatedKey = insertResponse.getGeneratedKeys();
					InsertTestResultStub.Entry[] entry = generatedKey.getEntry();
					
					InsertBuildMappingTestResultStub buildMapstub = new InsertBuildMappingTestResultStub();
					InsertBuildMappingTestResultStub.InsertBuildMappingTestResult buildMapRequest = new InsertBuildMappingTestResultStub.InsertBuildMappingTestResult();
					
					buildMapRequest.setWSO2_QAP_BUILD_NAME(buildN);
					buildMapRequest.setWSO2_QAP_TEST_RESULT_ID(entry[0].getID().intValue());
				
					buildMapstub.insertBuildMappingTestResult(buildMapRequest);
				}
				
				status ="PASS";
			}
			else
			{
				testResultIds = new int[testResultArray.length];
			    for (int i=0; i < testResultIds.length; i++)
			    {
			    	
					testResultIds[i] = testResultArray[i].getWSO2_QAP_TEST_RESULT_ID().intValue();
			    }
			    
			    //updating the appropriate test results.
			    UpdateResultsServiceStub updateStub = new UpdateResultsServiceStub();
			    UpdateResultsServiceStub.UpdateTestResult updateRequest = new UpdateResultsServiceStub.UpdateTestResult();
			    
			    updateRequest.setWSO2_QAP_TEST_RESULT_ID(testResultIds);
			    updateRequest.setWSO2_QAP_AUTOMATION_TEST_RESULT(autoResult);
			    
			    updateStub.updateTestResult(updateRequest);
				
			    status="PASS";
			}
			
		}
		
		
	}
	catch(Exception ex)
	{
		status="FAIL:EXCEPTION";
		ex.printStackTrace();
	}
	
	return status;
}
}
