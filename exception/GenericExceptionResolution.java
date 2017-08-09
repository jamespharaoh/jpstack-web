package wbs.framework.exception;

public
enum GenericExceptionResolution {

	tryAgainNow,
	tryAgainLater,
	ignoreWithUserWarning,
	ignoreWithThirdPartyWarning,
	ignoreWithLoggedWarning,
	ignoreWithNoWarning,
	fatalError;

}
