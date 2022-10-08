package digital.porsche.ib.javacro2022.kafka_pipelines.application.impl

import digital.porsche.ib.javacro2022.kafka_pipelines.config.AppProperties
import digital.porsche.ib.javacro2022.kafka_pipelines.domain.model.book.BookPurchaseCountRecord
import digital.porsche.ib.javacro2022.kafka_pipelines.domain.model.book.BookPurchaseRecord
import digital.porsche.ib.javacro2022.kafka_pipelines.domain.model.book.BookRecord
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.common.serialization.Serdes.StringSerde
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.TimeWindows
import org.apache.kafka.streams.kstream.Windowed
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Service
import java.time.Duration
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy


@Service
@DependsOn("appConfig")
class BookPurchaseProcessor(
    private val appProperties: AppProperties
) {

    lateinit var streams:KafkaStreams

    @PostConstruct
    fun initialize() {
        val config = StreamsConfig(
            mapOf(
                StreamsConfig.APPLICATION_ID_CONFIG to "book_purchase_processor_v1",
                StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
                StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to StringSerde::class.java,
                StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to SpecificAvroSerde::class.java,
                StreamsConfig.COMMIT_INTERVAL_MS_CONFIG to 1000,
                "schema.registry.url" to "http://localhost:8081"
            )
        )
        streams = KafkaStreams(createTopology(), config)
        streams.start()
    }

    fun createTopology(): Topology {
        val builder = StreamsBuilder() // create the topology builder

        val bookPurchaseStream =  builder.stream<String, BookPurchaseRecord>(appProperties.bookPurchasesTopic)

        val timeWindowedCount = bookPurchaseStream
            .groupByKey()
            .windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofMinutes(1), Duration.ofSeconds(10)))
            .count()

        val purchaseCountRecordStream = timeWindowedCount
            .toStream()
            .map { key, value ->
                val isbn = key.key()
                val windowStart = key.window().start()
                KeyValue(isbn, BookPurchaseCountRecord(BookRecord(), value, windowStart))
            }

        val booksTable = builder.table<String, BookRecord>(appProperties.booksTopic)
        purchaseCountRecordStream
            .join ( booksTable ) { pcr, book ->
                BookPurchaseCountRecord(book, pcr.getCount(), pcr.getWindowStart())
            }
            .map { key, value -> KeyValue("$key-${value.getWindowStart()}", value) }
            .to(appProperties.bookPurchasesCountTopic)

        return builder.build()
    }

    @PreDestroy
    fun destroy() {
        streams.close()
    }

}