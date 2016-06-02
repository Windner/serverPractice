package candice.good.scala

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.validation.{Range, Size}
import com.twitter.inject.Logging
import org.joda.time.Instant

import scala.collection.mutable


/**
  * Created by MIYAFENG1 on 2016/6/1.
  */
class WeightResource extends Controller with Logging {

  val db = mutable.Map[String, List[Weight]]()

  get("/weights") { request: Request =>
    info("finding all weights for all users...")
    db
  }

  get("/weights/delete/:user") { request: Request =>
    info( s"""delete weight for user ${request.params("user")}""")
    val result = db.remove(request.params("user"))
    result match{
      case Some(user) => print("Delete sucessful")
      case None => print("Not exist")
    }

    db
  }

  get("/weights/:user") { request: Request =>
    info( s"""finding weight for user ${request.params("user")}""")
    db.getOrElse(request.params("user"), List())
  }

  post("/weights") { weight: Weight =>
    info("post...")
    val r = time(s"Total time take to post weight for user '${weight.user}' is %d ms") {
      val weightsForUser = db.get(weight.user) match {
        case Some(weights) => weights :+ weight
        case None => List(weight)
      }
      db.put(weight.user, weightsForUser)
      response.created.location(s"/weights/${weight.user}")

    }
    r
  }

  post("/weights/update") { weight: Weight =>
    info("post...")
    val r = time(s"Total time take to post weight for user '${weight.user}' is %d ms") {
      val weightsForUser = db.getOrElse(weight.user, List())
      info(weightsForUser)
      info(weight)
      if (weightsForUser == weight)
        info("same")
      else
      {
        info("need to update")
        db.remove(weight.user)
        val newWeight = db.get(weight.user) match {
          case Some(weights) => weights :+ weight
          case None => List(weight)
        }
        db.put(weight.user, newWeight)
      }

      response.created.location(s"/weights/${weight.user}")

    }
    r
  }
}

case class Weight(
                   @Size(min = 1, max = 25) user: String,
                   @Range(min = 25, max = 200) weight: Int,
                   status: Option[String],
                   postedAt: Instant = Instant.now()
                 )