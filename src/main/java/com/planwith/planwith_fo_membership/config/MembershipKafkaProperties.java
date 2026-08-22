package com.planwith.planwith_fo_membership.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "membership.kafka")
public class MembershipKafkaProperties {

	private boolean consumerEnabled = false;
	private Topics topics = new Topics();

	public boolean isConsumerEnabled() {
		return consumerEnabled;
	}

	public void setConsumerEnabled(boolean consumerEnabled) {
		this.consumerEnabled = consumerEnabled;
	}

	public Topics getTopics() {
		return topics;
	}

	public void setTopics(Topics topics) {
		this.topics = topics;
	}

	public static class Topics {
		private String tokenDeductionRequested = "planwith.token.deduction-requested";
		private String paymentCompleted = "planwith.payment.completed";
		private String paymentFailed = "planwith.payment.failed";
		private String paymentRefunded = "planwith.payment.refunded";
		private String membershipSubscribed = "planwith.membership.subscribed";
		private String membershipCanceled = "planwith.membership.canceled";
		private String membershipExpired = "planwith.membership.expired";
		private String settlementRequested = "planwith.membership.settlement-requested";
		private String settlementCompleted = "planwith.membership.settlement-completed";

		public String getTokenDeductionRequested() {
			return tokenDeductionRequested;
		}

		public void setTokenDeductionRequested(String tokenDeductionRequested) {
			this.tokenDeductionRequested = tokenDeductionRequested;
		}

		public String getPaymentCompleted() {
			return paymentCompleted;
		}

		public void setPaymentCompleted(String paymentCompleted) {
			this.paymentCompleted = paymentCompleted;
		}

		public String getPaymentFailed() {
			return paymentFailed;
		}

		public void setPaymentFailed(String paymentFailed) {
			this.paymentFailed = paymentFailed;
		}

		public String getPaymentRefunded() {
			return paymentRefunded;
		}

		public void setPaymentRefunded(String paymentRefunded) {
			this.paymentRefunded = paymentRefunded;
		}

		public String getMembershipSubscribed() {
			return membershipSubscribed;
		}

		public void setMembershipSubscribed(String membershipSubscribed) {
			this.membershipSubscribed = membershipSubscribed;
		}

		public String getMembershipCanceled() {
			return membershipCanceled;
		}

		public void setMembershipCanceled(String membershipCanceled) {
			this.membershipCanceled = membershipCanceled;
		}

		public String getMembershipExpired() {
			return membershipExpired;
		}

		public void setMembershipExpired(String membershipExpired) {
			this.membershipExpired = membershipExpired;
		}

		public String getSettlementRequested() {
			return settlementRequested;
		}

		public void setSettlementRequested(String settlementRequested) {
			this.settlementRequested = settlementRequested;
		}

		public String getSettlementCompleted() {
			return settlementCompleted;
		}

		public void setSettlementCompleted(String settlementCompleted) {
			this.settlementCompleted = settlementCompleted;
		}
	}
}
