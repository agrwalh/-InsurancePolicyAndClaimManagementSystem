package com.harshit.monocept.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.harshit.monocept.entity.Policy;
import com.harshit.monocept.exception.BusinessRuleException;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

@Service
public class RazorpayService {

	private static final Logger log = LoggerFactory.getLogger(RazorpayService.class);

	@Value("${razorpay.key-id}")
	private String keyId;

	@Value("${razorpay.key-secret}")
	private String keySecret;


	public Order createOrder(Policy policy) {
		if (keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank()) {
			throw new BusinessRuleException("Payment gateway is not configured. Please contact support.");
		}

		try {
			RazorpayClient client = new RazorpayClient(keyId, keySecret);

			BigDecimal amountInRupees = policy.getInstallmentAmount();
			long amountInPaise = amountInRupees.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP)
					.longValueExact();

			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", amountInPaise);
			orderRequest.put("currency", "INR");
			orderRequest.put("receipt", "policy_" + policy.getId() + "_" + System.currentTimeMillis());

			JSONObject notes = new JSONObject();
			notes.put("policyId", String.valueOf(policy.getId()));
			notes.put("policyNumber", policy.getPolicyNumber());
			orderRequest.put("notes", notes);

			Order order = client.orders.create(orderRequest);
			log.info("Razorpay order created: orderId={}, policyId={}, amount={}", order.get("id"), policy.getId(),
					amountInRupees);
			return order;

		} catch (Exception e) {
			log.error("Razorpay order creation failed for policyId={}: {}", policy.getId(), e.getMessage());
			throw new BusinessRuleException("Could not initiate payment gateway. Please try again.");
		}
	}


	public boolean verifySignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
		try {
			JSONObject options = new JSONObject();
			options.put("razorpay_order_id", razorpayOrderId);
			options.put("razorpay_payment_id", razorpayPaymentId);
			options.put("razorpay_signature", razorpaySignature);

			return Utils.verifyPaymentSignature(options, keySecret);
		} catch (Exception e) {
			log.error("Razorpay signature verification error: {}", e.getMessage());
			return false;
		}
	}

	public String getKeyId() {
		return keyId;
	}
}