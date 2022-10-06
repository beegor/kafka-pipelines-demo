package digital.porsche.ib.javacro2022.kafka_pipelines.domain.model.book

import org.springframework.data.jpa.repository.JpaRepository

interface BookRepository: JpaRepository<Book, String>