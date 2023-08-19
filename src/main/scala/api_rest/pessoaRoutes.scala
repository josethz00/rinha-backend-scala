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
import PessoaActor.{ActionPerformed, GetUserResponse}
import scala.util.{Failure,Success}


class pessoaRoutes(pessoaManageActor: ActorRef[PessoaActor.Command])(implicit val system: ActorSystem[_]) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import jsonSerializer._

  private implicit val timeout: Timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getPessoa(nome: String): Future[GetUserResponse] =
    pessoaManageActor.ask(GetUser(nome, _))
  def createPessoa(pessoa: Pessoa): Future[ActionPerformed] =
    pessoaManageActor.ask(CreateUser(pessoa, _))


  val userRoutes: Route =
    pathPrefix("pessoas") {
      concat(
            post {
              entity(as[Pessoa]) { pessoa =>
                onComplete(createPessoa(pessoa)) {
                  case Success(performed) =>
                    complete((StatusCodes.Created, performed))
                  case Failure(ex) =>
                    complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
                }
              }
            },
        path(Segment) { id =>
          concat(
            get {
              rejectEmptyResponse {
                onSuccess(getPessoa(id)) { response =>
                  complete(response.maybeUser)
                }
              }
            })
        }
      )
    }
}
