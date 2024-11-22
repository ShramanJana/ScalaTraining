package security

import org.apache.pekko.stream.Materializer
import play.api.http.HttpFilters
import play.api.mvc.{EssentialFilter, Filter, RequestHeader, Result, Results}
import repositories.AdminRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JwtAuthFilter @Inject()(
                               implicit val mat: Materializer,
                               adminRepository: AdminRepository,
                               ec: ExecutionContext
                             ) extends Filter {



  override def apply(nextFilter: RequestHeader => Future[Result])(request: RequestHeader): Future[Result] = {
    val publicRoutes = Seq("/api/v1/login", "/api/v1/register")

    if (publicRoutes.exists(request.path.startsWith)) {
      // Allow public routes without authentication
      nextFilter(request)
    } else {

      val tokenOpt = request.headers.get("Authorization").map(_.replace("Bearer ", ""))
      println(tokenOpt)
      val tokenOptResult =JwtHelper.validateToken(tokenOpt.getOrElse(""))
      // Simulate JWT validation logic
      tokenOptResult match {
        // check if username exist in database
        case Some(username: String) =>  adminRepository.usernameExists(username).flatMap { exists =>
          if(exists)
            nextFilter(request)
          else
            Future.successful(Results.Unauthorized("Invalid user"))
        }
        case _ => Future.successful(Results.Unauthorized("Invalid or missing token"))
      }

    }
  }
}

// Filters Registration
class Filters @Inject()(jwtAuthFilter: JwtAuthFilter) extends HttpFilters {
  override def filters: Seq[EssentialFilter] = Seq(jwtAuthFilter)
}