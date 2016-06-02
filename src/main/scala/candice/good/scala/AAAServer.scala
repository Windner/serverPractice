package candice.good.scala

import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter

/**
  * Created by MIYAFENG1 on 2016/6/1.
  */
object AAA extends AAAServer

class AAAServer extends HttpServer
{
  override protected def configureHttp(router: HttpRouter): Unit = {
    router.add(new HelloController)
    router.add(new WeightResource)
    router.add(new ExampleController)
  }
}