package candice.good.scala.api

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.inject.Logging
import candice.good.scala.mongodb.MongodbManager
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.mongodb.scala.Document

import scala.collection.mutable


/**
  * Created by MIYAFENG1 on 2016/6/7.
  */
class BookRecord extends Controller with Logging{
  private[this] val jsonMapper = {
    val m = new ObjectMapper()
    m.registerModule(DefaultScalaModule)
  }

  val db = mutable.Map[String, List[Book]]()
  val mongodbManager = MongodbManager()

  //List all
  get("/book") { request: Request =>
    info("get all books....")
    val allItems = mongodbManager.show
    val stringBuilder = StringBuilder.newBuilder

    allItems.foreach(
      user => {
        stringBuilder.append(user.toJson().toString)
      }
    )
    if (stringBuilder.toString().isEmpty) response.noContent else response.ok(stringBuilder.toString())
  }

  //create
  post("/book") { book :Book =>
    try
      {
        info(s"""create ISBN: '${book.isbn}'""")
        if (mongodbManager.create(mongodbManager.getDocFromBook(book)))
        {
          response.created(mongodbManager.query(mongodbManager.KEY_ISBN,book.isbn).toJson().toString).location(s"/book/${book.isbn}")
        }
        else
          response.badRequest("Create book fail.")
      }
    catch
      {
        case e:Exception => response.badRequest(e.toString)
      }
  }

  //Read
  get("/book/:ISBN") { request: Request =>
      info(s"""get ISBN: '${request.params("ISBN")}'""")
      mongodbManager.query(mongodbManager.KEY_ISBN,request.params("ISBN")) match {
        case null => response.noContent
        case result:Document => result.toJson.toString
      }

  }

  //Update
  put("/book/:ISBN") { book : Book =>
    info(s"""update ISBN: '${book.isbn}'""")
    if (mongodbManager.update(book.isbn, mongodbManager.getDocFromBook(book)))
      response.ok.location(s"/book/${book.isbn}")
    else
      response.badRequest(s"Update Book:ISBN ${book.isbn} fail.")
    }

  //Delete
  delete("/book/:ISBN") { request:Request =>
    info(s"""delete book '${request.params("ISBN")} ...""")
    if (mongodbManager.delete(mongodbManager.KEY_ISBN,request.params("ISBN")))
      response.ok("Delete successfully.")
    else
      response.badRequest(s"Delete Book:ISBN ${request.params("ISBN")} fail.")
  }

  //patch
  patch("/book/:ISBN") { request: Request =>
    info("patch book")
    //https://github.com/savaki/todo-backend-finatra/blob/master/src/main/scala/todo/controller/TodoController.scalahttps://github.com/savaki/todo-backend-finatra/blob/master/src/main/scala/todo/controller/TodoController.scala
    try{
      val template: Book = jsonMapper.readValue(request.getContentString(), classOf[Book])
      info(s"""get request ${request.params("ISBN")}""")
      val updates = mongodbManager.getUpdateStrFromBook(template)
      mongodbManager.patch(updates, request.params("ISBN"))
      response.ok
    }
    catch {
      case e: Exception =>
        response.badRequest.location(s"/book/${request.params("ISBN")}")
    }

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