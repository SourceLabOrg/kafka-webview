package com.darksci.kafkaview.manager.kafka;

import com.darksci.kafkaview.manager.kafka.dto.KafkaResult;
import com.darksci.kafkaview.manager.kafka.dto.KafkaResults;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TransactionalKafkaClient {
    private final static Logger logger = LoggerFactory.getLogger(TransactionalKafkaClient.class);
    private final static long TIMEOUT = 5000L;

    private final KafkaConsumer kafkaConsumer;

    public TransactionalKafkaClient(final KafkaConsumer kafkaConsumer) {
        this.kafkaConsumer = kafkaConsumer;
    }

    public KafkaResults consume() {
        final List<KafkaResult> kafkaResultList = new ArrayList<>();
        final ConsumerRecords consumerRecords = kafkaConsumer.poll(TIMEOUT);

        logger.info("Consumed {} records", consumerRecords.count());
        final Iterator<ConsumerRecord> recordIterator = consumerRecords.iterator();
        while (recordIterator.hasNext()) {
            final ConsumerRecord consumerRecord = recordIterator.next();
            kafkaResultList.add(
                new KafkaResult(
                    consumerRecord.partition(),
                    consumerRecord.offset(),
                    consumerRecord.key(),
                    consumerRecord.value()
                )
            );
        }
        return new KafkaResults(kafkaResultList);
    }

    public void close() {
        kafkaConsumer.close();
    }
}
