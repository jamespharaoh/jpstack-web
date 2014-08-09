package wbs.framework.web;

public
class HttpStatus {

	private
	HttpStatus () {
		// never instantiated
	}

	public final static
	int

		httpContinue = 100,
		httpSwitchingProtocols = 101;

	public final static
	int

		httpOk = 200,
		httpCreated = 201,
		httpAccepted = 202,
		httpNonAuthoritativeInformation = 203,
		httpNoContent = 204,
		httpResetContent = 205,
		httpPartialContent = 206;

	public final static
	int

		httpMultipleChoices = 300,
		httpMovedPermanently = 301,
		httpFound = 302,
		httpSeeOther = 303,
		httpNotModified = 304,
		httpUseProxy = 305,
		httpTemporaryRedirect = 307;

	public final static
	int

		httpBadRequest = 400,
		httpUnauthorized = 401,
		httpPaymentRequired = 402,
		httpForbidden = 403,
		httpNotFound = 404,
		httpMethodNotAllowed = 405,
		httpNotAcceptable = 406,
		httpProxyAuthenticationRequired = 407,
		httpRequestTimeout = 408,
		httpConflict = 409,
		httpGone = 410,
		httpLengthRequired = 411,
		httpPreconditionFailed = 412,
		httpRequestEntityTooLarge = 413,
		httpRequestUriTooLong = 414,
		httpUnsupportedMediaType = 415,
		httpRequestedRangeNotSatisfiable = 416,
		httpExpectationFailed = 417;

	public final static
	int

		httpInternalServerError = 500,
		httpNotImplemented = 501,
		httpBadGateway = 502,
		httpServiceUnavailable = 503;

}
