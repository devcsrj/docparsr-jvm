# Parsr

![](https://img.shields.io/travis/devcsrj/docparsr-jvm)
![](https://img.shields.io/github/license/devcsrj/docparsr-jvm)
![](https://img.shields.io/maven-central/v/com.github.devcsrj/docparsr)

This project is a JVM client for [Axa group's Parsr](https://github.com/axa-group/Parsr) project.

## Download

Grab via Maven:

```
<dependency>
    <groupId>com.github.devcsrj</groupId>
    <artifactId>docparsr</artifactId>
    <version>(version)</version>
</dependency>
```

or Gradle:

```
implementation("com.github.devcsrj:docparsr:$version")
```

## Usage

`TBD`

## Building

Like any other [gradle](https://github.com/axa-group/Parsr)-based project, you can build the artifacts
with:

```
$ ./gradlew build
```

This project also includes functional test, which runs against an actual Parsr server. Assuming
you have [Docker](https://www.docker.com/) installed, run the tests with:

```
$ ./gradlew functionalTest
```

## Motivation

When I was working on the [Klerk](https://github.com/devcsrj/klerk) project, I realized how difficult
and time-consuming it is to scrape data from PDF documents. My approach then also involved the use of
heavy witchcraft using [Tesseract](https://github.com/tesseract-ocr), because typical PDF-to-text libraries
just don't cut it (especially on skewed, or garbled sections).

The [Parsr project](https://github.com/axa-group/Parsr) seems to also tackle the challenges I faced,
and more. To keep the data extraction out of my [Beam](https://beam.apache.org/) pipeline, I wrote this
library.