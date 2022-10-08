package digital.porsche.ib.javacro2022.kafka_pipelines.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "javacro22.demo")
class AppProperties(
    val booksTopic: String,
    val bookPurchasesTopic: String,
    val bookPurchasesCountTopic: String,
    val bookRatingsTopic: String,
)