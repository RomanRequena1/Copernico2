resolvers in ThisBuild += "lightbend-commercial-mvn" at
  "https://repo.lightbend.com/pass/nE7GFbBeyZHZh88VJpAoxg9k3chrESVerMgJFkN8VlRBKOmK/commercial-releases"
resolvers in ThisBuild += Resolver.url("lightbend-commercial-ivy",
  url("https://repo.lightbend.com/pass/nE7GFbBeyZHZh88VJpAoxg9k3chrESVerMgJFkN8VlRBKOmK/commercial-releases"))(Resolver.ivyStylePatterns)