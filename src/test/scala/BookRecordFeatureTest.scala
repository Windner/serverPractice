import candice.good.scala.AAAServer
import candice.good.scala.api.Book
import com.twitter.finagle.http.Status
import com.twitter.finatra.http.test.EmbeddedHttpServer
import com.twitter.inject.server.FeatureTest

/**
  * Created by MIYAFENG1 on 2016/6/8.
  */
class BookRecordFeatureTest extends FeatureTest{
  override val server = new EmbeddedHttpServer(
    twitterServer = new AAAServer
  )
  "BookRecord" should {

    "Save book when POST request is made" in {
      val response = server.httpPost(
        path = "/book",
        postBody =
          """
            |{
            |"isbn":"1",
            |"name":"scala",
            |"author":"lalala"
            |}
          """.stripMargin,
        andExpect = Status.Created,
        withLocation = "/book/1"
      )

      server.httpGetJson[List[Book]](
        path = response.location.get,
        andExpect = Status.Ok,
        withJsonBody =
          """
            |[
            |  {
            |    "isbn" : "1",
            |    "name" : "scala",
            |    "author" : "lalala"
            |  }
            |]
          """.stripMargin
      )
    }

    "List all book for a user when GET request is made" in {
      val response = server.httpPost(
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

      server.httpGetJson[List[Book]](
        path = response.location.get,
        andExpect = Status.Ok,
        withJsonBody =
          """
            |[
            |  {
            |    "isbn" : "2",
            |    "name" : "design patten",
            |    "author" : "god"
            |  }
            |]
          """.stripMargin
      )
    }


    "update book" in {

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

      val response = server.httpPut(
        path = "/book/2",
        putBody =
          """
            |{
            |"isbn":"2",
            |"name":"design patten",
            |"author":"superman"
            |}
          """.stripMargin,
        andExpect = Status.Ok
      )

      server.httpGetJson[List[Book]](
        path = response.location.get,
        andExpect = Status.Ok,
        withJsonBody =
          """
            |[
            |{
            |"isbn":"2",
            |"name":"design patten",
            |"author":"superman"
            |}
            |]
          """.stripMargin
      )
    }

    "delete book" in {
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

    "clean collection" in {
      server.httpPost(
        path = "/clean",
        postBody = "",
        andExpect = Status.Ok
      )

      server.httpGet(
        path = "/book",
        andExpect = Status.Ok,
        withBody = ""
      )
    }
  }
}
