package com.api_rest

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import com.api_rest.UserRegistry._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import com.database.{Pessoa}

//#import-json-formats
//#user-routes-class
class UserRoutes(userRegistry: ActorRef[UserRegistry.Command])(implicit val system: ActorSystem[_]) {

  //#user-routes-class

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._
  //#import-json-formats

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout: Timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getPessoa(id: String): Future[GetPessoaResponse] =
    userRegistry.ask(GetPessoa(id, _))

  def createPessoa(pessoa: Pessoa): Future[ApiResponse] =
    userRegistry.ask(CreatePessoa(pessoa, _))

  def getPessoas(t: String): Future[GetPessoasResponse] =
    userRegistry.ask(GetPessoas(t, _))

  //#all-routes
  //#users-get-post
  //#users-get-delete
  val userRoutes: Route =
  pathPrefix("pessoas") {
    concat(
      pathEnd {
        concat(
          post {
            entity(as[Pessoa]) { pessoa =>
              onSuccess(createPessoa(pessoa)) { performed =>
                performed.maybeId match {
                  case Some(id) =>
                    respondWithHeader(Location(s"/pessoas/$id")) {
                      complete((StatusCodes.Created, performed.description))
                    }
                  case None =>
                    complete((StatusCodes.InternalServerError, "Could not create user"))
                }
              }
            }
          },
          get {
            parameter(Symbol("t").?) {
              case Some(t) => {
                onSuccess(getPessoas(t)) { response =>
                  complete(response.pessoas)
                }
              }
              case None =>
                complete(StatusCodes.BadRequest, "Parameter 't' is required.")
            }
          }
        )
      },
      path(Segment)(id =>
        concat(
          get {
            rejectEmptyResponse {
              onSuccess(getPessoa(id)) { response =>
                complete(response.maybePessoa)
              }
            }
          })
      ))
  }
}
