import logging
from confluent_kafka import Producer
import json
import os

logger = logging.getLogger(__name__)

bootstrap = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
logger.info("Kafka producer connecting to: %s", bootstrap)

producer = Producer({
    "bootstrap.servers": bootstrap
})

TOPIC = "ai_service"

def delivery_report(err, msg):
    if err is not None:
        logger.error("Kafka delivery failed: %s", err)
    else:
        logger.info("Kafka message delivered to topic name : %s [partition %s]", msg.topic(), msg.partition())

def publish_event(event_dict: dict):
    try:
        payload = json.dumps(event_dict, default=str).encode("utf-8")
        logger.info("Publishing to Kafka topic '%s' (%d bytes)", TOPIC, len(payload))
        producer.produce(
            TOPIC,
            payload,
            callback=delivery_report
        )
        producer.flush()
    except Exception as e:
        logger.error("Failed to publish to Kafka: %s", str(e))
        raise