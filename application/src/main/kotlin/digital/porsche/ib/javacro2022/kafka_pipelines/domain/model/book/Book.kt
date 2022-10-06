package digital.porsche.ib.javacro2022.kafka_pipelines.domain.model.book

import javax.persistence.*

@Entity
@Table(name = "books")
class Book (
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long,

    val isbn: String,

    val title: String,

    val author: String
)