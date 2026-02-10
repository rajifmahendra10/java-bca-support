package com.bca.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

/**
 * BCA Security Demo Application
 * 
 * This application intentionally uses vulnerable dependencies to demonstrate
 * JFrog Xray security scanning capabilities.
 * 
 * VULNERABILITIES INCLUDED:
 * - Log4j 2.14.1 (CVE-2021-44228 - Log4Shell - CRITICAL)
 * - Spring Core 5.2.0 (Multiple CVEs - HIGH)
 * 
 * These vulnerabilities should be detected and blocked by JFrog Xray.
 */
public class App {
    private static final Logger logger = LogManager.getLogger(App.class);
    
    public static void main(String[] args) {
        logger.info("========================================");
        logger.info("BCA Security Demo Application Starting");
        logger.info("========================================");
        
        App app = new App();
        app.run();
        
        logger.info("Application completed successfully");
    }
    
    public void run() {
        logger.info("Running security demo...");
        
        // Demo 1: Test vulnerable Log4j
        testLog4j();
        
        // Demo 2: Test JSON processing
        testJsonProcessing();
        
        // Demo 3: Test business logic
        testBusinessLogic();
    }
    
    private void testLog4j() {
        logger.info("Testing Log4j vulnerability detection...");
        logger.warn("This application uses Log4j 2.14.1 - VULNERABLE to Log4Shell!");
        logger.error("CVE-2021-44228 should be detected by JFrog Xray");
    }
    
    private void testJsonProcessing() {
        logger.info("Testing JSON processing with GSON...");
        
        Gson gson = new Gson();
        Map<String, String> data = new HashMap<>();
        data.put("bank", "BCA");
        data.put("message", "Testing JFrog Xray Integration");
        data.put("status", "vulnerable");
        
        String json = gson.toJson(data);
        logger.info("JSON: " + json);
    }
    
    private void testBusinessLogic() {
        logger.info("Testing business logic...");
        
        String result = processTransaction("TRX-001", 1000);
        logger.info("Transaction result: " + result);
    }
    
    public String processTransaction(String transactionId, double amount) {
        logger.debug("Processing transaction: " + transactionId + " Amount: " + amount);
        
        // Simulate business logic
        if (amount > 0) {
            return "SUCCESS: Transaction " + transactionId + " processed";
        } else {
            return "FAILED: Invalid amount";
        }
    }
}
