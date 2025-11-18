ThisBuild / resolvers += "lightbend-commercial-mvn" at
  "https://repo.akka.io/pass/b37c9Kcj6eKryIZumB0UjC1okJB7hstBCBG67sNXLxMl2WSB/commercial-releases"
ThisBuild / resolvers += Resolver.url("lightbend-commercial-ivy",
  url("https://repo.akka.io/pass/b37c9Kcj6eKryIZumB0UjC1okJB7hstBCBG67sNXLxMl2WSB/commercial-releases"))(Resolver.ivyStylePatterns)
