package com.finanscore.motorscoring.infrastructure.messaging.kafka;

public final class KafkaTopicNames {
    public static final String SCORING_REQUESTED = "scoring.requested.v1";
    public static final String PAYMENT_VALIDATED = "payment.validated.v1";
    public static final String PAYMENT_REJECTED = "payment.rejected.v1";
    public static final String SCORING_CALCULATED = "scoring.calculated.v1";
    public static final String CREDIT_SCORE_DELIVERED = "credit-score.delivered.v1";

    private KafkaTopicNames() {}

    public static String fromEventType(String eventType) {
        return switch (eventType) {
            case "ScoringRequested" -> SCORING_REQUESTED;
            case "PaymentValidated" -> PAYMENT_VALIDATED;
            case "PaymentRejected" -> PAYMENT_REJECTED;
            case "ScoringCalculated" -> SCORING_CALCULATED;
            case "CreditScoreDelivered" -> CREDIT_SCORE_DELIVERED;
            default -> throw new IllegalArgumentException("No existe topic configurado para eventType=" + eventType);
        };
    }
}
