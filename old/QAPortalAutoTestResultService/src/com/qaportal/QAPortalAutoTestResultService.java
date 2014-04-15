package com.qaportal;

import java.util.ArrayList;
import java.util.List;

import org.wso2.ws.dataservice.DataServiceFault;
import org.wso2.ws.dataservice.Entry;
import org.wso2.ws.dataservice.GetTestcasesforAutoTestResultsService;
import org.wso2.ws.dataservice.GetTestcasesforAutoTestResultsServicePortType;
import org.wso2.ws.dataservice.InsertBuildMappingTestResultPortType;
import org.wso2.ws.dataservice.InsertBuildMappingTestResult_Service;
import org.wso2.ws.dataservice.InsertTestResultPortType;
import org.wso2.ws.dataservice.InsertTestResult_Service;
import org.wso2.ws.dataservice.TestCaseTestResultMapping;
import org.wso2.ws.dataservice.TestCaseTestResultMappingPortType;
import org.wso2.ws.dataservice.UpdateResultsService;
import org.wso2.ws.dataservice.UpdateResultsServicePortType;
import org.wso2.ws.dataservice.WSO2QAPTESTCASE;
import org.wso2.ws.dataservice.WSO2QAPTESTRESULTID;

public class QAPortalAutoTestResultService {
private String testClassName;
private String buildName;
private int autoTestResult;
private List<Integer> testcaseIDList;
private List<Integer> testResultIDList;
private String submitResultStatus;

public String submitResult(String className,String buildVersionName,int testResult)
{
	GetTestcasesforAutoTestResultsService testForAutoService = new GetTestcasesforAutoTestResultsService();
	GetTestcasesforAutoTestResultsServicePortType testForAutoPort = testForAutoService.getSOAP11Endpoint(); 
	testClassName=className;
	buildName=buildVersionName;
	autoTestResult=testResult;
	testcaseIDList = new ArrayList<Integer>(); 
	testResultIDList = new ArrayList<Integer>();
	try
	{
		List<WSO2QAPTESTCASE> testCaseIdObjs = testForAutoPort.getTestCases(testClassName, buildName);
		
		
		if(testCaseIdObjs.isEmpty())
		{
			//no test cases in the system for the automated test bed results
			//log
			System.out.println("no results...");
			submitResultStatus="FAIL";
		}
		else
		{
			
			for(WSO2QAPTESTCASE testcase:testCaseIdObjs)
			{				
				testcaseIDList.add(Integer.parseInt(testcase.getWSO2QAPTESTCASEID()));
			}
			/*
			 * GetTestcasesforAutoTestResultsService returns Test Case IDs which are affected by the auto bed test results.
			 * these test cases are used to find out which test results should be appended.
			 */
			TestCaseTestResultMapping caseResultMappingService = new TestCaseTestResultMapping();
			TestCaseTestResultMappingPortType caseResultMappingPort = caseResultMappingService.getSOAP11Endpoint();
			
			List<WSO2QAPTESTRESULTID> testResultIDObjs = caseResultMappingPort.getTestCaseTestResultMapping(testcaseIDList,buildName);
			
			
			if(testResultIDObjs.isEmpty())
			{
				/*
				 * in the case where there are no test results to be appended, a new test result is inserted.  
				 */
				
				InsertTestResult_Service insertResultService = new InsertTestResult_Service();
				InsertTestResultPortType insertResultPort = insertResultService.getSOAP11Endpoint();
				
				for(int testId : testcaseIDList)
				{
					List<Entry> resultID = insertResultPort.insertTestResult(testId,autoTestResult);
					
					InsertBuildMappingTestResult_Service buildMapService = new InsertBuildMappingTestResult_Service();
					InsertBuildMappingTestResultPortType buildMapPort = buildMapService.getSOAP11Endpoint();
					
					buildMapPort.insertBuildMappingTestResult(buildName, resultID.get(0).getID().intValue());
					
				}
				
				submitResultStatus="PASS:INSERT";	
				
			}
			else
			{
				/*
				 * in the case where there are test cases to be appended, an update is carried out. 
				 */				
				
				for(WSO2QAPTESTRESULTID resultidObj: testResultIDObjs)
				{
					testResultIDList.add(resultidObj.getWSO2QAPTESTRESULTID().intValue());
				
				}
				
				UpdateResultsService updateResultsService = new UpdateResultsService();
				UpdateResultsServicePortType updateResultsPort = updateResultsService.getSOAP11Endpoint();
				
				updateResultsPort.updateTestResult(autoTestResult, testResultIDList);
				
				submitResultStatus="PASS:UPDATE";
			}
			
		}
		
		
		
		
	}
	catch(DataServiceFault ex)
	{
		submitResultStatus="FAIL";
		ex.printStackTrace();
	}
	
	return submitResultStatus;
}
}
