package wbs.console.request;

import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrElse;
import static wbs.utils.etc.OptionalUtils.optionalOrThrow;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.fileupload.FileItem;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextStuff;
import wbs.console.notice.ConsoleNoticeType;
import wbs.console.notice.ConsoleNotices;
import wbs.console.tab.Tab;
import wbs.console.tab.TabContext;
import wbs.console.tab.TabList;
import wbs.framework.entity.record.Record;
import wbs.utils.string.FormatWriter;

public
interface ConsoleRequestContext {

	public final static
	Pattern codePattern =
		Pattern.compile ("^[a-z0-9_]+$");

	HttpServletRequest request ();
	HttpServletResponse response ();

	String servletPath ();

	// ========== notices

	void addNotice (
			ConsoleNoticeType type,
			String message);

	void addNotices (
			ConsoleNotices notices);

	default
	void addNoticeFormat (
			@NonNull ConsoleNoticeType type,
			@NonNull String ... arguments) {

		addNotice (
			type,
			stringFormatArray (
				arguments));

	}

	default
	void addNotice (
			@NonNull String message) {

		addNotice (
			ConsoleNoticeType.notice,
			message);

	}

	default
	void addNoticeFormat (
			@NonNull String ... arguments) {

		addNoticeFormat (
			ConsoleNoticeType.notice,
			arguments);

	}

	default
	void addWarning (
			@NonNull String message) {

		addNotice (
			ConsoleNoticeType.warning,
			message);

	}

	default
	void addWarningFormat (
			@NonNull String ... arguments) {

		addNoticeFormat (
			ConsoleNoticeType.warning,
			arguments);

	}

	default
	void addError (
			@NonNull String message) {

		addNotice (
			message);

	}

	default
	void addErrorFormat (
			@NonNull String ... arguments) {

		addNoticeFormat (
			ConsoleNoticeType.error,
			arguments);

	}

	// =========== scripts

	void addScript (
			String script);

	default
	void addScriptFormat (
			@NonNull String ... arguments) {

		addScript (
			stringFormatArray (
				arguments));

	}

	String applicationPathPrefix ();

	String resolveApplicationUrl (
			String applicationUrl);

	String resolveContextUrl (
			String contextUrl);

	@Deprecated
	String parameterOrNull (
			String key);

	String parameterRequired (
			String key);

	String parameterOrEmptyString (
			String key);

	Boolean parameterIsOn (
			String key);

	void session (
			String key,
			Serializable object);

	Serializable session (
			String key);

	void flushNotices ();

	void flushNotices (
			FormatWriter formatWriter);

	PrintWriter writer ();

	void flushScripts ();

	Object context (
			String key);

	List <String> getParameterValues (
			String name);

	Map <String, String> requestFormData ();

	void formData (
			String name,
			String value);

	TabContext setTabContext (
			TabContext tabContext);

	void addTabContext (
			Tab parentTab,
			String title,
			TabList tabList);

	default
	Optional <TabContext> tabContext () {

		return optionalCast (
			TabContext.class,
			request (
				"tabContext"));

	}

	default
	TabContext tabContextRequired () {

		return (TabContext)
			requestRequired (
				"tabContext");

	}

	void tabContext (
			String title1,
			TabList tabList1);

	String resolveLocalUrl (
			String wantedPath);

	void formData (
			Map <String, String> newFormData);

	void setEmptyFormData ();

	Map <String, String> formData ();

	default
	Optional <String> form (
			@NonNull String key) {

		return optionalFromNullable (
			formData ().get (
				key));

	}

	default
	boolean formIsPresent (
			@NonNull String key) {

		return optionalIsPresent (
			form (key));

	}

	default
	String formRequired (
			String key) {

		return optionalOrThrow (
			form (key),
			() -> new NoSuchElementException (
				stringFormat (
					"Form data does not contain key: %s",
					key)));


	}

	default
	String formOrElse (
			@NonNull String key,
			@NonNull Supplier <String> defaultSupplier) {

		return optionalOrElse (
			form (key),
			defaultSupplier);

	}

	default
	String formOrDefault (
			@NonNull String key,
			@NonNull String defaultValue) {

		return optionalOrElse (
			form (key),
			() -> defaultValue);

	}

	default
	String formOrEmptyString (
			@NonNull String key) {

		return formOrElse (
			key,
			() -> "");

	}

	boolean isCommitted ();

	void reset ();

	ConsoleContext consoleContext ();

	ConsoleContextStuff contextStuff ();

	default
	Object stuff (
			@NonNull String key) {

		return contextStuff ()
			.get (key);

	}

	default
	Long stuffInteger (
			@NonNull String key) {

		return (Long)
			contextStuff ().get (
				key);

	}

	default
	String stuffString (
			@NonNull String key) {

		return (String)
			contextStuff ().get (
				key);

	}

	boolean canContext (
			String... privKeys);

	boolean canView (
			Record<?> object);

	Long requestUnique ();

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

	Map <String, List <String>> parameterMap ();

	void setHeader (
			String key,
			String value);

	OutputStream outputStream ()
		throws IOException;

	Optional <String> parameter (
			String key);

	String parameterOrDefault (
			String key,
			String defaultValue);

	String parameterOrElse (
			String key,
			Supplier <String> orElse);

	Long parameterIntegerRequired (
			String key);

	String sessionId ();

	Map <String, String> parameterMapSimple ();

	RequestDispatcher requestDispatcher (
			String path);

	String requestPath ();

	boolean post ();

	InputStream getResourceAsStream (
			String path);

	String realPath (
			String string);

	String foreignContextPath ();

	ConsoleRequestContext foreignContextPath (
			String path);

	String changedContextPath ();

	ConsoleRequestContext changedContextPath (
			String path);

	boolean isMultipart ();

	List <FileItem> fileItems ();

	FileItem fileItemFile (
			 String name);

	String fileItemField (
			 String name);

	// http headers

	Optional <String> header (
			String name);

	// request attributes

	void request (
			String key,
			Object value);

	Optional <?> request (
			String key);

	Object requestRequired (
			String key);

	Optional <Long> requestInteger (
			String key);

	Long requestIntegerRequired (
			String key);

	Optional <String> requestString (
			String key);

	String requestStringRequired (
			String key);

}