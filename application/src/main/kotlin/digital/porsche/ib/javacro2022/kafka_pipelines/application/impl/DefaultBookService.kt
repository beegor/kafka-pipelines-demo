package digital.porsche.ib.javacro2022.kafka_pipelines.application.impl

import digital.porsche.ib.javacro2022.kafka_pipelines.application.BookService
import digital.porsche.ib.javacro2022.kafka_pipelines.config.AppProperties
import digital.porsche.ib.javacro2022.kafka_pipelines.domain.model.book.BookPurchaseRecord
import digital.porsche.ib.javacro2022.kafka_pipelines.domain.model.book.BookRatingRecord
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.specific.SpecificData
import org.apache.avro.specific.SpecificRecordBase
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseBody
import java.util.UUID

@Service
class DefaultBookService(
    private val appProperties: AppProperties
) : BookService {

    private final val producer: KafkaProducer<String, SpecificRecordBase>

    init {
        val config = mapOf (
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            "schema.registry.url" to "http://localhost:8081"
        )
        producer = KafkaProducer(config)
    }

    override fun bookPurchesed(isbn: String) {
        val purchaseRecord = BookPurchaseRecord(isbn, UUID.randomUUID().toString())
        producer.send( ProducerRecord(appProperties.bookPurchasesTopic, isbn, purchaseRecord))
    }

    override fun bookRated(isbn: String, rating: Int) {
        val ratingRecord = BookRatingRecord(isbn, UUID.randomUUID().toString(), rating)
        producer.send( ProducerRecord(appProperties.bookRatingsTopic, isbn, ratingRecord))
    }
}