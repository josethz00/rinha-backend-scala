package database

import api_rest.Pessoa
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

object PrivateExecutionContext {
  val executor = Executors.newFixedThreadPool(4)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executor)
}

object operations {

  import PrivateExecutionContext._
  import slick.jdbc.PostgresProfile.api._

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

}
