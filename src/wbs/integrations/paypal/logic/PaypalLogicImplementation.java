package wbs.integrations.paypal.logic;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.integrations.paypal.model.PaypalAccountRec;

@SingletonComponent ("paypalApiLogic")
public
class PaypalLogicImplementation
	implements PaypalLogic {

	@Override
	public
	Map<String,String> expressCheckoutProperties (
			PaypalAccountRec paypalAccount) {

		return ImmutableMap.<String,String>builder ()

			.putAll (
				accountSettings (
					paypalAccount))

			.putAll (
				httpSettings)

			.putAll (
				serviceSettings (
					paypalAccount))

			.build ();

	}

	Map<String,String> accountSettings (
			PaypalAccountRec paypalAccount) {

		return ImmutableMap.<String,String>builder ()

			.put (
				"mode",
				paypalAccount.getMode ())

			.put (
				"acct1.UserName",
				paypalAccount.getUsername ())

			.put (
				"acct1.Password",
				paypalAccount.getPassword ())

			.put (
				"acct1.Signature",
				paypalAccount.getSignature ())

			.put (
				"acct1.AppId",
				paypalAccount.getAppId ())

			.build ();

	}

	Map<String,String> serviceSettings (
			PaypalAccountRec paypalAccount) {

		return ImmutableMap.<String,String> builder ()

			.put (
				"service.RedirectURL",
				"https://www.sandbox.paypal.com/webscr&cmd=")

			.put (
				"service.DevCentralURL",
				"https://developer.paypal.com")

			.put (
				"service.IPNEndpoint",
				"https://ipnpb.sandbox.paypal.com/cgi-bin/webscr")

			.put (
				"service.EndPoint.PayPalAPI",
				paypalAccount.getServiceEndpointPaypalApi ())

			.put (
				"service.EndPoint.PayPalAPIAA",
				paypalAccount.getServiceEndpointPaypalApiAa ())

			.put (
				"service.EndPoint.Permissions",
				paypalAccount.getServiceEndpointPermissions ())

			.put (
				"service.EndPoint.AdaptivePayments",
				paypalAccount.getServiceEndpointAdaptivePayments ())

			.put (
				"service.EndPoint.AdaptiveAccounts",
				paypalAccount.getServiceEndpointAdaptiveAccounts ())

			.put (
				"service.EndPoint.Invoice",
				paypalAccount.getServiceEndpointInvoice ())

			.build ();

	}

	Map<String,String> httpSettings =
		ImmutableMap.<String,String>builder ()

		.put (
			"http.ConnectionTimeOut",
			"5000")

		.put (
			"http.Retry",
			"2")

		.put (
			"http.ReadTimeOut",
			"30000")

		.put (
			"http.MaxConnection",
			"100")

		.put (
			"http.IPAddress",
			"127.0.0.1")

		.put (
			"http.UseProxy",
			"false")

		.put (
			"http.GoogleAppEngine",
			"false")

		.build ();

}

