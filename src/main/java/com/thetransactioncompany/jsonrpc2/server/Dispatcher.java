package com.thetransactioncompany.jsonrpc2.server;


import java.util.*;

import com.thetransactioncompany.jsonrpc2.*;


/**
 * Dispatcher for JSON-RPC 2.0 requests and notifications.
 *
 * <p>Use the {@code register()} methods to add a request or notification
 * handler for an RPC method.
 *
 * <p>Use the {@code dispatch()} methods to have an incoming request or
 * notification processed by the matching handler.
 *
 * <p>The {@code reportProcTime()} method enables reporting of request 
 * processing time (in microseconds) by appending a non-standard "xProcTime" 
 * attribute to the resulting JSON-RPC 2.0 response message.
 *
 * <p>Example:
 *
 * <pre>
 * { 
 *   "result"    : "xyz",
 *   "id"        : 1,
 *   "jsonrpc"   : "2.0",
 *   "xProcTime" : "189 us"
 * }
 * </pre>
 *
 * @author Vladimir Dzhuvinov
 * @version 1.5.1 (2011-07-15)
 */
public class Dispatcher {
	
	
	/** 
	 * Hashtable of request name / handler pairs. 
	 */
	private Hashtable<String,RequestHandler> requestHandlers;
	
	
	/**
	 * Hashtable of notification name / handler pairs.
	 */
	private Hashtable<String,NotificationHandler> notificationHandlers;
	
	
	/**
	 * Controls reporting of request processing time by appending a 
	 * non-standard "xProcTime" attribute to the JSON-RPC 2.0 response.
	 */
	private boolean reportProcTime = false;
	
	
	/**
	 * Creates a new dispatcher with no registered handlers.
	 */
	public Dispatcher() {
	
		requestHandlers = new Hashtable<String,RequestHandler>();
		notificationHandlers = new Hashtable<String,NotificationHandler>();
	}
	
	
	/**
	 * Registers a new JSON-RPC 2.0 request handler.
	 *
	 * @param handler The request handler to register.
	 *
	 * @throws IllegalArgumentException On attempting to register a handler
	 *                                  that duplicates an existing request
	 *                                  name.
	 */
	public void register(final RequestHandler handler) {
	
		for (String name: handler.handledRequests()) {
		
			if (requestHandlers.containsKey(name))
				throw new IllegalArgumentException("Cannot register a duplicate handler for request " + name);
		
			requestHandlers.put(name, handler);
		}
	}
	
	
	/**
	 * Registers a new JSON-RPC 2.0 notification handler.
	 *
	 * @param handler The notification handler to register.
	 *
	 * @throws IllegalArgumentException On attempting to register a handler
	 *                                  that duplicates an existing
	 *                                  notification name.
	 */
	public void register(final NotificationHandler handler) {
	
		for (String name: handler.handledNotifications()) {
		
			if (notificationHandlers.containsKey(name))
				throw new IllegalArgumentException("Cannot register a duplicate handler for notification " + name);
		
			notificationHandlers.put(name, handler);
		}
	}
	
	
	/**
	 * Returns the registered request names.
	 *
	 * @return The request names.
	 */
	public String[] handledRequests() {
	
		return requestHandlers.keySet().toArray(new String[0]);
	}
	
	
	/**
	 * Returns the registered notification names.
	 *
	 * @return The notification names.
	 */
	public String[] handledNotifications() {
	
		return notificationHandlers.keySet().toArray(new String[0]);
	}
	
	
	/**
	 * Gets the handler for the specified JSON-RPC 2.0 request name.
	 *
	 * @param requestName The request name to lookup.
	 *
	 * @return The corresponding request handler or {@code null} if none 
	 *         was found.
	 */
	public RequestHandler getRequestHandler(final String requestName) {
	
		return requestHandlers.get(requestName);
	}
	
	
	/**
	 * Gets the handler for the specified JSON-RPC 2.0 notification name.
	 *
	 * @param notificationName The notification name to lookup.
	 *
	 * @return The corresponding notification handler or {@code null} if
	 *         none was found.
	 */
	public NotificationHandler getNotificationHandler(final String notificationName) {
	
		return notificationHandlers.get(notificationName);
	}
	
	
	/**
	 * Dispatches the specified JSON-RPC 2.0 request to the appropriate 
	 * handler for processing and returns the response.
	 *
	 * @param request    The JSON-RPC 2.0 request to dispatch for 
	 *                   processing.
	 * @param requestCtx Context information about the request, may be 
	 *                   {@code null} if undefined.
	 *
	 * @return The response, which may indicate a processing error, such
	 *         as METHOD_NOT_FOUND.
	 */
	public JSONRPC2Response dispatch(final JSONRPC2Request request, final MessageContext requestCtx) {
	
		long startNanosec = 0;
		
		// Measure request processing time?
		if (reportProcTime)
			startNanosec = System.nanoTime();
		
	
		final String method = request.getMethod();
		
		RequestHandler handler = getRequestHandler(method);
		
		if (handler == null) {
		
			// We didn't find a handler for the requested RPC
		
			Object id = request.getID();
			
			return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, id);
		}
			
		// Process the request
		
		JSONRPC2Response response = handler.process(request, requestCtx);
		
		if (reportProcTime) {
		
			final long procTimeNanosec = System.nanoTime() - startNanosec;
			
			response.appendNonStdAttribute("xProcTime", procTimeNanosec / 1000 + " us");
		}
		
		return response;
	}
	
	
	/**
	 * Dispatches the specified JSON-RPC 2.0 notification to the appropriate 
	 * handler for processing.
	 *
	 * <p>Note that JSON-RPC 2.0 notifications don't produce a response!
	 *
	 * @param notification    The JSON-RPC 2.0 notification to dispatch for 
	 *                        processing.
	 * @param notificationCtx Context information about the notification,
	 *                        may be {@code null} if undefined.
	 */
	public void dispatch(final JSONRPC2Notification notification, final MessageContext notificationCtx) {
	
		final String method = notification.getMethod();
		
		NotificationHandler handler = getNotificationHandler(method);
		
		if (handler == null) {
		
			// We didn't find a handler for the requested RPC
			return;
		}
			
		// Process the notification
		
		handler.process(notification, notificationCtx);
	}
	
	
	/**
	 * Controls reporting of request processing time by appending a 
	 * non-standard "xProcTime" attribute to the JSON-RPC 2.0 response.
	 * Reporting is disabled by default.
	 *
	 * @param enable {@code true} to enable proccessing time reporting, 
	 *               {@code false} to disable it.
	 */
	public void reportProcTime(final boolean enable) {
	
		reportProcTime = enable;
	}
	
	
	/**
	 * Returns {@code true} if reporting of request processing time is 
	 * enabled. See the {@link #reportProcTime} description for more
	 * information.
	 *
	 * @return {@code true} if reporting of request processing time is 
	 *         enabled, else {@code false}.
	 */
	public boolean reportsProcTime() {
	
		return reportProcTime;
	}
}
