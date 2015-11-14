package wbs.platform.exception.model;

public
enum ExceptionResolution {

	tryAgainNow,
	tryAgainLater,
	ignoreWithUserWarning,
	ignoreWithThirdPartyWarning,
	ignoreWithLoggedWarning,
	ignoreWithNoWarning,
	fatalError;

}
