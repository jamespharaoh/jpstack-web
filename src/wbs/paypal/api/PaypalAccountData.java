package wbs.paypal.api;

import java.util.Properties;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public class PaypalAccountData {

	@DataAttribute
	Integer id;

	@DataAttribute
	String code;

	@DataAttribute
	String name;

	@DataAttribute
	String description;
	
	@DataAttribute
	Boolean deleted;
	
	@DataAttribute
	String username;

	@DataAttribute
	String password;

	@DataAttribute
	String signature;

	@DataAttribute
	String apiId;
	
	@DataAttribute
	String serviceEndpointPaypalApi;
	
	@DataAttribute
	String serviceEndpointPaypalApiAa;

	@DataAttribute
	String serviceEndpointPermissions;

	@DataAttribute
	String serviceEndpointAdaptivePayments;

	@DataAttribute
	String serviceEndpointAdaptiveAccounts;
	
	@DataAttribute
	String serviceEndpointInvoice;
	
	public Properties generateProperties() {
			
			Properties expressCheckoutProperties = new Properties();
		
			expressCheckoutProperties.setProperty("acct1.UserName", username);
			expressCheckoutProperties.setProperty("acct1.Password", password);
			expressCheckoutProperties.setProperty("acct1.Signature", signature);
			expressCheckoutProperties.setProperty("acct1.AppId", apiId);
			
			expressCheckoutProperties.setProperty("http.ConnectionTimeOut", "5000");
			expressCheckoutProperties.setProperty("http.Retry", "2");
			expressCheckoutProperties.setProperty("http.ReadTimeOut", "30000");
			expressCheckoutProperties.setProperty("http.MaxConnection", "100");
			expressCheckoutProperties.setProperty("http.IPAddress", "127.0.0.1");
	
			expressCheckoutProperties.setProperty("http.UseProxy", "false");
			expressCheckoutProperties.setProperty("http.GoogleAppEngine", "false");
	
			expressCheckoutProperties.setProperty("service.RedirectURL", "https://www.sandbox.paypal.com/webscr&cmd=");
			expressCheckoutProperties.setProperty("service.DevCentralURL", "https://developer.paypal.com");
			expressCheckoutProperties.setProperty("service.IPNEndpoint", "https://ipnpb.sandbox.paypal.com/cgi-bin/webscr");
			
			expressCheckoutProperties.setProperty("service.EndPoint.PayPalAPI", serviceEndpointPaypalApi);
			expressCheckoutProperties.setProperty("service.EndPoint.PayPalAPIAA", serviceEndpointPaypalApiAa);
			expressCheckoutProperties.setProperty("service.EndPoint.Permissions", serviceEndpointPermissions);
			expressCheckoutProperties.setProperty("service.EndPoint.AdaptivePayments", serviceEndpointAdaptivePayments);
			expressCheckoutProperties.setProperty("service.EndPoint.AdaptiveAccounts", serviceEndpointAdaptiveAccounts);
			expressCheckoutProperties.setProperty("service.EndPoint.Invoice", serviceEndpointInvoice);
			
			return expressCheckoutProperties;
			
	}
	
}
