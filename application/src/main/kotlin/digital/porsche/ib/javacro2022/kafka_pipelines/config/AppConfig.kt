package digital.porsche.ib.javacro2022.kafka_pipelines.config


import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.KafkaAdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.util.*
import javax.annotation.PostConstruct

@Configuration
@EnableConfigurationProperties(AppProperties::class)
class AppConfig(
    private val appProperties: AppProperties
) {

    @PostConstruct
    fun init(){
        val props = Properties()
        props[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"

        val adminClient = KafkaAdminClient.create(props)
        try {
            val result = adminClient.createTopics(listOf(
                NewTopic(appProperties.booksTopic, 3, 1),
                NewTopic(appProperties.bookPurchasesTopic, 3, 1),
                NewTopic(appProperties.bookPurchasesCountTopic, 3, 1),
                NewTopic(appProperties.bookRatingsTopic, 3, 1)
            ))
            result.all().get()
        } catch (e:Exception){
            e.printStackTrace()
        }
        finally {
            adminClient.close()
        }

    }

}