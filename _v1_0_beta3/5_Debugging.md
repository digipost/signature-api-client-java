---
id: Debugging
title: Debugging
layout: default
---


<h3 id="loggingrequestflow">Logging request and response</h3>

> Warning: Enabling request logging should never be used in a production system. It will impact the performance of the client.	

You may configure the client library to log HTTP requests and responses by calling `.enableRequestAndResponseLogging()` when creating the [client's configuration](#uc01). You may configure the logger `no.digipost.signature.client.http.requestresponse` in order to customize logging. It must be set to at least `INFO` to write anything to the log.


<h3 id="loggingdocumentbundle">Writing document bundle to disk</h3>

You may configure the client library to write a ZIP file with the document bundle by calling `.enableDocumentBundleDiskDump(Path)` when creating the [client's configuration](#uc01).

The [Path](https://docs.oracle.com/javase/7/docs/api/java/nio/file/Path.html) parameter is the directory to where the files will be written. This directory *must* exists as the client library won't try creating it.

If you have needs for the document bundle other than just saving it to disk, add your own document bundle processor by calling `.addDocumentBundleProcessor(…)` with your own `DocumentBundleProcessor` when creating the [client's configuration](#uc01).



