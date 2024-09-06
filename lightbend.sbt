ThisBuild / resolvers += "lightbend-commercial-mvn" at
  "https://repo.lightbend.com/pass/6kHELK6KmBWLKH_YkIglRIZPRv0OgXBWrbCzGN7x6xyYxKBm/commercial-releases"
ThisBuild / resolvers += Resolver.url("lightbend-commercial-ivy",
  url("https://repo.lightbend.com/pass/6kHELK6KmBWLKH_YkIglRIZPRv0OgXBWrbCzGN7x6xyYxKBm/commercial-releases"))(Resolver.ivyStylePatterns)


credentials += Credentials(Path.userHome / ".lightbend" / "commercial.credentials")
resolvers += "com-mvn" at "https://repo.lightbend.com/commercial-releases/"
resolvers += Resolver.url("com-ivy",
  url("https://repo.lightbend.com/commercial-releases/"))(Resolver.ivyStylePatterns)
