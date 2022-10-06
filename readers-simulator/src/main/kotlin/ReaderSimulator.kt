import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodySubscribers
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.random.Random

class ReaderSimulator {

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    val stopped = AtomicBoolean(false)

    private val isbns = arrayOf(
        "0000000001",
        "0000000002",
        "0000000003",
        "0000000004",
        "0000000005",
        "0000000006",
        "0000000007",
        "0000000008",
        "0000000009",
        "0000000010"
    )

    init {
        Runtime.getRuntime().addShutdownHook(
            thread(false) {
                stopped.set(true)
            }
        )
    }

    fun start(){
        thread {
            while (!stopped.get()) {
                simulatePurchase()
                simulateRating()
                Thread.sleep(100)
            }
        }
    }

    fun simulatePurchase(){
        println("Sending purchase request")
        val randomIsbn = isbns[ Random.nextInt(0, isbns.size) ]
        val url = URI("http://localhost:8080/book/purchase/$randomIsbn")
        val request = HttpRequest.newBuilder(url).GET().build()
        httpClient.sendAsync(request) { resp ->
            println("Purchase response received: $resp")
            BodySubscribers.discarding()
        }
    }

    fun simulateRating(){
        println("Sending rating request")
        val randomIsbn = isbns[ Random.nextInt(0, isbns.size) ]
        val randomRating = Random.nextInt() % 5 + 1
        val url = URI("http://localhost:8080/book/rate/$randomIsbn/$randomRating")
        val request = HttpRequest.newBuilder(url).GET().build()
        httpClient.sendAsync(request) { resp ->
            println("Rating response received: $resp")
            BodySubscribers.discarding()
        }
    }

}

fun main() {
    ReaderSimulator().start()
}