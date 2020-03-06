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

Assuming you have the [API server running](https://github.com/axa-group/Parsr#usage), you can communicate 
with it using: 

```kotlin
val uri = URI.create("http://localhost:3001")
val parser = DocParsr.create(uri)
```

Then, submit your file with:

```kotlin
val file = File("hello.pdf")    // your pdf or image file
val config = Configuration()    // or, 'parser.getDefaultConfig()`
val job = parser.newParsingJob(file, config)
```

At this point, the `job` object presents you with either synchronous:

```kotlin
val result = job.execute()
``` 

...or asynchronous method:

```kotlin
val callback = object: Callback {
    fun onFailure(job: ParsingJob, e: Exception) {}
    fun onProgress(job: ParsingJob, progress: Progress) {}
    fun onSuccess(job: ParsingJob, result: ParsingResult) {}
}
job.enqueue(callback)
```

Regardless of the approach you choose, you end up with a `ParsingResult`. You can then
access the [various generated output](https://github.com/axa-group/Parsr/blob/master/docs/api-guide.md#3-get-the-results)
from the server with:

```kotlin
result.source(Text).use {
   // copy the InputStream
}
``` 

If you are instead interested on the [JSON schema](https://github.com/axa-group/Parsr/blob/master/docs/json-output.md), this
library provides a [Visitor](https://en.wikipedia.org/wiki/Visitor_pattern) -based API:

```kotlin
val visitor = object: DocumentVisitor {
   // override methods
}
val document = Document.from(result)
document.accept(visitor) 
```

## Building

Like any other [gradle](https://github.com/axa-group/Parsr) -based project, you can build the artifacts
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