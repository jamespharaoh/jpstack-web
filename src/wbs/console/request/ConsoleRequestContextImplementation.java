package wbs.console.request;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.orNull;
import static wbs.utils.etc.OptionalUtils.ifNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalValueEqualSafe;
import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.fileupload.FileItem;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextStuff;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.notice.ConsoleNoticeType;
import wbs.console.notice.ConsoleNotices;
import wbs.console.priv.UserPrivDataLoader;
import wbs.console.tab.Tab;
import wbs.console.tab.TabContext;
import wbs.console.tab.TabList;

import wbs.framework.component.annotations.ProxiedRequestComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;

import wbs.utils.etc.OptionalUtils;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.WriterFormatWriter;

import wbs.web.context.RequestContext;

/**
 * Extends RequestContext to provide loads of extra functionality useful in all
 * console code. This object is completely stateless, with all stateful
 * information being stored into and retrieved from the request, response and
 * context objects. As such an instance can be constructed at any time from
 * these three references and be functionally identical.
 *
 * The static factory function make (...) takes a RequestContext and returns it
 * as is if it is already a ConsoleRequestContext or constructs a new
 * ConsoleRequestContext from it if not.
 */
@Accessors (fluent = true)
@ProxiedRequestComponent (
	value = "consoleRequestContext",
	proxyInterface = ConsoleRequestContext.class)
