package candice.good.scala.mongodb

/**
  * Created by MIYAFENG1 on 2016/6/14.
  */

import java.util.concurrent.TimeUnit

import candice.good.scala.api.Book
import com.twitter.inject.Logging
import org.bson.BsonDocumentWriter
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala._
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.UpdateOptions

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class MongodbManager private extends Logging{

  //define
  val MONGODB_URI = "mongodb://localhost"
  val MONGODB_NAME = "db"
  val MONGODB_COLLECTION_NAME = "BookRecord"

  val DEFAULT_AWAIT_TIMEOUT = 10
  val DEFAULT_AWAIT_TIME_UNIT = TimeUnit.SECONDS

  val KEY_ISBN = "isbn"
  val KEY_NAME = "name"
  val KEY_AUTHOR = "author"

  // Use a Connection String
  val mongoClient: MongoClient = MongoClient(MONGODB_URI)
  val database: MongoDatabase = mongoClient.getDatabase(MONGODB_NAME)
  // collection == table
  val collection: MongoCollection[Document] = database.getCollection(MONGODB_COLLECTION_NAME);

  //get
  def query (key: String, value: String) : Document = {
    val returnDoc:Document = try {
      val queryFuture = collection.find(equal(key, value)).projection(excludeId()).toFuture()
      Await.result(queryFuture, Duration(10, TimeUnit.SECONDS)).head
    }
    catch {
      case e: Exception =>
        println(e.toString)
        null
    }
    returnDoc
  }

  //create
  def create (document: Document): Boolean = {
    val createResult =
      try {
        val insertFuture = collection.insertOne(document).toFuture()
        Await.result(insertFuture, Duration(DEFAULT_AWAIT_TIMEOUT, DEFAULT_AWAIT_TIME_UNIT))
        true
      }
    catch {
      case ex: Exception => {
        println(ex.getMessage)
      }
        false
    }
    createResult
  }

  //update
  def update (value: String, document: Document): Boolean = {
    val actionResult =
      try {
        val updateOpts = (new UpdateOptions()).upsert(true)
        val updateFuture = collection.replaceOne(equal(KEY_ISBN,value), document, updateOpts).toFuture()
        Await.result(updateFuture,  Duration(DEFAULT_AWAIT_TIMEOUT, DEFAULT_AWAIT_TIME_UNIT))
        true
      }
    catch {
        case ex: Exception => {
          println(ex.getMessage)
        }
        false
    }
        actionResult
  }

  //update partial
  def patch (updates: BsonDocument, id:String): String = {
    val actionResult =
      try {
        val updateFuture = collection.findOneAndUpdate(equal(KEY_ISBN, id), updates).toFuture() // Not found: return null
        //val updateFuture = collection.updateOne(equal(KEY_ISBN, id), updates).toFuture() //Not found: return result
        Await.result(updateFuture,Duration(DEFAULT_AWAIT_TIMEOUT, DEFAULT_AWAIT_TIME_UNIT)).head.toString()
      }
      catch {
        case ex: Exception => {
        println (ex.getMessage)
        "Update fail"
          }
      }
    actionResult
  }

  //delete
  def delete (key: String, value: String): Boolean = {
    val actionResult =
      try {
        val deleteFuture = collection.deleteOne(equal(key,value)).toFuture()
        val deleteCount = Await.result(deleteFuture,  Duration(DEFAULT_AWAIT_TIMEOUT, DEFAULT_AWAIT_TIME_UNIT)).head.getDeletedCount
        if (deleteCount > 0) true else false
      }
    catch {
        case ex: Exception => {
          println(ex.getMessage)
        }
        false
    }
      actionResult
  }

  //show
  def show : List[Document] = {
    val queryFuture = collection.find().projection(excludeId()).toFuture()
    Await.result(queryFuture, Duration(DEFAULT_AWAIT_TIMEOUT, DEFAULT_AWAIT_TIME_UNIT)).toList
  }

  //clean collection
  def clean: Unit = {
    info("mongodb drop collection")
    Await.result(collection.drop().toFuture(), Duration(DEFAULT_AWAIT_TIMEOUT, DEFAULT_AWAIT_TIME_UNIT))
  }

  //close
  def close: Unit = {
    mongoClient.close()
  }

  //util
  def getDocFromBook(book: Book): Document = {
    //info(s"getDocFromBook: '${book.ISBN}'")
    Document(
      KEY_ISBN -> book.isbn,
      KEY_NAME -> book.name,
      KEY_AUTHOR -> book.author)
  }

  //util
  def getDocFromUpdateElement(updateElement: UpdateElement): Document = {
    Document(
      "KEY" -> updateElement.KEY,
      "VALUE" -> updateElement.VALUE
    )
  }

  //JAVA, reference: AbstractBsonWriter.java
  def getUpdateStrFromBook(book: Book): BsonDocument = {
    info("get update string")
    val writer: BsonDocumentWriter = new BsonDocumentWriter(new BsonDocument)

    writer.writeStartDocument
    writer.writeName("$set")
    writer.writeStartDocument
    book.productElement(0) match {
      case null => println("ISBN is null")
      case isbn:String => {
        writer.writeName(KEY_ISBN)
        writer.writeString(book.isbn)
      }
    }

    book.productElement(1) match {
      case null => println("NAME is null")
      case name:String => {
        writer.writeName(KEY_NAME)
        writer.writeString(book.name)
      }
    }

    book.productElement(2) match {
      case null => println("Author is null")
      case author:String => {
        writer.writeName(KEY_AUTHOR)
        writer.writeString(book.author)
      }
    }

    writer.writeEndDocument
    writer.writeEndDocument
    println(writer.getDocument.toString)
    writer.getDocument
  }
}

case class UpdateElement (
  KEY: String,
  VALUE: String
  )

object MongodbManager {
  private val mongodbManager = new MongodbManager

  def apply() = mongodbManager
}
