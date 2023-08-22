package api_rest

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import PessoaActor._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import PessoaActor.{GetPessoaResponse, GetPessoa, CreatePessoa}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import jsonSerializer._
import org.json4s._
import scala.util.{Failure, Success}
import org.json4s.native.{Json, Serialization}


class pessoaRoutes(pessoaManageActor: ActorRef[PessoaActor.Command])(implicit val system: ActorSystem[_]) {

  implicit val formats = Serialization.formats(NoTypeHints)

  private implicit val timeout: Timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getPessoa(uuid: String): Future[GetPessoaResponse] =
    pessoaManageActor.ask(GetPessoa(uuid, _))
  def createPessoa(pessoa: Pessoa): Future[PessoaCreated] =
    pessoaManageActor.ask(CreatePessoa(pessoa, _))


  val userRoutes: Route =
    pathPrefix("pessoas") {
      concat(
            post {
              entity(as[Pessoa]) { pessoa =>
                onComplete(createPessoa(pessoa)) {
                  case Success(performed) =>
                    complete((StatusCodes.Created,performed))
                  case Failure(ex) =>
                    complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
                }
              }
            },

        path(Segment) { uuid =>
          concat(
            get {
              onComplete(getPessoa(uuid)) {
                case Success(response) =>
                  response.maybeUser match {
                    case Some(pessoa) =>
                      complete(pessoa)
                    case None =>
                      val message = Map("Message" -> s"UUID $uuid não está associado a uma pessoa.")
                      complete(StatusCodes.NotFound, Json(DefaultFormats).write(message))
                  }
                case Failure(ex) =>
                  complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
                }
              })
            }
      )
    }
}
