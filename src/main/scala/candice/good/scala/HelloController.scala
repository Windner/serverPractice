package candice.good.scala
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

/**
  * Created by MIYAFENG1 on 2016/6/1.
  */
class HelloController extends Controller {

  get("/hello") { request: Request =>
    "AAA says hello"
  }
  get("/users/:id") { request: Request =>
    "You looked up " + request.params("id")
  }
}