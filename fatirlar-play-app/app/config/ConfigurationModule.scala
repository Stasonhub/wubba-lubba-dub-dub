package config

import com.typesafe.config.Config
import play.api.{ConfigLoader, Configuration, Environment}
import play.api.inject.Module

class ConfigurationModule extends Module {

  def bindings(environment: Environment, configuration: Configuration) = Seq(
    bind[DbConnectionConfig].toInstance(configuration.get[DbConnectionConfig]("db.default")(dbConnectionConfigLoader)),
    bind[ImportScheduleConfig].toInstance(configuration.get[ImportScheduleConfig]("import.schedule")(importScheduleConfigLoader)),
    bind[PhotosStorageConfig].toInstance(configuration.get[PhotosStorageConfig]("photo.storage")(photoStorageConfigLoader)),
    bind[ProxyConfig].toInstance(configuration.get[ProxyConfig]("proxy")(proxyConfigLoader)),
    bind[Int].qualifiedWith("avito.max.items").to(configuration.get[Int]("import.items.max.avito")),
    bind[Int].qualifiedWith("totook.max.items").to(configuration.get[Int]("import.items.max.totook")),
    bind[String].qualifiedWith("app.domain").to(configuration.get[String]("app.domain"))
  )

  def dbConnectionConfigLoader = new ConfigLoader[DbConnectionConfig] {
    def load(rootConfig: Config, path: String): DbConnectionConfig = {
      val config = rootConfig.getConfig(path)
      DbConnectionConfig(
        driver = config.getString("driver"),
        url = config.getString("url"),
        username = config.getString("username"),
        password = config.getString("password")
      )
    }
  }

  def importScheduleConfigLoader = new ConfigLoader[ImportScheduleConfig] {
    def load(rootConfig: Config, path: String): ImportScheduleConfig = {
      val config = rootConfig.getConfig(path)
      ImportScheduleConfig(
        initialDelay = config.getInt("initial"),
        interval = config.getInt("interval")
      )
    }
  }

  def photoStorageConfigLoader = new ConfigLoader[PhotosStorageConfig] {
    def load(rootConfig: Config, path: String): PhotosStorageConfig = {
      val config = rootConfig.getConfig(path)
      PhotosStorageConfig(
        path = config.getString("path"),
        testMode = config.getBoolean("test.mode")
      )
    }
  }

  def proxyConfigLoader = new ConfigLoader[ProxyConfig] {
    def load(rootConfig: Config, path: String): ProxyConfig = {
      val config = rootConfig.getConfig(path)
      ProxyConfig(
        host = config.getString("host"),
        port = config.getInt("port"),
        username = config.getString("username"),
        password = config.getString("password")
      )
    }
  }

}