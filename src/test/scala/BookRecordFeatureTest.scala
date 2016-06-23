import candice.good.scala.AAAServer
import candice.good.scala.api.Book
import com.twitter.finagle.http.Status
import com.twitter.finatra.http.test.EmbeddedHttpServer
import com.twitter.inject.server.FeatureTest
import com.google.gson.Gson

/**
  * Created by MIYAFENG1 on 2016/6/8.
  */
class BookRecordFeatureTest extends FeatureTest{
  override val server = new EmbeddedHttpServer(
    twitterServer = new AAAServer
  )
  val gson: Gson = new Gson()

  "BookRecord" should {

    "POST" in {
      val book:Book = new Book (isbn = "1", name = "Scala", author = "lalala")
      val bookJson = gson.toJson(book,classOf[Book])
      val response = server.httpPost(
        path = "/book",
        postBody = """
                     |{
                     |"isbn":"1",
                     |"name":"Scala",
                     |"author":"lalala"
                     |}
                   """.stripMargin,
        andExpect = Status.Created,
        withLocation = "/book/1"
      )
      server.httpGetJson[Book](
        path = response.location.get,
        withJsonBody = bookJson
      )
    }

    "POST(Bad request)" in {
      server.httpPost(
        path = "/book",
        postBody =
          """
            |{
            |"isbn":"3",
            |"name":"scala"
            |}
          """.stripMargin,
        andExpect = Status.InternalServerError
      )

    }

    "GET:List all book for a user when GET request is made" in {
       server.httpPost(
        path = "/book",
        postBody =
          """
            |{
            |"isbn":"2",
            |"name":"design patten",
            |"author":"god"
            |}
          """.stripMargin,
        andExpect = Status.Created
      )

      val book1:Book = new Book (isbn = "1", name = "Scala", author = "lalala")
      val book2:Book = new Book (isbn = "2", name = "design patten", author = "god")

      server.httpGetJson[List[Book]](
        path = "/book",
        andExpect = Status.Ok,
        withBody = s"[${gson.toJson(book1, classOf[Book])}, ${gson.toJson(book2, classOf[Book])}]"
      )
    }

    "GET: All but no content" in {

      server.httpPost(
        path = "/clean",
        andExpect = Status.Ok,
        postBody = ""
      )

      server.httpGet(
        path = "/book",
        andExpect = Status.NoContent
      )

    }

    "GET: One but no content" in {

      server.httpGet(
        path = "/book/7",
        andExpect = Status.NoContent
      )
    }

    "UPDATE: Update book" in {

      val book:Book = new Book (isbn = "6", name = "design patten", author = "superman")
      val bookJson = gson.toJson(book,classOf[Book])
      server.httpPost(
        path = "/book",
        postBody =
          """
            |{
            |"isbn":"6",
            |"name":"design patten",
            |"author":"god"
            |}
          """.stripMargin,
        andExpect = Status.Created
      )

      val response = server.httpPut(
        path = "/book/6",
        putBody =
          """
            |{
            |"isbn":"6",
            |"name":"design patten",
            |"author":"superman"
            |}
          """.stripMargin,
        andExpect = Status.Ok
      )

      server.httpGetJson[Book](
        path = response.location.get,
        andExpect = Status.Ok,
        withJsonBody = bookJson
      )
    }


    "UPDATE(Bad request)" in {
      server.httpPut(
        path = "/book/3",
        putBody =
          """
            |{
            |"isbn":"3",
            |"name":"scala"
            |}
          """.stripMargin,
        andExpect = Status.InternalServerError
      )
    }

    "DELETE: Delete book" in {
      server.httpPost(
        path = "/book",
        postBody =
          """
            |{
            |"isbn":"5",
            |"name":"design patten",
            |"author":"god"
            |}
          """.stripMargin,
        andExpect = Status.Created
      )
      val response = server.httpDelete(
        path = "/book/5",
        andExpect = Status.Ok,
        deleteBody = "Delete successfully."
      )
    }

    "DELETE(Not found): Not exist" in {
      server.httpDelete(
        path = "/book/5",
        andExpect = Status.BadRequest,
        deleteBody = "Delete Book:ISBN 5 fail."
      )
    }

    "PATCH: Update a specific element" in {
      val response = server.httpPost(
        path = "/book",
        postBody = """
                     |{
                     |"isbn":"9",
                     |"name":"clean code",
                     |"author":"HTC"
                     |}
                   """.stripMargin,
        andExpect = Status.Created,
        withLocation = "/book/9"
      )

      server.httpPatch(
        path = response.location.get,
        patchBody =
          """
            |{
            |"author": "htc"
            |}
          """.stripMargin,
        andExpect = Status.Ok
      )

      val book:Book = new Book (isbn = "9", name = "clean code", author = "htc")
      val bookJson = gson.toJson(book,classOf[Book])

      server.httpGetJson[Book](
        path = response.location.get,
        andExpect = Status.Ok,
        withJsonBody =bookJson

      )
    }

    "PATCH: Update some specific elements" in {
      server.httpPatch(
        path = "/book/9",
        patchBody =
          """
            |{
            |"name":"TCP/IP",
            |"author": "HTC"
            |}
          """.stripMargin,
        andExpect = Status.Ok
      )

      val book:Book = new Book (isbn = "9", name = "TCP/IP", author = "HTC")
      val bookJson = gson.toJson(book,classOf[Book])

      server.httpGetJson[Book](
        path = "/book/9",
        andExpect = Status.Ok,
        withJsonBody =bookJson

      )

    }

    "PATCH(Bad request)" in {
      server.httpPatch(
        path = "/book/9",
        patchBody =
          """
            |{
            |"name":"TCP/IP"
            |"author": "HTC"
            |}
          """.stripMargin,
        andExpect = Status.BadRequest
      )

    }

    "clean collection" in {
      server.httpPost(
        path = "/clean",
        postBody = "",
        andExpect = Status.Ok
      )

      server.httpGet(
        path = "/book",
        andExpect = Status.NoContent,
        withBody = ""
      )
    }
  }
}
