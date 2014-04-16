
/**
 * DataServiceFaultException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */

package com.qaportal;

public class DataServiceFaultException extends java.lang.Exception{

    private static final long serialVersionUID = 1397627891982L;
    
    private com.qaportal.GetTestcasesforAutoTestResultsServiceStub.DataServiceFault faultMessage;

    
        public DataServiceFaultException() {
            super("DataServiceFaultException");
        }

        public DataServiceFaultException(java.lang.String s) {
           super(s);
        }

        public DataServiceFaultException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public DataServiceFaultException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(com.qaportal.GetTestcasesforAutoTestResultsServiceStub.DataServiceFault msg){
       faultMessage = msg;
    }
    
    public com.qaportal.GetTestcasesforAutoTestResultsServiceStub.DataServiceFault getFaultMessage(){
       return faultMessage;
    }
}
    