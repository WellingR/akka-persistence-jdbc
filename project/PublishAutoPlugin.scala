import sbt._
import sbt.Keys._
import bintray.BintrayKeys._

object PublishAutoPlugin extends AutoPlugin { 

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = sbtrelease.ReleasePlugin

  object autoImport {
  }

 import autoImport._

 override lazy val projectSettings = Seq(
    publishMavenStyle := true,
    scmInfoSetting("akka-persistence-jdbc"),
    pomExtraSetting("akka-persistence-jdbc"),
    homepageSetting("akka-persistence-jdbc"),
    bintrayPackageLabelsSettings,
    bintrayPackageAttributesSettings("akka-persistence-jdbc")
 )
  
  def pomExtraSetting(name: String) = pomExtra :=
    <developers>
      <developer>
        <id>dnvriend</id>
        <name>Dennis Vriend</name>
        <url>https://github.com/dnvriend</url>
      </developer>
      <developer>
        <id>wellingr</id>
        <name>Ruud Welling</name>
        <url>https://github.com/wellingr</url>
      </developer>
    </developers>

  def homepageSetting(name: String) =
    homepage := Some(url(s"https://github.com/wellingr/$name"))

  def bintrayPackageLabelsSettings =
    bintrayPackageLabels := Seq("akka", "akka-persistence", "jdbc")

  def bintrayPackageAttributesSettings(name: String) = bintrayPackageAttributes ~=
    (_ ++ Map(
      "website_url" -> Seq(bintry.Attr.String(s"https://github.com/wellingr/$name")),
      "github_repo" -> Seq(bintry.Attr.String(s"https://github.com/wellingr/$name.git")),
      "issue_tracker_url" -> Seq(bintry.Attr.String(s"https://github.com/wellingr/$name.git/issues/"))
    ))

  def scmInfoSetting(name: String) =
    scmInfo := Some(ScmInfo(
      url(s"https://github.com/wellingr/$name"),
      s"scm:git:git@github.com:wellingr/$name.git"
    ))
}