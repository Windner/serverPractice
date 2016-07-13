package candice.good.scala.mongodb

/**
  * Created by MIYAFENG1 on 2016/6/14.
  */

import java.util.NoSuchElementException
import java.util.concurrent.TimeUnit

import candice.good.scala.api.Book
import com.twitter.inject.Logging
import com.typesafe.config.ConfigFactory
import org.bson.BsonDocumentWriter
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala._
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.UpdateOptions

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object MongodbManager extends Logging{

  //define
  //val MONGODB_URI = "mongodb://localhost"
  val MONGODB_NAME = "db"
  val MONGODB_COLLECTION_NAME = "BookRecord"

  val DEFAULT_AWAIT_TIMEOUT = 10
  val DEFAULT_AWAIT_TIME_UNIT = TimeUnit.SECONDS

  val KEY_ISBN = "isbn"
  val KEY_NAME = "name"
  val KEY_AUTHOR = "author"

  //load configure file
  // TODO: need to new a class to handle
  val mongodbConf = ConfigFactory.load
  val root = mongodbConf.getConfig("mongodb")

  val MONGODB_ADDRESS = root.getString("hostAddress")
  val MONGODB_PORT = root.getString("hostPort")

  // Use a Connection String
  val mongoClient: MongoClient = MongoClient(s"mongodb://${MONGODB_ADDRESS}:${MONGODB_PORT}")
  val database: MongoDatabase = mongoClient.getDatabase(MONGODB_NAME)
  // collection == table
  val collection: MongoCollection[Document] = database.getCollection(MONGODB_COLLECTION_NAME);

  //TODO: test index: list Index
  //collection.createIndex(compoundIndex(ascending("x"), descending("y"))) => TODO:what dose this do?
  collection.createIndex(Document(KEY_ISBN -> 1))

  //get
  def query (key: String, value: String) : Future[Document] = (for {
    queryFuture <- collection.find(equal(key, value)).projection(excludeId()).toFuture()
  } yield queryFuture.head)
    .recover {
      case e:NoSuchElementException => throw new NoSuchElementException(e.getMessage)
      case e:Exception => {
        println(e.getMessage)
        throw new Exception(e)
      }
        Document.empty
    }

  //create
  def create (document: Document): Future[Boolean] = ( for {
    //TODO: validate document format, can't be empty document
        insertFuture <- collection.insertOne(document).toFuture()
        list <- collection.listIndexes().toFuture()
      } yield {
      list.foreach(doc => println(doc))

      true}).recover {
    case ex:DuplicateKeyException =>throw new Exception(ex)
    case ex: Exception => {
      println(ex.getMessage)
      throw new Exception(ex)
      false
    }
  }

  val updateOpts = (new UpdateOptions()).upsert(true)
  //update(replace)
  def update (value: String, document: Document): Future[Boolean] = (for {
        updateFuture <- collection.replaceOne(equal(KEY_ISBN,value), document, updateOpts).toFuture()
      } yield true).recover {
          case ex: Exception => {
          println(ex.getMessage)
        }
        false
    }

  //update partial
  def patch (updates: BsonDocument, id:String): Future[Boolean] = (for {
      updateFuture <- collection.findOneAndUpdate(equal(KEY_ISBN, id), updates).toFuture() // Not found: return null
  } yield {
    true
  }).recover {
    case ex: Exception => {
      println(ex.getMessage)
      throw new Exception(ex)
      false
    }
  }

  //delete
  def delete (key: String, value: String): Future[Boolean] = (for {
    deleteFuture <- collection.deleteOne(equal(key, value)).toFuture()
  } yield {
    if ( deleteFuture.head.getDeletedCount > 0 ) true else false
  }).recover {
    case ex: Exception => {
      println(ex.getMessage)
      throw new Exception(ex)
      false
    }
  }

  //show
  def show : Future[List[Document]] = (for {
    queryFuture <- collection.find().projection(excludeId()).toFuture()

  } yield {
    //queryFuture.toList.foreach(data =>println(data.toJson().toString))
    queryFuture.toList
  }).recover {
    case e:Exception => println(e.getMessage)
      Nil
  }

  //clean collection
  def clean: Unit = Future {
    info("mongodb drop collection")
    collection.drop()
  }

  //close
  def close: Unit = Future {
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


  //JAVA, reference: AbstractBsonWriter.java
  //TODO: new a file to handle all operations
  def getUpdateStrFromBook(book: Book): Future[BsonDocument] = Future {
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

/*
object MongodbManager {
  private val mongodbManager = new MongodbManager

  def apply() = mongodbManager
}
*/