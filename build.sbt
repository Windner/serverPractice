name := "scalaHi"

version := "1.0"

scalaVersion := "2.11.8"


lazy val versions = new {
  val finatra = "2.1.2"
  val logback = "1.1.3"
  val guice = "4.0"
  val specs2 = "2.3.12"
  val scalatest = "2.2.6"
  val mongodb = "1.1.1"
  val bijection = "0.9.2"
}

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Twitter Maven" at "https://maven.twttr.com"
)

libraryDependencies += "com.twitter.finatra" % "finatra-http_2.11" % versions.finatra
libraryDependencies += "com.twitter.finatra" % "finatra-slf4j_2.11" % versions.finatra
libraryDependencies += "com.twitter" % "bijection-core_2.11" % versions.bijection
libraryDependencies += "com.twitter" % "bijection-util_2.11" % versions.bijection
libraryDependencies += "com.typesafe" % "config" % "1.2.1" //for configure file
libraryDependencies += "ch.qos.logback" % "logback-classic" % versions.logback
libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % versions.mongodb

libraryDependencies += "com.twitter.finatra" % "finatra-slf4j_2.11" % versions.finatra
libraryDependencies += "ch.qos.logback" % "logback-classic" % versions.logback

libraryDependencies += "com.twitter.finatra" % "finatra-http_2.11" % versions.finatra % "test"
libraryDependencies += "com.twitter.inject" % "inject-server_2.11" % versions.finatra % "test"
libraryDependencies += "com.twitter.inject" % "inject-app_2.11" % versions.finatra % "test"
libraryDependencies += "com.twitter.inject" % "inject-core_2.11" % versions.finatra % "test"
libraryDependencies += "com.twitter.inject" %% "inject-modules" % versions.finatra % "test"
libraryDependencies += "com.google.inject.extensions" % "guice-testlib" % versions.guice % "test"
libraryDependencies +=  "com.twitter.finatra" % "finatra-jackson_2.11" % versions.finatra % "test"

libraryDependencies += "com.twitter.finatra" % "finatra-http_2.11" % versions.finatra % "test" classifier "tests"
libraryDependencies += "com.twitter.inject" % "inject-server_2.11" % versions.finatra % "test" classifier "tests"
libraryDependencies += "com.twitter.inject" % "inject-app_2.11" % versions.finatra % "test" classifier "tests"
libraryDependencies += "com.twitter.inject" % "inject-core_2.11" % versions.finatra % "test" classifier "tests"
libraryDependencies += "com.twitter.inject" % "inject-modules_2.11" % versions.finatra % "test" classifier "tests"
libraryDependencies += "com.google.inject.extensions" % "guice-testlib" % versions.guice % "test" classifier "tests"
libraryDependencies +=  "com.twitter.finatra" % "finatra-jackson_2.11" % versions.finatra % "test"  classifier "tests"

// http://mvnrepository.com/artifact/com.google.code.gson/gson
libraryDependencies += "com.google.code.gson" % "gson" % "2.6.2"


libraryDependencies += "org.scalatest" %% "scalatest" % versions.scalatest % "test"
libraryDependencies += "org.specs2" %% "specs2" % versions.specs2 % "test"

unmanagedClasspath in Runtime += baseDirectory.value / "conf"