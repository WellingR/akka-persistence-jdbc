# akka-persistence-jdbc

[![Join the chat at https://gitter.im/dnvriend/akka-persistence-jdbc](https://badges.gitter.im/dnvriend/akka-persistence-jdbc.svg)](https://gitter.im/dnvriend/akka-persistence-jdbc?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/dnvriend/akka-persistence-jdbc.svg?branch=master)](https://travis-ci.org/dnvriend/akka-persistence-jdbc)
[![Download](https://api.bintray.com/packages/dnvriend/maven/akka-persistence-jdbc/images/download.svg)](https://bintray.com/dnvriend/maven/akka-persistence-jdbc/_latestVersion)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a5d8576c2a56479ab1c40d87c78bba58)](https://www.codacy.com/app/dnvriend/akka-persistence-jdbc?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dnvriend/akka-persistence-jdbc&amp;utm_campaign=Badge_Grade)
[![License](http://img.shields.io/:license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

akka-persistence-jdbc writes journal and snapshot entries to a configured JDBC store. It implements the full akka-persistence-query API and is therefore very useful for implementing DDD-style application models using Akka and Scala for creating reactive applications.

## Installation
Add the following to your `build.sbt`:

```scala
// the library is available in Bintray's JCenter
resolvers += Resolver.jcenterRepo

// akka 2.5.x (not yet available)
libraryDependencies += "com.github.wellingr" %% "akka-persistence-jdbc" % "3.0.0"

// akka 2.4.x (not yet available)
libraryDependencies += "com.github.wellingr" %% "akka-persistence-jdbc-akka24" % "3.0.0"
```

## Contribution policy

Contributions via GitHub pull requests are gladly accepted from their original author. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

## Code of Conduct
Contributors all agree to follow the [W3C Code of Ethics and Professional Conduct][w3c-cond].

If you want to take action, feel free to contact Dennis Vriend <dnvriend@gmail.com>. You can also contact W3C Staff as explained in [W3C Procedures][w3c-proc].

## License
This source code is made available under the [Apache 2.0 License][apache]. The [quick summary of what this license means is available here](https://tldrlegal.com/license/apache-license-2.0-(apache-2.0))

## Configuration
The plugin relies on Slick 3.2.x to do create the SQL dialect for the database in use, therefore the following must be configured in `application.conf`

Configure `akka-persistence`:
- instruct akka persistence to use the `jdbc-journal` plugin,
- instruct akka persistence to use the `jdbc-snapshot-store` plugin,

Configure `slick`:
- The following slick drivers are supported:
  - `slick.jdbc.PostgresProfile$`
  - `slick.jdbc.MySQLProfile$`
  - `slick.jdbc.H2Profile$`
  - `slick.jdbc.OracleProfile$`

## Database Schema

- [Postgres Schema](https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/test/resources/schema/postgres/postgres-schema.sql)
- [MySQL Schema](https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/test/resources/schema/mysql/mysql-schema.sql)
- [H2 Schema](https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/test/resources/schema/h2/h2-schema.sql)
- [Oracle Schema](https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/test/resources/schema/oracle/oracle-schema.sql)

## Configuration

- [Default](https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/main/resources/reference.conf)
- [Postgres](https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/test/resources/postgres-application.conf)
- [MySQL](https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/test/resources/mysql-application.conf)
- [H2](https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/test/resources/h2-application.conf)
- [Oracle](https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/test/resources/oracle-application.conf)

## DataSource lookup by JNDI name
The plugin uses `slick` as the database access library. Slick [supports jndi][slick-jndi] for looking up [DataSource][ds]s.

To enable the JNDI lookup, you must add the following to your application.conf:

```
jdbc-journal {
  slick {
    driver = "slick.jdbc.PostgresDriver$"
    jndiName = "java:jboss/datasources/PostgresDS"   
  }
}
```

## How to get the ReadJournal using Scala
The `ReadJournal` is retrieved via the `akka.persistence.query.PersistenceQuery` extension:

```scala
import akka.persistence.query.PersistenceQuery
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal

val readJournal: JdbcReadJournal = PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)
```

## How to get the ReadJournal using Java
The `ReadJournal` is retrieved via the `akka.persistence.query.PersistenceQuery` extension:

```java
import akka.persistence.query.PersistenceQuery
import akka.persistence.jdbc.query.javadsl.JdbcReadJournal

final JdbcReadJournal readJournal = PersistenceQuery.get(system).getReadJournalFor(JdbcReadJournal.class, JdbcReadJournal.Identifier());
```

## Persistence Query
The plugin supports the following queries:

## AllPersistenceIdsQuery and CurrentPersistenceIdsQuery
`allPersistenceIds` and `currentPersistenceIds` are used for retrieving all persistenceIds of all persistent actors.

```scala
import akka.actor.ActorSystem
import akka.stream.{Materializer, ActorMaterializer}
import akka.stream.scaladsl.Source
import akka.persistence.query.PersistenceQuery
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal

implicit val system: ActorSystem = ActorSystem()
implicit val mat: Materializer = ActorMaterializer()(system)
val readJournal: JdbcReadJournal = PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)

val willNotCompleteTheStream: Source[String, NotUsed] = readJournal.allPersistenceIds()

val willCompleteTheStream: Source[String, NotUsed] = readJournal.currentPersistenceIds()
```

The returned event stream is unordered and you can expect different order for multiple executions of the query.

When using the `allPersistenceIds` query, the stream is not completed when it reaches the end of the currently used persistenceIds,
but it continues to push new persistenceIds when new persistent actors are created.

When using the `currentPersistenceIds` query, the stream is completed when the end of the current list of persistenceIds is reached,
thus it is not a `live` query.

The stream is completed with failure if there is a failure in executing the query in the backend journal.

## EventsByPersistenceIdQuery and CurrentEventsByPersistenceIdQuery
`eventsByPersistenceId` and `currentEventsByPersistenceId` is used for retrieving events for
a specific PersistentActor identified by persistenceId.

```scala
import akka.actor.ActorSystem
import akka.stream.{Materializer, ActorMaterializer}
import akka.stream.scaladsl.Source
import akka.persistence.query.{ PersistenceQuery, EventEnvelope }
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal

implicit val system: ActorSystem = ActorSystem()
implicit val mat: Materializer = ActorMaterializer()(system)
val readJournal: JdbcReadJournal = PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)

val willNotCompleteTheStream: Source[EventEnvelope, NotUsed] = readJournal.eventsByPersistenceId("some-persistence-id", 0L, Long.MaxValue)

val willCompleteTheStream: Source[EventEnvelope, NotUsed] = readJournal.currentEventsByPersistenceId("some-persistence-id", 0L, Long.MaxValue)
```

You can retrieve a subset of all events by specifying `fromSequenceNr` and `toSequenceNr` or use `0L` and `Long.MaxValue` respectively to retrieve all events. Note that the corresponding sequence number of each event is provided in the `EventEnvelope`, which makes it possible to resume the stream at a later point from a given sequence number.

The returned event stream is ordered by sequence number, i.e. the same order as the PersistentActor persisted the events. The same prefix of stream elements (in same order) are returned for multiple executions of the query, except for when events have been deleted.

The stream is completed with failure if there is a failure in executing the query in the backend journal.

## EventsByTag and CurrentEventsByTag
`eventsByTag` and `currentEventsByTag` are used for retrieving events that were marked with a given
`tag`, e.g. all domain events of an Aggregate Root type.

```scala
import akka.actor.ActorSystem
import akka.stream.{Materializer, ActorMaterializer}
import akka.stream.scaladsl.Source
import akka.persistence.query.{ PersistenceQuery, EventEnvelope }
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal

implicit val system: ActorSystem = ActorSystem()
implicit val mat: Materializer = ActorMaterializer()(system)
val readJournal: JdbcReadJournal = PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)

val willNotCompleteTheStream: Source[EventEnvelope, NotUsed] = readJournal.eventsByTag("apple", 0L)

val willCompleteTheStream: Source[EventEnvelope, NotUsed] = readJournal.currentEventsByTag("apple", 0L)
```

## Tagging events
To tag events you'll need to create an [Event Adapter][event-adapter] that will wrap the event in a [akka.persistence.journal.Tagged](http://doc.akka.io/api/akka/2.4.1/#akka.persistence.journal.Tagged) class with the given tags. The `Tagged` class will instruct `akka-persistence-jdbc` to tag the event with the given set of tags.

The persistence plugin will __not__ store the `Tagged` class in the journal. It will strip the `tags` and `payload` from the `Tagged` class, and use the class only as an instruction to tag the event with the given tags and store the `payload` in the  `message` field of the journal table.

```scala
import akka.persistence.journal.{ Tagged, WriteEventAdapter }
import com.github.dnvriend.Person.{ LastNameChanged, FirstNameChanged, PersonCreated }

class TaggingEventAdapter extends WriteEventAdapter {
  override def manifest(event: Any): String = ""

  def withTag(event: Any, tag: String) = Tagged(event, Set(tag))

  override def toJournal(event: Any): Any = event match {
    case _: PersonCreated ⇒
      withTag(event, "person-created")
    case _: FirstNameChanged ⇒
      withTag(event, "first-name-changed")
    case _: LastNameChanged ⇒
      withTag(event, "last-name-changed")
    case _ ⇒ event
  }
}
```

The `EventAdapter` must be registered by adding the following to the root of `application.conf` Please see the  [demo-akka-persistence-jdbc](https://github.com/dnvriend/demo-akka-persistence-jdbc) project for more information.

```bash
jdbc-journal {
  event-adapters {
    tagging = "com.github.dnvriend.TaggingEventAdapter"
  }
  event-adapter-bindings {
    "com.github.dnvriend.Person$PersonCreated" = tagging
    "com.github.dnvriend.Person$FirstNameChanged" = tagging
    "com.github.dnvriend.Person$LastNameChanged" = tagging
  }
}
```

You can retrieve a subset of all events by specifying offset, or use 0L to retrieve all events with a given tag. The offset corresponds to an ordered sequence number for the specific tag. Note that the corresponding offset of each  event is provided in the EventEnvelope, which makes it possible to resume the stream at a later point from a given offset.

In addition to the offset the EventEnvelope also provides persistenceId and sequenceNr for each event. The sequenceNr is  the sequence number for the persistent actor with the persistenceId that persisted the event. The persistenceId + sequenceNr  is an unique identifier for the event.

The returned event stream contains only events that correspond to the given tag, and is ordered by the creation time of the events. The same stream elements (in same order) are returned for multiple executions of the same query. Deleted events are not deleted from the tagged event stream.

## Custom DAO Implementation
The plugin supports loading a custom DAO for the journal and snapshot. You should implement a custom Data Access Object (DAO) if you wish to alter the default persistency strategy in
any way, but wish to reuse all the logic that the plugin already has in place, eg. the Akka Persistence Query API. For example, the default persistency strategy that the plugin
supports serializes journal and snapshot messages using a serializer of your choice and stores them as byte arrays in the database.

By means of configuration in `application.conf` a DAO can be configured, below the default DAOs are shown:

```bash
jdbc-journal {
  dao = "akka.persistence.jdbc.journal.dao.ByteArrayJournalDao"
}

jdbc-snapshot-store {
  dao = "akka.persistence.jdbc.snapshot.dao.ByteArraySnapshotDao"
}

jdbc-read-journal {
  dao = "akka.persistence.jdbc.query.dao.ByteArrayReadJournalDao"
}
```

Storing messages as byte arrays in blobs is not the only way to store information in a database. For example, you could store messages with full type information as a normal database rows, each event type having its own table.
For example, implementing a Journal Log table that stores all persistenceId, sequenceNumber and event type discriminator field, and storing the event data in another table with full typing

You only have to implement two interfaces `akka.persistence.jdbc.journal.dao.JournalDao` and/or `akka.persistence.jdbc.snapshot.dao.SnapshotDao`. As these APIs are only now exposed for public use, the interfaces may change when the API needs to
change for whatever reason.

For example, take a look at the following two custom DAOs:

```scala
class MyCustomJournalDao(db: Database, val profile: JdbcProfile, journalConfig: JournalConfig, serialization: Serialization)(implicit ec: ExecutionContext, mat: Materializer) extends JournalDao {
    // snip
}

class MyCustomSnapshotDao(db: JdbcBackend#Database, val profile: JdbcProfile, snapshotConfig: SnapshotConfig, serialization: Serialization)(implicit ec: ExecutionContext, val mat: Materializer) extends SnapshotDao {
    // snip
}
```

As you can see, the custom DAOs get a _Slick database_, a _Slick profile_, the journal or snapshot _configuration_, an _akka.serialization.Serialization_, an _ExecutionContext_ and _Materializer_ injected after constructed.
You should register the Fully Qualified Class Name in `application.conf` so that the custom DAOs will be used.

For more information please review the two default implementations `akka.persistence.jdbc.dao.bytea.journal.ByteArrayJournalDao` and `akka.persistence.jdbc.dao.bytea.snapshot.ByteArraySnapshotDao` or the demo custom DAO example from the [demo-akka-persistence](https://github.com/dnvriend/demo-akka-persistence-jdbc) site.

## Explicitly shutting down the database connections
The plugin automatically shuts down the HikariCP connection pool only when the ActorSystem is explicitly terminated.
It is advisable to register a shutdown hook to be run when the VM exits that terminates the ActorSystem:

```scala
sys.addShutdownHook(system.terminate())
```

## Changelog
## 3.0.0 (TO BE RELEASED)
  - Forked from [dnvriend-akka-persistence-jdbc]
  - Increased major version number, the akka 2.4 version will get a different artifact id.
  - Fixed bug in eventsByTag and currentEventsByTag query, which could case some event to be skipped
  - Implemented batch writing for the journal which results in better thoughput.
  - Fixed a potential issue where actors could retrieve their latest sequence number too early upon restart.

## 2.4.18.2 (2017-06-09)
  - Fixed Issue #106 'JdbcReadJournal in javadsl does not support CurrentEventsByTagQuery2 and EventsByTagQuery2'

## 2.5.2.0 (2017-06-09)
  - Merged PR #105 [aenevala][aenevala] Added support for event adapters on query side for 2.5.2.0, thanks!
  - Akka 2.5.1 -> 2.5.2

## 2.4.18.1 (2017-06-04)
  - Merged PR #103 [aenevala][aenevala] Added support for event adapters on query side, thanks!

### 2.5.1.0 (2017-05-03)
  - Akka 2.5.0 -> 2.5.1

### 2.4.18.0 (2017-05-03)
  - Akka 2.4.17 -> 2.4.18
  - Merged PR #88 [Andrey Kouznetsov][kouznetsov] Compiled inserts, thanks!
  - Tweaked threads, maxConnections and minConnections to leverage [slick issue #1461 - fixes issue 1274: Slick deadlock](https://github.com/slick/slick/pull/1461)

### 2.5.0.0 (2017-04-13)
  - Merged PR #97 [Dan Di Spaltro][dispalt] Update to akka-2.5.0-RC2, thanks!
  - Akka 2.5.0-RC2 -> Akka 2.5.0

### 2.4.17.1 (2017-02-24)
  - Slick 3.1.1 -> 3.2.0
  - Scala 2.11.8 and 2.12.1 support
  - The following slick drivers are supported:
    - `slick.jdbc.PostgresProfile$`
    - `slick.jdbc.MySQLProfile$`
    - `slick.jdbc.H2Profile$`
    - `slick.jdbc.OracleProfile$`

### 2.4.17.0.3.2.0-RC1
  - Slick 3.2.0-RC1 test release
  - Akka 2.4.17
  - Only availabe on Bintray's JCenter

### 2.4.17.0 (2016-02-12)
  - New versioning scheme; now using the version of Akka with the akka-persistence-inmemory version appended to it, starting from `.0`
  - Akka 2.4.16 -> 2.4.17

### 2.6.12-3.2.0-M2 (2016-12-21)
  - Special thanks to [joseblas][joseblas] for PR #85: Slick 3.2.0-M1 migration, thanks!
  - Special thanks to [Timothy Klim][timothyklim] for PR #86: Upgrade slick to 3.2.0-M2, thanks!
  - Slick 3.2.0-M2 test release
  - Scala 2.11.8 and 2.12.1 build
  - Only availabe on Bintray's JCenter
  - The following slick drivers are supported:
    - `slick.jdbc.PostgresProfile$`
    - `slick.jdbc.MySQLProfile$`
    - `slick.jdbc.H2Profile$`
    - `slick.jdbc.OracleProfile$`

### 2.6.12 (2016-12-20)
  - Akka 2.4.14 -> 2.4.16

### 2.6.11 (2016-12-09)
  - Merged PR #3 [Sergey Kisel][skisel] - Freeslick support to use it with Oracle, thanks!

### 2.6.10 (2016-11-22)
  - Akka 2.4.13 -> 2.4.14

### 2.6.9 (2016-11-20)
  - Akka 2.4.12 -> 2.4.13

### 2.6.8 (2016-11-03)
  - Akka 2.4.10 -> 2.4.12
  - Fixed 'Snapshot storage BLOB handling' by [Sergey Kisel][skisel], thanks!
  - Filter out events that have already been deleted.
  - Removed the _non-official_ and __never-to-be-used__ bulk loading interface.
  - Support for the new queries `CurrentEventsByTagQuery2` and `EventsByTagQuery2`, please read the akka-persistence-query documentation to see what has changed.
  - The akka-persistence-jdbc plugin only supports the `akka.persistence.query.NoOffset` and the `akka.persistence.query.Sequence` offset types.
  - There is no support for the `akka.persistence.query.TimeBasedUUID` offset type. When used, akka-persistence-jdbc will throw an IllegalArgumentException if offered to the read-journal.

### 2.6.7 (2016-09-07)
  - Merged PR #75 [jroper][jroper] - Removed binary dependency on slick-extensions, thanks!
  - Please note, slick-extensions 3.1.0 are open source, but the license didn't change, so you cannot use it for free, you still need a [Lightbend Subscription](https://www.lightbend.com/platform/subscription).
  - Akka 2.4.9 -> 2.4.10

### 2.6.6 (2016-08-22)
  - Merged PR #66 [monktastic][monktastic], eventsByPersistenceId should terminate when toSequenceNr is reached, thanks!

### 2.6.5 (2016-08-20)
  - Akka 2.4.9-RC2 -> 2.4.9

### 2.6.5-RC2 (2016-08-06)
  - Akka 2.4.9-RC1 -> 2.4.9-RC2

### 2.6.5-RC1 (2016-08-03)
  - Akka 2.4.8 -> 2.4.9-RC1

### 2.6.4 (2016-07-30)
  - Merged PR #62 [jtysper][jtysper], Fix Oracle support, thanks!

### 2.6.3 (2016-07-27)
  - Merged PR #61 [Nikolay Tatarinov][rockjam], Sql optimizations, thanks!

### 2.6.2 (2016-07-26)
  - Fix for issue #60 where an immutable.Vector was trying to be matched by the serializer in TrySeq stage.

### 2.6.1 (2016-07-23)
  - Support for the __non-official__ bulk loading interface [akka.persistence.query.scaladsl.EventWriter](https://github.com/dnvriend/akka-persistence-query-writer/blob/master/src/main/scala/akka/persistence/query/scaladsl/EventWriter.scala)
    added. I need this interface to load massive amounts of data, that will be processed by many actors, but initially I just want to create and store one or
    more events belonging to an actor, that will handle the business rules eventually. Using actors or a shard region for that matter, just gives to much
    actor life cycle overhead ie. too many calls to the data store. The `akka.persistence.query.scaladsl.EventWriter` interface is non-official and puts all
    responsibility of ensuring the integrity of the journal on you. This means when some strange things are happening caused by wrong loading of the data,
    and therefore breaking the integrity and ruleset of akka-persistence, all the responsibility on fixing it is on you, and not on the Akka team.

### 2.6.0 (2016-07-17)
  - Removed the `deleted_to` and `created` columns of the `journal` table to become compatible with
   `akka-persistence-query` spec that states that all messages should be replayed, even deleted ones
  - New schema's are available for [postgres][postgres-schema], [mysql][mysql-schema] and [h2][h2-schema]
  - No need for Query Publishers with the new akka-streams API
  - Codacy code cleanup
  - There is still no support for Oracle since the addition of the ordering SERIAL column which Oracle does not support. Help to add Oracle support is appreciated.

### 2.5.2 (2016-07-03)
  - The `eventsByTag` query should now be fixed.

### 2.5.1 (2016-07-03)
  - There is no 2.5.1; error while releasing

### 2.5.0 (2016-06-29)
  - Changed the database schema to include two new columns, an `ordering` and a `deleted` column. Both fields are needed
    to support the akka-persistence-query API. The `ordering` column is needed to register the total ordering of events
    an is used for the offset for both `*byTag` queries. The deleted column is not yet used.
  - Known issue: will not work on Oracle (yet).

[aenevala]: https://github.com/aenevala
[dispalt]: https://github.com/dispalt
[monktastic]: https://github.com/monktastic
[fcristovao]: https://github.com/fcristovao
[ellawala]: https://github.com/charithe
[turner]: https://github.com/wwwiiilll
[kouznetsov]: https://github.com/prettynatty
[boldyrev]: https://github.com/bpg
[roman]: https://github.com/romusz
[vila]: https://github.com/miguel-vila
[mwkohout]: https://github.com/mwkohout
[krasser]: https://github.com/krasserm
[shah]: https://github.com/gopalsaob
[rockjam]: https://github.com/rockjam
[jtysper]: https://github.com/jtysper
[jroper]: https://github.com/jroper
[skisel]: https://github.com/skisel
[joseblas]: https://github.com/joseblas
[timothyklim]: https://github.com/TimothyKlim

[dnvriend-akka-persistence-jdbc]: https://github.com/dnvriend/akka-persistence-jdbc

[scalikejdbc]: http://scalikejdbc.org/
[slick]: http://slick.typesafe.com/
[slick-jndi]: http://slick.typesafe.com/doc/3.1.1/database.html#using-a-jndi-name
[slick-ex]: http://slick.typesafe.com/doc/3.1.1/extensions.html
[slick-ex-lic]: http://slick.typesafe.com/news/2016/02/01/slick-extensions-licensing-change.html

[apache]: http://www.apache.org/licenses/LICENSE-2.0
[w3c-cond]: http://www.w3.org/Consortium/cepc/
[w3c-proc]: http://www.w3.org/Consortium/pwe/#Procedures
[lightbend]: http://www.lightbend.com/

[postgres]: http://www.postgresql.org/
[ap-testkit]: https://github.com/krasserm/akka-persistence-testkit
[ds]: http://docs.oracle.com/javase/8/docs/api/javax/sql/DataSource.html

[ser]: http://doc.akka.io/docs/akka/current/scala/serialization.html
[event-adapter]: http://doc.akka.io/docs/akka/current/scala/persistence.html#event-adapters-scala

[inmemory]: https://github.com/dnvriend/akka-persistence-inmemory
[postgres-application.conf]: https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/test/resources/postgres-application.conf
[mysql-application.conf]: https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/test/resources/mysql-application.conf
[h2-application.conf]: https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/test/resources/h2-application.conf
[oracle-application.conf]: https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/test/resources/oracle-application.conf

[postgres-schema]: https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/main/resources/schema/postgres/postgres-schema.sql
[mysql-schema]: https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/main/resources/schema/mysql/mysql-schema.sql
[h2-schema]: https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/main/resources/schema/h2/h2-schema.sql
[oracle-schema]: https://github.com/dnvriend/akka-persistence-jdbc/blob/master/src/main/resources/schema/oracle/oracle-schema.sql
