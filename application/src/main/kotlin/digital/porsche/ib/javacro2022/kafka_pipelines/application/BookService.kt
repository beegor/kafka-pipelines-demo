package digital.porsche.ib.javacro2022.kafka_pipelines.application

interface BookService {

    fun bookPurchesed(isbn: String)

    fun bookRated(isbn: String, rating: Int)

}