import play.sbt.routes.RoutesKeys

name := """membership-system-rest-api"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.1"

libraryDependencies  ++= Seq(
  guice,
  "org.scalactic"           %% "scalactic"                      % "3.0.8",
  "org.reactivemongo"       %% "play2-reactivemongo"            % "0.20.0-play28",
  "org.scalatest"           %% "scalatest"                      % "3.0.8"           % Test,
  "org.mockito"              % "mockito-all"                    % "1.10.19"         % Test,
  "org.scalatestplus.play"  %% "scalatestplus-play"             % "5.0.0"           % Test
)

routesGenerator := InjectedRoutesGenerator
RoutesKeys.routesImport += "models.Card"
