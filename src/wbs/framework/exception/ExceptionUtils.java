package wbs.framework.exception;

public
interface ExceptionUtils {

	String throwableSummary (
			Throwable throwable);

	String throwableDump (
			Throwable throwable);

}
