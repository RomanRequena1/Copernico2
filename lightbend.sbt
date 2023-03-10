ThisBuild / resolvers += "lightbend-commercial-mvn" at
  "https://repo.lightbend.com/pass/W0i_x64OmI09s64XMy8P8LzK2ZmFEvajdt2jG3VqgAd0swow/commercial-releases"
ThisBuild / resolvers += Resolver.url("lightbend-commercial-ivy",
  url("https://repo.lightbend.com/pass/W0i_x64OmI09s64XMy8P8LzK2ZmFEvajdt2jG3VqgAd0swow/commercial-releases"))(Resolver.ivyStylePatterns)


credentials += Credentials(Path.userHome / ".lightbend" / "commercial.credentials")
resolvers += "com-mvn" at "https://repo.lightbend.com/commercial-releases/"
resolvers += Resolver.url("com-ivy",
  url("https://repo.lightbend.com/commercial-releases/"))(Resolver.ivyStylePatterns)
