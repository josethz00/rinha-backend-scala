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

  def insertPessoa(pessoa: Pessoa, numTries: Int = 3): Future[Unit] = {

    val insertQuery = Tables.pessoaTable += pessoa
    val futureId: Future[Int] = connection.db.run(insertQuery)
    futureId.flatMap { _ =>
      println(s"Query was successful.")
      Future.successful(())
    }.recoverWith {
      case ex: Throwable if numTries > 1 =>
        println(s"Query failed, reason: $ex")
        insertPessoa(pessoa, numTries - 1)
      case ex: Throwable =>
        println(s"Query failed after $numTries tries, reason: $ex")
        Future.failed(ex)

    }
  }

}
