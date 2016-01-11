package wbs.console.request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.joda.time.Instant;

import com.google.common.base.Optional;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextStuff;
import wbs.console.tab.Tab;
import wbs.console.tab.TabContext;
import wbs.console.tab.TabList;
import wbs.framework.record.Record;

public
interface ConsoleRequestContext {

	public final static
	Pattern codePattern =
		Pattern.compile ("^[a-z0-9_]+$");

	HttpServletRequest request ();
	HttpServletResponse response ();

	Object request (
			String key);

	String servletPath ();

	void addError (
			String message);

	void addNotice (
			String message);

	void addNotice (
			String message,
			String type);

	void addScript (
			String script);

	void addWarning (
			String message);

	String applicationPathPrefix ();

	String resolveApplicationUrl (
			String applicationUrl);

	String resolveContextUrl (
			String contextUrl);

	String parameter (
			String key);

	void session (
			String key,
			Serializable object);

	Serializable session (
			String key);

	void copyIntParamToSession (
			String name);

	void flushNotices ();

	void flushNotices (
			PrintWriter out);

	PrintWriter writer ();

	void flushScripts ();

	Object context (
			String key);

	List<String> getParameterValues (
			String name);

	Map<String,String> requestFormData ();

	String magicTdCheck (
			String name,
			String label,
			boolean value,
			int colspan);

	String magicTdCheck (
			String name,
			String label,
			boolean value);

	void magicTdRadio (
			String name,
			String value,
			String label,
			boolean selected,
			Map<String,Object> options);

	void magicTdRadio (
			String name,
			String value,
			String label,
			boolean selected);

	TabContext setTabContext (
			TabContext tabContext);

	void addTabContext (
			Tab parentTab,
			String title,
			TabList tabList);

	TabContext tabContext ();

	void tabContext (
			String title1,
			TabList tabList1);

	String resolveLocalUrl (
			String wantedPath);

	String prettyDateDiff (
			Instant before,
			Instant after);

	String prettyMsInterval (
			Long interval);

	String prettyMsInterval (
			Integer interval);

	String prettySecsInterval (
			Long interval);

	String prettySecsInterval (
			Integer interval);

	void request (
			String key,
			Object value);

	void formData (
			Map<String,String> newFormData);

	void setEmptyFormData ();

	Map<String,String> getFormData ();

	String getForm (
			String key);

	String getForm (
			String key,
			String def);

	boolean isCommitted ();

	void reset ();

	ConsoleContext consoleContext ();

	ConsoleContextStuff contextStuff ();

	Integer stuffInt (
			String key);

	Object stuff (
			String key);

	boolean canContext (
			String... privKeys);

	boolean canView (
			Record<?> object);

	int requestUnique ();

	void grant (
			String string);

	void addNotices (
			List<String> notices);

	void hideFormData (
			Set<String> keys);

	String pathInfo ();

	boolean canGetWriter ();

	void sendRedirect (
			String location)
		throws IOException;

	void status (
			int status);

	String method ();

	String requestUri ();

	Map<String,List<String>> parameterMap ();

	void setHeader (
			String key,
			String value);

	OutputStream outputStream ()
		throws IOException;

	String parameter (
			String key,
			String defaultValue);

	int requestInt (
			String key);

	int parameterInt (
			String key);

	String sessionId ();

	Map<String,String> parameterMapSimple ();

	RequestDispatcher requestDispatcher (
			String path);

	String requestPath ();

	boolean post ();

	InputStream getResourceAsStream (
			String path);

	Integer userId ();

	String realPath (
			String string);

	String foreignContextPath ();

	ConsoleRequestContext foreignContextPath (
			String path);

	String changedContextPath ();

	ConsoleRequestContext changedContextPath (
			String path);

	boolean isMultipart ();

	List<FileItem> fileItems ();

	FileItem fileItemFile (
			 String name);

	String fileItemField (
			 String name);

	Optional<String> header (
			String name);

}