/*
 * Copyright 2016 Dennis Vriend and Ruud Welling
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package akka.persistence.jdbc
package util

import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

private[jdbc] object ConfigOps {
  implicit class ConfigOperations(val config: Config) extends AnyVal {
    def as[A](key: String): Option[A] =
      Try(config.getAnyRef(key)).map(_.asInstanceOf[A]).toOption

    def as[A](key: String, default: A): A =
      Try(config.getAnyRef(key)).map(_.asInstanceOf[A])
        .getOrElse(default)

    def asConfig(key: String, default: Config = ConfigFactory.empty): Config =
      Try(config.getConfig(key))
        .getOrElse(default)

    def asInt(key: String, default: Int): Int =
      Try(config.getInt(key))
        .getOrElse(default)

    def asBoolean(key: String, default: Boolean): Boolean =
      Try(config.getBoolean(key))
        .getOrElse(default)

    def asFiniteDuration(key: String, default: FiniteDuration): FiniteDuration =
      Try(FiniteDuration(config.getDuration(key).toMillis, TimeUnit.MILLISECONDS))
        .getOrElse(default)
  }

  final implicit class TryOps[A](val t: Try[A]) extends AnyVal {
    def ?:(default: A): A = t.getOrElse(default)
  }

  final implicit class StringOptOps(val t: Option[String]) extends AnyVal {
    /**
     * Trim the String content, when empty, return None
     */
    def trim: Option[String] = t.map(_.trim).filter(_.nonEmpty)
  }

  final implicit class Requiring[A](val value: A) extends AnyVal {
    @inline def requiring(cond: Boolean, msg: => Any): A = {
      require(cond, msg)
      value
    }

    @inline def requiring(cond: A => Boolean, msg: => Any): A = {
      require(cond(value), msg)
      value
    }
  }
}