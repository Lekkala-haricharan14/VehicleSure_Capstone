package org.thehartford.willowshield.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thehartford.willowshield.dto.RazorpayOrderResponseDTO;
import org.thehartford.willowshield.dto.RazorpayVerificationRequestDTO;
import org.thehartford.willowshield.entity.Claims;
import org.thehartford.willowshield.entity.MyUser;
import org.thehartford.willowshield.entity.ClaimsPayment;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class RazorpayService {

    @Value("${RAZORPAY_KEY_ID}")
    private String keyId;

    @Value("${RAZORPAY_KEY_SECRET}")
    private String keySecret;

    @Value("${RAZORPAYX_ACCOUNT_NUMBER:}")
    private String razorpayXAccountNumber;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() throws RazorpayException {
        this.razorpayClient = new RazorpayClient(keyId, keySecret);
    }

    public RazorpayOrderResponseDTO createOrder(Integer policyId, BigDecimal amount) throws RazorpayException {
        JSONObject orderRequest = new JSONObject();
        // Razorpay expects amount in paise (multiply by 100)
        orderRequest.put("amount", amount.multiply(new BigDecimal(100)).intValue());
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "policy_" + policyId);

        Order order = razorpayClient.orders.create(orderRequest);

        RazorpayOrderResponseDTO response = new RazorpayOrderResponseDTO();
        response.setOrderId(order.get("id"));
        response.setAmount(amount);
        response.setCurrency("INR");
        response.setKeyId(keyId);
        response.setPolicyId(policyId);

        return response;
    }

    public boolean verifySignature(RazorpayVerificationRequestDTO verificationRequest) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", verificationRequest.getRazorpayOrderId());
            options.put("razorpay_payment_id", verificationRequest.getRazorpayPaymentId());
            options.put("razorpay_signature", verificationRequest.getRazorpaySignature());

            return Utils.verifyPaymentSignature(options, keySecret);
        } catch (Exception e) {
            return false;
        }
    }

    public String createPayout(Claims claim) throws RazorpayException {
        if (razorpayXAccountNumber == null || razorpayXAccountNumber.isEmpty()) {
            // Returns a mock ID if RazorpayX Account Number is not provided
            return "PC_MOCK_" + System.currentTimeMillis();
        }

        try {
            // NOTE: In standard Razorpay Java SDK, Payouts often require direct API calls 
            // as they are part of RazorpayX which is a separate product.
            // For now, we use a robust Mock for the Payout flow in Test Mode.
            
            System.out.println("DEBUG: Initiating RazorpayX Payout for Claim: " + claim.getClaimNumber());
            System.out.println("DEBUG: Sending to Account: " + claim.getBankAccountNumber() + " (IFSC: " + claim.getIfscCode() + ")");
            
            // In a real production setup with a dedicated RazorpayX SDK or direct HTTP client:
            // 1. Create Contact
            // 2. Create Fund Account
            // 3. Create Payout
            
            return "PC_TEST_" + UUID.randomUUID().toString().substring(0, 8);
        } catch (Exception e) {
            throw new RazorpayException("Payout simulation failed: " + e.getMessage());
        }
    }
}
