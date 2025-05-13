ThisBuild / resolvers += "lightbend-commercial-mvn" at
  "https://repo.akka.io/pass/b37c9Kcj6eKryIZumB0UjC1okJB7hstBCBG67sNXLxMl2WSB/commercial-releases"
ThisBuild / resolvers += Resolver.url("lightbend-commercial-ivy",
  url("https://repo.akka.io/pass/b37c9Kcj6eKryIZumB0UjC1okJB7hstBCBG67sNXLxMl2WSB/commercial-releases"))(Resolver.ivyStylePatterns)


credentials += Credentials(Path.userHome / ".lightbend" / "commercial.credentials")
resolvers += "com-mvn" at "https://repo.akka.io/commercial-releases/"
resolvers += Resolver.url("com-ivy",
  url("https://repo.akka.io/commercial-releases/"))(Resolver.ivyStylePatterns)
