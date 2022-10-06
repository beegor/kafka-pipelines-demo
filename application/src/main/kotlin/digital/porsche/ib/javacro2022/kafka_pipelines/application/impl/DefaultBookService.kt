package digital.porsche.ib.javacro2022.kafka_pipelines.application.impl

import digital.porsche.ib.javacro2022.kafka_pipelines.application.BookService
import digital.porsche.ib.javacro2022.kafka_pipelines.domain.model.book.BookPurchaseRecord
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DefaultBookService : BookService {

    private final val producer: KafkaProducer<String, BookPurchaseRecord>

    init {
        val config = mapOf (
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            "schema.registry.url" to "localhost:8081"
        )
        producer = KafkaProducer(config)
    }

    override fun bookPurchesed(isbn: String) {
        val purchaseRecord = BookPurchaseRecord(isbn, UUID.randomUUID().toString())
        producer.send( ProducerRecord("book_purchases", isbn, purchaseRecord))
    }

    override fun bookRated(isbn: String, rating: Int) {
        TODO("Not yet implemented")
    }
}