package wbs.framework.processapi;

import static wbs.utils.etc.LogicUtils.notEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import lombok.NonNull;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.json.simple.JSONValue;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.data.tools.DataToSimple;

import wbs.web.misc.HttpStatus;

@SingletonComponent ("processApiStatusHandler")
public
class ProcessApiStatusHandler
	extends HttpHandler {

	@Override
	public
	void service (
			@NonNull Request request,
			@NonNull Response response)
		throws IOException {

		// check method

		if (

			notEqualSafe (
				request.getMethod (),
				Method.HEAD)

			&& notEqualSafe (
				request.getMethod (),
				Method.GET)

		) {

			response.sendError (
				toJavaIntegerRequired (
					HttpStatus.httpMethodNotAllowed),
				"Method not allowed");

			return;

		}

		// create response

		ProcessApiStatusResponse.Builder statusResponseBuilder =
			new ProcessApiStatusResponse.Builder ();

		// check memory usage

		checkHeapMemoryUsage (
			statusResponseBuilder);

		checkNonHeapMemoryUsage (
			statusResponseBuilder);

		// send response

		DataToSimple dataToJson =
			new DataToSimple ();

		Object jsonResponseObject =
			dataToJson.toJson (
				statusResponseBuilder.build ());

		StringWriter stringWriter =
			new StringWriter ();

		JSONValue.writeJSONString (
			jsonResponseObject,
			stringWriter);

		String jsonResponseString =
			stringWriter.toString ();

		response.setContentType (
			"application/json");

		response.setCharacterEncoding (
			"utf-8");

		response.setContentLength (
			jsonResponseString.length ());

		response.getWriter ().write (
			jsonResponseString);

	}

	private
	void checkHeapMemoryUsage (
			@NonNull ProcessApiStatusResponse.Builder statusResponseBuilder) {

		MemoryMXBean memoryMxBean =
			ManagementFactory.getMemoryMXBean ();

		MemoryUsage heapMemoryUsage =
			memoryMxBean.getHeapMemoryUsage ();

		long heapMemoryUsageRatio =
			10000
				* heapMemoryUsage.getUsed ()
				/ heapMemoryUsage.getMax ();

		String heapMemoryUsageString =
			stringFormat (
				"Heap memory %s of %s MiB or %s%%",
				integerToDecimalString (
					heapMemoryUsage.getUsed () / 1024 / 1024),
				integerToDecimalString (
					heapMemoryUsage.getMax () / 1024 / 1024),
				integerToDecimalString (
					heapMemoryUsageRatio / 100));

		if (heapMemoryUsageRatio < 6000) {

			statusResponseBuilder.okFormat (
				"%s",
				heapMemoryUsageString);

		} else if (heapMemoryUsageRatio < 8000) {

			statusResponseBuilder.warningFormat (
				"%s (warning is 60%%)",
				heapMemoryUsageString);

		} else {

			statusResponseBuilder.criticalFormat (
				"%s (critical is 80%%)",
				heapMemoryUsageString);

		}

	}

	private
	void checkNonHeapMemoryUsage (
			@NonNull ProcessApiStatusResponse.Builder statusResponseBuilder) {

		MemoryMXBean memoryMxBean =
			ManagementFactory.getMemoryMXBean ();

		MemoryUsage nonHeapMemoryUsage =
			memoryMxBean.getNonHeapMemoryUsage ();

		String nonHeapMemoryUsageString =
			stringFormat (
				"Non-heap memory %s MiB",
				integerToDecimalString (
					nonHeapMemoryUsage.getUsed () / 1024 / 1024));

		statusResponseBuilder.okFormat (
			"%s",
			nonHeapMemoryUsageString);

	}

}
