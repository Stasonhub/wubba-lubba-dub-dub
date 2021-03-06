name := """breezy-web-server"""
organization := "breezy"
version := "1.0-SNAPSHOT"

resolvers += "Osgeo" at "http://download.osgeo.org/webdav/geotools/"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// using cached resolution
updateOptions := updateOptions.value.withCachedResolution(true)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

val doobieVersion = "0.4.2-SNAPSHOT"

libraryDependencies += jdbc
libraryDependencies += evolutions
libraryDependencies += filters
libraryDependencies += "org.tpolecat" %% "doobie-core-cats" % doobieVersion
libraryDependencies += "org.tpolecat" %% "doobie-postgres-cats" % doobieVersion
libraryDependencies += "org.postgresql" % "postgresql" % "9.4.1212"
libraryDependencies += "io.github.bonigarcia" % "webdrivermanager" % "1.5.0"
libraryDependencies += "org.seleniumhq.selenium" % "selenium-support" % "3.1.0"
libraryDependencies += "org.seleniumhq.selenium" % "selenium-chrome-driver" % "3.1.0"
libraryDependencies += "net.lightbody.bmp" % "browsermob-core" % "2.1.4"
libraryDependencies += "com.squareup.okhttp3" % "okhttp" % "3.4.2"
libraryDependencies += "org.bytedeco" % "javacpp" % "1.3.2"
libraryDependencies += "org.bytedeco.javacpp-presets" % "tesseract" % "3.04.01-1.2"
libraryDependencies += "org.bytedeco.javacpp-presets" % "leptonica" % "1.73-1.2"
libraryDependencies += "org.bytedeco.javacpp-presets" % "tesseract" % "3.04.01-1.2" classifier "macosx-x86_64"
libraryDependencies += "org.bytedeco.javacpp-presets" % "leptonica" % "1.73-1.2" classifier "macosx-x86_64"
libraryDependencies += "org.bytedeco.javacpp-presets" % "tesseract" % "3.04.01-1.2" classifier "linux-x86_64"
libraryDependencies += "org.bytedeco.javacpp-presets" % "leptonica" % "1.73-1.2" classifier "linux-x86_64"
libraryDependencies += "com.twelvemonkeys.imageio" % "imageio-jpeg" % "3.3.2"
libraryDependencies += "com.twelvemonkeys.imageio" % "imageio-core" % "3.3.2"
libraryDependencies += "com.github.dfabulich" % "sitemapgen4j" % "1.0.6"
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.5"
libraryDependencies += "commons-io" % "commons-io" % "2.5"
libraryDependencies += "org.geotools" % "gt-geojson" % "16.0"
libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "4.10"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.1"

libraryDependencies += "org.specs2" %% "specs2-core" % "3.9.1" % "test"
libraryDependencies += "org.specs2" %% "specs2-mock" % "3.9.1" % "test"
libraryDependencies += "org.specs2" %% "specs2-matcher-extra" % "3.9.1" % "test"
libraryDependencies += "org.tpolecat" %% "doobie-specs2-cats" % doobieVersion % Test
