package com.database

import java.time.LocalDate
import scala.collection.immutable

//#user-case-classes
final case class Pessoa(id:Option[String], apelido: String, nome: String, nascimento: String, stack:Option[Seq[String]])
final case class Pessoas(pessoas: immutable.Seq[Pessoa])