name := "Simple Lambda Calculus Evaluator"

scalaVersion in ThisBuild := "2.12.4"

lazy val root = project.in(file("."))
  .aggregate(lambdaJS, lambdaJVM)

lazy val lambda = crossProject.in(file("."))
  .settings(
    libraryDependencies ++=
      "com.lihaoyi" %%% "fastparse" % "1.0.0" ::
      "org.typelevel" %%% "cats-core" % "1.0.0-RC1" :: Nil,
    scalacOptions += "-Ypartial-unification"
  )
  .jvmSettings(
    initialCommands in console := "import lambda._",
    libraryDependencies ++=
      "org.jline" % "jline" % "3.5.1" :: Nil
  )
  .jsSettings(
    resolvers ++= Seq(
      "amateras-repo" at "http://amateras.sourceforge.jp/mvn/"),
    libraryDependencies ++=
      "com.scalawarrior" %%% "scalajs-ace" % "0.0.4" :: Nil,
    scalaJSUseMainModuleInitializer := true
  )

lazy val lambdaJS = lambda.js
lazy val lambdaJVM = lambda.jvm
