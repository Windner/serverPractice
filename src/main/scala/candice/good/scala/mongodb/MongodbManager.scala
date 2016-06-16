package candice.good.scala.mongodb

/**
  * Created by MIYAFENG1 on 2016/6/14.
  */

import java.util.concurrent.TimeUnit

import candice.good.scala.api.Book
import com.twitter.inject.Logging
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala._
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

  val KEY_ISBN = "ISBN"
  val KEY_NAME = "NAME"
  val KEY_AUTHOR = "AUTHOR"

  // Use a Connection String
  val mongoClient: MongoClient = MongoClient(MONGODB_URI)
  val database: MongoDatabase = mongoClient.getDatabase(MONGODB_NAME)
  // collection == table
  val collection: MongoCollection[Document] = database.getCollection(MONGODB_COLLECTION_NAME);

  //get
  def query (key: String, value: String) : Document = {
    val queryFuture = collection.find(equal(key, value)).projection(excludeId()).toFuture()
    Await.result(queryFuture, Duration(10, TimeUnit.SECONDS)).head
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
    info(s"getDocFromBook: '${book.ISBN}'")
    Document(
      KEY_ISBN -> book.ISBN,
      KEY_NAME -> book.NAME,
      KEY_AUTHOR -> book.AUTHOR)
  }
}

object MongodbManager {
  private val mongodbManager = new MongodbManager

  def apply() = mongodbManager
}