public
class ConsoleRequestContextImplementation
	implements ConsoleRequestContext {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivDataLoader privChecker;

	@SingletonDependency
	RequestContext requestContext;

	// properties

	@Getter @Setter
	String foreignContextPath;

	@Getter @Setter
	String changedContextPath;

	// TODO store a single copy of us in request instead of allowing multiple
	// copies and sharing data via request

	// TODO remove anything which doesn't belong here

	// TODO use spring scopes to make much of this redundant

	@Override
	public
	String servletPath () {

		return requestContext
			.servletPath ();

	}

	@Override
	public
	void addNotice (
			@NonNull ConsoleNoticeType type,
			@NonNull String message) {

		ConsoleNotices.add (
			requestContext.request (),
			message,
			type);

	}

	@Override
	public
	void addNotices (
			@NonNull ConsoleNotices notices) {

		notices.notices ().forEach (
			notice ->
				addNotice (
					notice.type (),
					notice.html ()));

	}

	@Override
	public
	void addScript (
			@NonNull String script) {

		@SuppressWarnings ("unchecked")
		List<String> scripts =
			(List<String>)
			orNull (
				request (
					"scripts"));

		if (scripts == null) {

			scripts =
				new ArrayList<String> ();

			request (
				"scripts",
				scripts);

		}

		scripts.add (
			script);

	}

	@Override
	public
	void addWarning (
			@NonNull String message) {

		ConsoleNotices.addWarning (
			requestContext.request (),
			message);

	}

	@Override
	public
	String applicationPathPrefix () {

		return requestContext.applicationPathPrefix ();

	}

	@Override
	public
	String resolveContextUrl (
			@NonNull String contextUrl) {

		if (foreignContextPath () == null) {

			throw new IllegalStateException (
				stringFormat (
					"Unable due resolve a context URL, as there is no current ",
					"context."));

		}

		return joinWithoutSeparator (
			foreignContextPath (),
			contextUrl);

	}

	@Override
	public
	Optional<String> parameter (
			@NonNull String key) {

		return Optional.fromNullable (
			requestContext.parameterOrNull (
				key));

	}

	@Override
	public
	String parameterRequired (
			@NonNull String key) {

		String value =
			requestContext.parameterOrNull (
				key);

		if (
			isNull (
				value)
		) {

			throw new NoSuchElementException (
				stringFormat (
					"Required request parameter '%s' is not present",
					key));

		}

		return value;

	}

	@Override
	public
	String parameterOrEmptyString (
			@NonNull String key) {

		return emptyStringIfNull (
			requestContext.parameterOrNull (
				key));

	}

	@Override
	public
	Boolean parameterIsOn (
			@NonNull String key) {

		return optionalValueEqualSafe (
			parameter (
				key),
			"on");

	}

	@Override
	public
	String parameterOrNull (
			@NonNull String key) {

		return requestContext
			.parameterOrNull (key);

	}

	@Override
	public
	String parameterOrElse (
			@NonNull String key,
			@NonNull Supplier<String> orElse) {

		String value =
			requestContext.parameterOrNull (
				key);

		if (
			isNotNull (
				value)
		) {

			return value;

		} else {

			return orElse.get ();

		}

	}

	@Override
	public
	void session (
			@NonNull String key,
			Serializable object) {

		requestContext.session (
			key,
			object);

	}

	@Override
	public
	Serializable session (
			@NonNull String key) {

		return requestContext.session (
			key);

	}

	@Override
	public
	void flushNotices () {

		// TODO this is messy

		flushNotices (
			new WriterFormatWriter (
				writer ()));

	}

	@Override
	public
	void flushNotices (
			@NonNull FormatWriter formatWriter) {

		ConsoleNotices notices =
			(ConsoleNotices)
			requestContext.request ().getAttribute (
				"wbs.notices");

		if (notices != null) {

			notices.flush (
				formatWriter);

		}

	}

	@Override
	public
	PrintWriter writer () {

		return requestContext.writer ();

	}

	@Override
	public
	void flushScripts () {

		PrintWriter out =
			writer ();

		@SuppressWarnings ("unchecked")
		List <String> scripts =
			(List <String>)
			orNull (
				request (
					"scripts"));

		if (scripts == null)
			return;

		out.println ("<script language=\"JavaScript\">");

		for (String s : scripts) {
			out.println (s);
		}

		out.println ("</script>");

		scripts.clear ();

	}

	@Override
	public
	Object context (
			String key) {

		return requestContext
			.context (key);

	}

	@Override
	public
	List<String> getParameterValues (
			@NonNull String name) {

		return Arrays.asList (
			requestContext
				.request ()
				.getParameterValues (name));

	}

	@Override
	public
	Map <String, String> requestFormData () {

		return parameterMapSimple ();

	}

	@Override
	public
	void formData (
			@NonNull String name,
			@NonNull String value) {

		formData (
			ImmutableMap.copyOf (
				formData ().entrySet ().stream ()

			.map (
				entry ->
					new SimpleEntry<> (
						entry.getKey (),
						stringEqualSafe (entry.getKey (), name)
							? value
							: entry.getValue ()))

			.collect (
				Collectors.toMap (
					Map.Entry::getKey,
					Map.Entry::getValue))

		));

	}

	@Override
	public
	TabContext setTabContext (
			@NonNull TabContext tabContext) {

		request (
			"tabContext",
			tabContext);

		return tabContext;

	}

	@Override
	public
	void addTabContext (
			@NonNull Tab parentTab,
			@NonNull String title,
			@NonNull TabList tabList) {

		tabContextRequired ().add (
			parentTab,
			title,
			tabList);

	}

	@Override
	public
	void tabContext (
			@NonNull String title1,
			@NonNull TabList tabList1) {

		setTabContext (
			new TabContext (
				title1,
				tabList1));

	}

	@Override
	public
	String resolveLocalUrl (
			@NonNull String wantedPath) {

		if (! wantedPath.startsWith ("/")) {

			throw new IllegalArgumentException (
				stringFormat (
					"Invalid wanted path: %s",
					wantedPath));

		}

		if (isNotNull (
				changedContextPath ())) {

			return joinWithoutSeparator (
				applicationPathPrefix (),
				changedContextPath (),
				wantedPath);

		} else {

			return wantedPath.substring (1);

		}

	}

	@Override
	public
	void request (
			@NonNull String key,
			Object value) {

		requestContext.request (
			key,
			value);

	}

	@Override
	public
	void formData (
			@NonNull Map <String, String> newFormData) {

		request (
			"formData",
			newFormData);

	}

	@Override
	public
	void setEmptyFormData () {

		request (
			"formData",
			Collections.emptyMap ());

	}

	@Override
	public
	Map <String, String> formData () {

		@SuppressWarnings ("unchecked")
		Optional <Map <String, String>> formData =
			(Optional <Map <String, String>>)
			request (
				"formData");

		if (
			optionalIsPresent (
				formData)
		) {
			return formData.get ();
		}

		return requestFormData ();

	}

	@Override
	public
	boolean isCommitted () {

		return response ().isCommitted ();

	}

	@Override
	public
	void reset () {

		response ().reset ();

	}

	@Override
	public
	ConsoleContext consoleContext () {

		return (ConsoleContext)
			requestRequired (
				"context");

	}

	@Override
	public
	ConsoleContextStuff contextStuff () {

		return (ConsoleContextStuff)
			requestRequired (
				"contextStuff");

	}

	@Override
	public
	boolean canContext (
			@NonNull String... privKeys) {

		return contextStuff ()
			.can (privKeys);

	}

	@Override
	public
	boolean canView (
			@NonNull Record<?> object) {

		return objectManager
			.canView (object);

	}

	@Override
	public
	Long requestUnique () {

		Long unique =
			ifNotPresent (

			OptionalUtils.optionalMapRequired (
				requestInteger (
					"unique"),
				value ->
					value + 1l),

			Optional.of (
				0l)

		);

		request (
			"unique",
			unique);

		return unique;

	}

	@Override
	public
	void grant (
			@NonNull String string) {

		contextStuff ()
			.grant (string);

	}

	@Override
	public
	void addNotices (
			@NonNull List<String> notices) {

		for (String notice
				: notices) {

			addNotice (notice);

		}

	}

	@Override
	public
	void hideFormData (
			@NonNull Set<String> keys) {

		formData (
			ImmutableMap.<String,String>copyOf (
				Maps.filterKeys (
					formData (),
					Predicates.in (keys))));

	}

	@Override
	public
	String pathInfo () {

		return requestContext
			.pathInfo ();

	}

	@Override
	public
	boolean canGetWriter () {

		return requestContext
			.canGetWriter ();

	}

	@Override
	public
	void sendRedirect (
			@NonNull String location)
		throws IOException {

		requestContext
			.sendRedirect (location);

	}

	@Override
	public
	void status (
			int status) {

		requestContext
			.status (status);

	}

	@Override
	public
	String method () {

		return requestContext.method ();

	}

	@Override
	public
	String requestUri () {

		return requestContext.requestUri ();

	}

	@Override
	public
	Map<String,List<String>> parameterMap () {

		return requestContext.parameterMap ();

	}

	@Override
	public
	void setHeader (
			@NonNull String key,
			@NonNull String value) {

		requestContext.setHeader (
			key,
			value);

	}

	@Override
	public
	OutputStream outputStream ()
		throws IOException {

		return requestContext
			.outputStream ();

	}

	@Override
	public
	String parameterOrDefault (
			@NonNull String key,
			String defaultValue) {

		return requestContext
			.parameterOrDefault (
				key,
				defaultValue);

	}

	@Override
	public
	Optional <Long> requestInteger (
			@NonNull String key) {

		return requestContext.requestInteger (
			key);

	}

	@Override
	public
	Long parameterIntegerRequired (
			@NonNull String key) {

		return requestContext.parameterIntegerRequired (
			key);

	}

	@Override
	public
	String sessionId () {

		return requestContext
			.sessionId ();

	}

	@Override
	public
	Map<String,String> parameterMapSimple () {

		return requestContext
			.parameterMapSimple ();

	}

	@Override
	public
	RequestDispatcher requestDispatcher (
			@NonNull String path) {

		return requestContext
			.requestDispatcher (path);

	}

	@Override
	public
	String requestPath () {

		return requestContext
			.requestPath ();

	}

	@Override
	public
	boolean post () {

		return requestContext
			.post ();

	}

	@Override
	public
	InputStream getResourceAsStream (
			@NonNull String path) {

		return requestContext.resourceAsStream (
			path);

	}

	@Override
	public
	String realPath (
			@NonNull String path) {

		return requestContext.realPath (
			path);

	}

	@Override
	public
	HttpServletResponse response () {

		return requestContext.response ();

	}

	@Override
	public
	HttpServletRequest request () {

		return requestContext.request ();

	}

	@Override
	public
	String resolveApplicationUrl (
			@NonNull String applicationUrl) {

		return requestContext.resolveApplicationUrl (
			applicationUrl);

	}

	@Override
	public
	boolean isMultipart () {

		return requestContext.isMultipart ();

	}

	@Override
	public
	List<FileItem> fileItems () {

		return requestContext.fileItems ();

	}

	@Override
	public
	FileItem fileItemFile (
			String name) {

		return requestContext.fileItemFile (
			name);

	}

	@Override
	public
	String fileItemField (
			String name) {

		return requestContext.fileItemField (
			name);

	}

	@Override
	public
	Optional<String> header (
			String name) {

		return Optional.fromNullable (
			requestContext.header (
				name));

	}

	// request attributes

	@Override
	public
	Optional<?> request (
			@NonNull String key) {

		return requestContext.request (
			key);

	}

	@Override
	public
	Object requestRequired (
			@NonNull String key) {

		return requestContext.requestRequired (
			key);

	}

	@Override
	public
	Long requestIntegerRequired (
			@NonNull String key) {

		return requestContext.requestIntegerRequired (
			key);

	}

	@Override
	public
	Optional<String> requestString (
			@NonNull String key) {

		return requestContext.requestString (
			key);

	}

	@Override
	public
	String requestStringRequired (
			@NonNull String key) {

		return requestContext.requestStringRequired (
			key);

	}

}
