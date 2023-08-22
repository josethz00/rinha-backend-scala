package database

import api_rest.Pessoa
import java.util.UUID
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.PostgresProfile.api._
object operations {

  val executor = Executors.newFixedThreadPool(4)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executor)

  def insertPessoa(pessoa: Pessoa): Future[Unit] = {

    val insertQuery = Tables.pessoaTable += pessoa
    val futureId: Future[Int] = connection.db.run(insertQuery)
    futureId.flatMap { _ =>
      Future.successful(())
    }.recoverWith {
      case ex: Throwable =>
        println(ex)
        Future.failed(ex)

    }
  }
  def getPessoasByID(uuid:UUID): Future[Seq[Pessoa]]  = {
    val query = Tables.pessoaTable.filter(_.id === uuid)
    connection.db.run(query.result)
  }

}
