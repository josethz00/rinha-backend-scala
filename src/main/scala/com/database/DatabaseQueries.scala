package com.database

import doobie.ConnectionIO
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.util.{ Read, Write }
import doobie.util.query.Query0

object DatabaseQueries {
  implicit val pessoaRead: Read[Pessoa] = Read[Pessoa]
  implicit val pessoaWrite: Write[Pessoa] = Write[Pessoa]

  def insertPessoa(pessoa: Pessoa): ConnectionIO[String] =
    sql"""
        INSERT INTO pessoas (apelido, nome, nascimento)
        VALUES (${pessoa.apelido}, ${pessoa.nome}, ${pessoa.nascimento})
        RETURNING id
      """.query[String].unique

  def findPessoaByID(id: String): ConnectionIO[Option[Pessoa]] = {
    sql"SELECT id, apelido, nome, nascimento, stack FROM pessoas WHERE id = $id"
      .query[Pessoa]
      .option
  }
  def listPessoas(t: String): ConnectionIO[Pessoas] =
    sql"SELECT * FROM pessoas WHERE apelido % $t OR nome % $t OR stack @> ARRAY[$t]::varchar[]"
      .query[Pessoa]
      .to[List]
      .map(Pessoas.apply)

  def contagemPessoas(): ConnectionIO[Int] =
    sql"SELECT COUNT(*) FROM pessoas".query[Int].unique
}
