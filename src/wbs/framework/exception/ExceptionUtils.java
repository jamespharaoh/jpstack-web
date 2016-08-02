package wbs.framework.exception;

import org.json.simple.JSONObject;

public
interface ExceptionUtils {

	String throwableSummary (
			Throwable throwable);

	String throwableDump (
			Throwable throwable);

	JSONObject throwableDumpJson (
			Throwable throwable);

}
