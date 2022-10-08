package digital.porsche.ib.javacro2022.kafka_pipelines

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaPipelinesApplication

fun main(args: Array<String>) {
	runApplication<KafkaPipelinesApplication>(*args)
}
