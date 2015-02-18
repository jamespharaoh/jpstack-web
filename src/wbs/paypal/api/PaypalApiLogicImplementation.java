package wbs.paypal.api;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.paypal.model.PaypalAccountRec;


@SingletonComponent ("paypalApiLogic")
public class PaypalApiLogicImplementation
	implements PaypalApiLogic {

	
	// implementation
	
	@Override
	public
	PaypalAccountData paypalAccountData (
			PaypalAccountRec paypalAccount) {

		return new PaypalAccountData ()
	
			.id (
				paypalAccount.getId ())
	
			.code (
				paypalAccount.getCode())
				
			.name (
				paypalAccount.getName ())	
				
			.description (
				paypalAccount.getDescription ())
				
			.deleted (
				paypalAccount.getDeleted ())
				
			.username (
				paypalAccount.getUsername ())
	
			.password (
				paypalAccount.getPassword ())
				
			.signature (
				paypalAccount.getSignature ())	
				
			.apiId (
				paypalAccount.getApiId ())
				
			.serviceEndpointPaypalApi (
				paypalAccount.getServiceEndpointPaypalApi ())

			.serviceEndpointPaypalApiAa (
				paypalAccount.getServiceEndpointPaypalApiAa ())
	
			.serviceEndpointPermissions (
				paypalAccount.getServiceEndpointPermissions ())
				
			.serviceEndpointAdaptivePayments (
				paypalAccount.getServiceEndpointAdaptivePayments ())	
				
			.serviceEndpointAdaptiveAccounts (
				paypalAccount.getServiceEndpointAdaptiveAccounts ())
				
			.serviceEndpointInvoice (
				paypalAccount.getServiceEndpointInvoice ());
	
	}
	
}
