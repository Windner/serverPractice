package candice.good.scala.api

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.inject.Logging
import candice.good.scala.mongodb.MongodbManager
import org.mongodb.scala.Document
import org.mongodb.scala.bson.BsonElement

import scala.collection.mutable

/**
  * Created by MIYAFENG1 on 2016/6/7.
  */
class BookRecord extends Controller with Logging{

  val db = mutable.Map[String, List[Book]]()
  val mongodbManager = MongodbManager()

  get("/book") { request: Request =>
    info("get all books....")
    val allItems = mongodbManager.show
    val stringBuilder = StringBuilder.newBuilder

    allItems.foreach(
      user => {
        stringBuilder.append(user.toJson().toString)
      }
    )

    stringBuilder.toString()
  }

  //create
  post("/book") { book: Book =>
    info(s"""create ISBN: '${book.ISBN}'""")
    if (mongodbManager.create(mongodbManager.getDocFromBook(book)))
      {
        response.created(mongodbManager.query("ISBN",book.ISBN).toJson().toString).location(s"/book/${book.ISBN}")
      }
    else
      response.badRequest("Create book fail.")
  }

  //Read
  get("/book/:ISBN") { request: Request =>
      info(s"""get ISBN: '${request.params("ISBN")}'""")
      "[" + mongodbManager.query("ISBN",request.params("ISBN")).toJson().toString.toLowerCase() + "]"

  }

  //Update
  put("/book/:ISBN") { book : Book =>
    info(s"""update ISBN: '${book.ISBN}'""")
    if (mongodbManager.update(book.ISBN, mongodbManager.getDocFromBook(book)))
      response.ok.location(s"/book/${book.ISBN}")
    else
      response.badRequest(s"Update Book:ISBN ${book.ISBN} fail.")
    }

  //Delete
  delete("/book/:ISBN") { request:Request =>
    info(s"""delete book '${request.params("ISBN")} ...""")
    if (mongodbManager.delete("ISBN",request.params("ISBN")))
      response.ok("Delete successfully.")
    else
      response.badRequest(s"Delete Book:ISBN ${request.params("ISBN")} fail.")
  }

  //clean
  post("/clean") { request:Request =>
    info("clean collection")
    mongodbManager.clean
    response.ok
  }
}

case class Book(
                 ISBN: String,
                 NAME: String,
                 AUTHOR: String
               )