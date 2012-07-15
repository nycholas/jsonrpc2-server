JSON-RPC 2.0 Server

Copyright (c) Vladimir Dzhuvinov, 2010 - 2012

README

Simple server framework for processing JSON-RPC 2.0 requests and notifications.


Usage:

	1. Implement request and / or notification handlers for the various 
	   expected JSON-RPC 2.0 messages. A handler may process one or more 
	   request/notification methods (identified by method name).
	   
	2. Create a new Dispatcher and register the handlers with it.
	
	3. Pass the received JSON-RPC 2.0 requests and notifications to the 
	   appropriate Dispatcher.dispatch(...) method, then, if the message is 
	   a request, pass back to the client the resulting JSON-RPC 2.0 
	   response. 


Package requirements:

	* Java 1.5 or later
	
	* JSON-RPC 2.0 Base for creating and representing JSON-RPC 2.0 messages.
	  This package is included in the lib/ directory. Online documentation 
	  is at http://software.dzhuvinov.com/json-rpc-2.0-base.html.
	
	* Java Servlet API for constructing MessageContext objects from HTTP 
	  servlet requests. 


Package content:

	README.txt                    This file.
	
	LICENSE.txt                   The software license.
	
	CHANGELOG.txt                 The change log.
	
	jsonrpc2-server-{version}.jar JAR file containing the compiled package
	                              files.
	
	Example.java                  Example program.
	
	javadoc/                      The Java Docs for this package.
	
	build.xml                     Apache Ant build file.
	
	lib/                          The package dependencies.
	
	src/                          The full source code for this package.



For complete JSON-RPC 2.0 Server documentation, examples and updates visit

	http://software.dzhuvinov.com/json-rpc-2.0-server.html


The JSON-RPC 2.0 specification and user group forum can be found at

        http://groups.google.com/group/json-rpc

[EOF]
