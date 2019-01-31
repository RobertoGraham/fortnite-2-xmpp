[![Maven Central](https://img.shields.io/maven-central/v/io.github.robertograham/fortnite-2-xmpp.svg?label=Maven%20Central&style=flat-square)](https://search.maven.org/search?q=g:%22io.github.robertograham%22%20AND%20a:%22fortnite-2-xmpp%22)

# fortnite-2-xmpp

A Java 11+ client for Fortnite's XMPP services

## Features

* Ability to send chat messages and process inbound chat messages

## Installation

### Maven

```xml
<properties>
  ...
  <!-- Use the latest version whenever possible. -->
  <fortnite-2-xmpp.version>1.0.0</fortnite-2.version>
  ...
</properties>

<dependencies>
  ...
  <dependency>
    <groupId>io.github.robertograham</groupId>
    <artifactId>fortnite-2-xmpp</artifactId>
    <version>${fortnite-2-xmpp.version}</version>
  </dependency>
  ...
</dependencies>
```

## Usage

### Instantiating a client

This is the simplest way to instantiate a client:

```java
import io.github.robertograham.fortnite2.implementation.DefaultFortnite;
import io.github.robertograham.fortnite2.xmpp.implementation.DefaultFortniteXmpp;

public final class Main {

    public static void main(final String[] args) {
        final var fortnite = DefaultFortnite.Builder.newInstance("epicGamesEmailAddress", "epicGamesPassword")
            .build();
        final var fortniteXmpp = DefaultFortniteXmpp.Builder.newInstance(fortnite)
            .build();
    }
}
```

By default fortnite-2-xmpp will not debug raw XMPP traffic but this can be enabled like so:

```java
import io.github.robertograham.fortnite2.implementation.DefaultFortnite;
import io.github.robertograham.fortnite2.xmpp.implementation.DefaultFortniteXmpp;

public final class Main {

    public static void main(final String[] args) {
        final var fortnite = DefaultFortnite.Builder.newInstance("epicGamesEmailAddress", "epicGamesPassword")
            .build();
        final var fortniteXmpp = DefaultFortniteXmpp.Builder.newInstance(fortnite)
            .setDebugXmppConnections(true)
            .build();
    }
}
```

### Cleaning up

When you no longer need your client instance, remember to close your XMPP connections with a call to `FortniteXmpp.close()`. Usage examples further in this document will make 
this call implicitly using `try`-with-resources statements.

```java
import io.github.robertograham.fortnite2.implementation.DefaultFortnite;
import io.github.robertograham.fortnite2.xmpp.implementation.DefaultFortniteXmpp;

public final class Main {

    public static void main(final String[] args) {
        final var fortnite = DefaultFortnite.Builder.newInstance("epicGamesEmailAddress", "epicGamesPassword")
            .build();
        final var fortniteXmpp = DefaultFortniteXmpp.Builder.newInstance(fortnite)
            .build();
        fortniteXmpp.close();
        fortnite.close();
    }
}
```