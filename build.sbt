import android.Keys._
import android.Dependencies.aar

android.Plugin.androidBuild

platformTarget in Android := "android-22"

name := "Reversi Infinity"

scalaVersion := "2.11.6"

scalacOptions += "-target:jvm-1.6"

run <<= run in Android

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "jcenter" at "http://jcenter.bintray.com"
)

// scalacOptions in (Compile, compile) ++=
//  (dependencyClasspath in Compile).value.files.map("-P:wartremover:cp:" + _.toURI.toURL)

// scalacOptions in (Compile, compile) += "-P:wartremover:traverser:macroid.warts.CheckUi"

proguardScala in Android := true

proguardOptions in Android ++= Seq(
  "-ignorewarnings",
  "-keep class scala.Dynamic"
)

libraryDependencies ++= Seq(
  // aar("org.macroid" %% "macroid" % "2.0.0-M3"),
  aar("com.android.support" % "appcompat-v7" % "21.0.0"),
  aar("com.android.support" % "gridlayout-v7" % "21.0.0"),
  aar("com.android.support" % "support-v4" % "21.0.0"),
  "com.lihaoyi" %% "scalarx" % "0.2.8",
  "org.scalaz" %% "scalaz-core" % "7.1.0"
    exclude("org.scala-lang.modules", "scala-parser-combinators_2.11")
    exclude("org.scala-lang.modules", "scala-xml_2.11"),
  "org.scalaz" %% "scalaz-effect" % "7.1.0",
  "org.scalaz" %% "scalaz-concurrent" % "7.1.0"
  // compilerPlugin("org.brianmckenna" %% "wartremover" % "0.11")
)
