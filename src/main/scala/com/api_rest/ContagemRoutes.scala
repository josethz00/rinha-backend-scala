package com.api_rest

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.api_rest.UserRegistry._
import com.database.Pessoa

import scala.concurrent.Future

class ContagemRoutes(userRegistry: ActorRef[UserRegistry.Command])(implicit val system: ActorSystem[_]) {

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout: Timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getContagemPessoas(): Future[GetContagemPessoasResponse] =
    userRegistry.ask(GetContagemPessoas(_))

  val contagemPessoasRoutes: Route =
    pathPrefix("contagem-pessoas") {
      concat(
        pathEnd {
          get {
            onSuccess(getContagemPessoas()) { response =>
              complete(response.n.toString)
            }
          }
        },
      )
    }
}
