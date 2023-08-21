package com.database

import cats.effect.{IO, Resource}
import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor

object DatabaseConnection {
  // Resource yielding a transactor configured with a bounded connect EC and an unbounded
  // transaction EC. Everything will be closed and shut down cleanly after use.
  val transactor: Resource[IO, HikariTransactor[IO]] =
  for {
    hikariConfig <- Resource.pure {
      // For the full list of hikari configurations see https://github.com/brettwooldridge/HikariCP#gear-configuration-knobs-baby
      val config = new HikariConfig()
      config.setDriverClassName("org.postgresql.Driver")
      config.setJdbcUrl("jdbc:postgresql://db:5432/rinha?user=postgres&password=admin")
      config
    }
    xa <- HikariTransactor.fromHikariConfig[IO](hikariConfig)
  } yield xa
}
