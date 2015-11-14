package wbs.framework.exception;

public
interface ExceptionLogic {

	String throwableSummary (
			Throwable throwable);

	String throwableDump (
			Throwable throwable);

}
