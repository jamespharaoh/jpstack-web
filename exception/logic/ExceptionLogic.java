package wbs.platform.exception.logic;

public
interface ExceptionLogic {

	void logException (
			String typeCode,
			String source,
			String summary,
			String dump,
			Integer userId,
			boolean fatal);

	void logException (
			String typeCode,
			String source,
			Throwable throwable,
			Integer userId,
			boolean fatal);

	void logException (
			String typeCode,
			String source,
			String summary,
			Throwable throwable,
			Integer userId,
			boolean fatal);

}