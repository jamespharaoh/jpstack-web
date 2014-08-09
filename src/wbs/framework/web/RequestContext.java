package wbs.framework.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.log4j.Logger;

public interface RequestContext {

	HttpServletRequest request ();

	HttpServletResponse response ();

	ServletContext context ();

	Object context (
			String key);

	Object request (
			String key);

	String applicationPathPrefix ();

	String resolveApplicationUrl (
			String applicationUrl);

	InputStream inputStream ()
		throws IOException;

	String method ();

	String parameter (
			String key);

	String parameter (
			String key,
			String defaultValue);

	int parameterInt (
			String key);

	Map<String,List<String>> parameterMap ();

	Map<String,String> parameterMapSimple ();

	Enumeration<String> parameterNames ();

	boolean parameterOn (
			String key);

	String[] parameterValues (
			String name);

	String pathInfo ();

	Reader reader ()
		throws IOException;

	Integer requestInt (
			String key);

	int requestInt (
			String key,
			int defaultValue);

	RequestDispatcher requestDispatcher (
			String path);

	String requestUri ();

	String servletPath ();

	HttpSession session ();

	Object session (
			String key);

	String sessionId ();

	PrintWriter writer ();

	void sendError (
			int statusCode)
		throws IOException;

	void sendError (
			int statusCode,
			String message)
		throws IOException;

	void sendRedirect (
			String location)
		throws IOException;

	void request (
			String key,
			Object value);

	void session (
			String key,
			Object value);

	boolean isCommitted ();

	void status (
			int status);

	void setHeader (
			String name,
			String value);

	void addHeader (
			String name,
			String value);

	OutputStream outputStream ()
		throws IOException;

	String requestPath ();

	boolean canGetWriter ();

	void debugParameters (
			Logger logger);

	ServletRequestContext getFileUploadServletRequestContext ();

	boolean isMultipart ();

	List<FileItem> fileItems ()
		throws FileUploadException;

	String header (
			String name);

	void debugDump (
			Logger logger);

	void debugDump (
			Logger logger,
			boolean doFiles);

	boolean post ();

	String realPath (
			String string);

	InputStream resourceAsStream (
			String path);

}