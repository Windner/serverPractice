package candice.good.scala.api

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.inject.Logging
import candice.good.scala.mongodb.MongodbManager
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.util.{Future => TwitterFuture}
import com.twitter.bijection.Conversion._
import com.twitter.bijection.twitter_util.UtilBijections.twitter2ScalaFuture

import com.twitter.bijection._
import scala.concurrent.{Future => ScalaFuture}


/**
  * Created by MIYAFENG1 on 2016/6/7.
  */
class BookRecord extends Controller with Logging{
  private[this] val jsonMapper = {
    val m = new ObjectMapper()
    m.registerModule(DefaultScalaModule)
  }

  val db = mutable.Map[String, List[Book]]()
  val mongodbManager = MongodbManager

  //List all
  get("/book") { request: Request =>
    info("get all books....")
    val stringBuilder = StringBuilder.newBuilder
    val showResult = (for {
      allItems <- mongodbManager.show
    } yield {
      allItems.foreach(doc => {println(doc); stringBuilder.append(doc.toJson().toString())} )
      if (allItems != Nil)
        response.ok(stringBuilder.toString())
      else response.noContent
    }).recover {
      case e: Exception =>
        println(e.getMessage)
        response.internalServerError
    }

    Bijection[ScalaFuture[ResponseBuilder#EnrichedResponse], TwitterFuture[ResponseBuilder#EnrichedResponse]](showResult)
  }

  //create
  //TODO: query isbn; if not exist => create; else return duplicate; encode
  post("/book") { book :Book =>
    info(s"""create ISBN: '${book.isbn}'""")
    val createResult = ( for {
      result <- mongodbManager.create(mongodbManager.getDocFromBook(book))
      getNewBook <- mongodbManager.query(mongodbManager.KEY_ISBN, book.isbn)
    } yield {
      if (result)
        response.created(getNewBook.toJson().toString).location(s"/book/${book.isbn}")
      else
        response.badRequest
    }).recover {
      case e:Exception => response.badRequest(e.toString)
    }

    Bijection[ScalaFuture[ResponseBuilder#EnrichedResponse], TwitterFuture[ResponseBuilder#EnrichedResponse]](createResult)
  }

  //Read
  get("/book/:ISBN") { request: Request =>
    info(s"""get ISBN: '${request.params("ISBN")}'""")
    val queryResult = (for {
      r <- mongodbManager.query(mongodbManager.KEY_ISBN,request.params("ISBN"))
    }yield
      {
        response.ok(r.toJson().toString)
      }).recover {
      case e: NoSuchElementException => response.noContent
      case e: Exception =>
        println(s" get exception: ${e.getMessage}")
        response.internalServerError
    }

    //queryResult will print response message directly. Success(Response("HTTP/1.1 Status(200)"))
    //ResponseBuilder#EnrichedResponse] => '#' usage: EnrichedResponse is a (object\case class\sub class) of ResponseBuilder
    //queryResult.as[TwitterFuture[ResponseBuilder#EnrichedResponse]]
    Bijection[ScalaFuture[ResponseBuilder#EnrichedResponse], TwitterFuture[ResponseBuilder#EnrichedResponse]](queryResult)

  }

  //Update
  //TODO: show update book; encode
  put("/book/:ISBN") { book: Book =>
    info(s"""update ISBN: '${book.isbn}'""")
    val putResult = (for {
      r <- mongodbManager.update(book.isbn, mongodbManager.getDocFromBook(book))
    } yield {
      if (r)
        response.ok.location(s"/book/${book.isbn}")
      else
        response.badRequest(s"Update Book:ISBN ${book.isbn} fail.")
    }).recover {
      case e: Exception =>
        println(s" get exception: ${e.getMessage}")
        response.badRequest
    }
    Bijection[ScalaFuture[ResponseBuilder#EnrichedResponse], TwitterFuture[ResponseBuilder#EnrichedResponse]](putResult)
  }

  //Delete
  delete("/book/:ISBN") { request:Request =>
    info(s"""delete book '${request.params("ISBN")} ...""")
    val deleteResult = (for {
      result <- mongodbManager.delete(mongodbManager.KEY_ISBN,request.params("ISBN"))
    } yield {
      if (result)
        response.ok("Delete successfully.")
      else
        response.badRequest(s"Delete Book:ISBN ${request.params("ISBN")} fail.")
    }).recover {
      case e: Exception =>
        println(s" get exception: ${e.getMessage}")
        response.internalServerError
    }

    Bijection[ScalaFuture[ResponseBuilder#EnrichedResponse], TwitterFuture[ResponseBuilder#EnrichedResponse]](deleteResult)
  }

  //patch
  //TODO: show updated book data; encode
  patch("/book/:ISBN") { request: Request =>
    info("patch book")
    info(s"""get request ${request.params("ISBN")}""")
    val template:Book = jsonMapper.readValue(request.getContentString(), classOf[Book])
    //https://github.com/savaki/todo-backend-finatra/blob/master/src/main/scala/todo/controller/TodoController.scalahttps://github.com/savaki/todo-backend-finatra/blob/master/src/main/scala/todo/controller/TodoController.scala
    val patchResult =  (for {
      updates <- mongodbManager.getUpdateStrFromBook(template)
      r <- mongodbManager.patch(updates, request.params("ISBN"))
    } yield {
      response.ok
    }).recover {
      case e: Exception =>
        response.badRequest.location(s"/book/${request.params("ISBN")}")
    }
    Bijection[ScalaFuture[ResponseBuilder#EnrichedResponse], TwitterFuture[ResponseBuilder#EnrichedResponse]](patchResult)
  }

  //clean
  post("/clean") { request:Request =>
    info("clean collection")
    mongodbManager.clean
    response.ok
  }
}

case class Book(
                 isbn: String,
                 name: String,
                 author: String
               )