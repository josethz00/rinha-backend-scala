package com.api_rest

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import cats.effect.IO
import cats.effect.unsafe.IORuntime
import akka.http.scaladsl.server.Directives._
import scala.util.Failure
import scala.util.Success

//#main-class
object QuickstartApp {

  private val runtime: IORuntime = IORuntime.global

  //#start-http-server
  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    import system.executionContext

    val futureBinding = Http().newServerAt("0.0.0.0", 3335).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", "0.0.0.0", "3335")
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }
  //#start-http-server
  def main(args: Array[String]): Unit = {
    val transactorResource = com.database.DatabaseConnection.transactor

    transactorResource.use { transactor =>

      val rootBehavior: Behavior[Nothing] = Behaviors.setup[Nothing] { context =>

        val userRegistryActor = context.spawn(UserRegistry(transactor), "UserRegistryActor")
        context.watch(userRegistryActor)

        val userRoutes = new UserRoutes(userRegistryActor)(context.system)
        val contagemRoutes = new ContagemRoutes(userRegistryActor)(context.system)

        // pass the two routes to `startHttpServer` method

        startHttpServer(userRoutes.userRoutes ~ contagemRoutes.contagemPessoasRoutes)(context.system)

        Behaviors.empty
      }

      val system = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")

      IO.never // Keep the application running

    }.unsafeRunSync()(runtime)
    //#server-bootstrapping
  }
}
//#main-class
