package candice.good.scala

/**
  * Created by MIYAFENG1 on 2016/6/2.
  */
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import javax.inject.Inject

class ExampleController extends Controller {

  get("/ping") { request: Request =>
    "pong"
  }

  get("/name") { request: Request =>
    response.ok.body("Bob")
  }

  post("/foo") { request: Request =>

    "bar"
  }
}