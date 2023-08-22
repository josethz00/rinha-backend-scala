package database

import slick.jdbc.PostgresProfile.api._

object connection {
  val db = Database.forConfig("postgres")
}