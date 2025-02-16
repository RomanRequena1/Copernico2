ThisBuild / resolvers += "lightbend-commercial-mvn" at
  "https://repo.akka.io/pass/UEbGwaz9EeZxpdgCEjxg0D0IKSo3USUMzK06IkoK8DxhYyij/commercial-releases"
ThisBuild / resolvers += Resolver.url("lightbend-commercial-ivy",
  url("https://repo.akka.io/pass/UEbGwaz9EeZxpdgCEjxg0D0IKSo3USUMzK06IkoK8DxhYyij/commercial-releases"))(Resolver.ivyStylePatterns)


credentials += Credentials(Path.userHome / ".lightbend" / "commercial.credentials")
resolvers += "com-mvn" at "https://repo.akka.io/commercial-releases/"
resolvers += Resolver.url("com-ivy",
  url("https://repo.akka.io/commercial-releases/"))(Resolver.ivyStylePatterns)
