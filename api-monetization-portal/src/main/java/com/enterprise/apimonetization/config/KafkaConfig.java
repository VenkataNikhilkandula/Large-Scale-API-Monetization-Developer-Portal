package com.enterprise.apimonetization.config;

/**
 * Kafka configuration constants.
 * Topic creation is intentionally skipped here to allow the app
 * to start without a running Kafka broker. Topics are auto-created
 * by Kafka when the producer first sends a message (if Kafka is running).
 */
public class KafkaConfig {

    public static final String USAGE_TOPIC = "api-usage-events";
}
