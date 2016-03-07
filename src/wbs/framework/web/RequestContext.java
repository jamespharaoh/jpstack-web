package wbs.framework.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.log4j.Logger;

import com.google.common.base.Optional;

public
interface RequestContext {

	HttpServletRequest request ();

	HttpServletResponse response ();

	ServletContext context ();

	Object context (
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

	boolean parameterOn (
			String key);

	String[] parameterValues (
			String name);

	String pathInfo ();

	Reader reader ();

	RequestDispatcher requestDispatcher (
			String path);

	String requestUri ();

	String servletPath ();

	HttpSession session ();

	Serializable session (
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

	void session (
			String key,
			Object value);

	boolean isCommitted ();

	void status (
			int status);

	Map<String,List<String>> headerMap ();

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

	List<FileItem> fileItems ();

	Map<String,FileItem> fileItemFiles ();
	Map<String,String> fileItemFields ();

	FileItem fileItemFile (
			 String name);

	String fileItemField (
			 String name);

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

	String realIp ();

	// request attributes

	void request (
			String key,
			Object value);

	Optional<Object> request (
			String key);

	Object requestRequired (
			String key);

	Optional<String> requestString (
			String key);

	String requestStringRequired (
			String key);

	Optional<Integer> requestInt (
			String key);

	Integer requestIntRequired (
			String key);

}
