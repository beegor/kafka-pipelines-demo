package digital.porsche.ib.javacro2022.kafka_pipelines.interfaces

import digital.porsche.ib.javacro2022.kafka_pipelines.application.BookService
import digital.porsche.ib.javacro2022.kafka_pipelines.domain.model.book.BookRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*


@Controller
@RequestMapping("/book")
class BookController(
    private val bookRepository: BookRepository,
    private val bookService: BookService
) {

    @GetMapping()
    fun listBooks(pageable: Pageable, model: Model): String {
        val books =  bookRepository.findAll(pageable)
        model.addAttribute("booksPage", books)
        return "books"
    }


    @PostMapping("/purchase/{isbn}")
    @ResponseBody
    fun purchaseBook(@PathVariable isbn: String) {
        bookService.bookPurchesed(isbn)
    }

    @PostMapping("/rate/{isbn}/{rating}")
    @ResponseBody
    fun rateBook(
        @PathVariable isbn: String,
        @PathVariable rating: Int
    ) {
        bookService.bookRated(isbn, rating)
    }



}