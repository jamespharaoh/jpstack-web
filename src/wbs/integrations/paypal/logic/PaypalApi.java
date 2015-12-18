package wbs.integrations.paypal.logic;

import java.util.Map;

import com.google.common.base.Optional;

public
interface PaypalApi {

	Optional<String> setExpressCheckout (
			String amount,
			String returnUrl,
			String cancelUrl,
			String checkoutUrl,
			Map<String,String> expressCheckoutProperties);

	Optional<String> getExpressCheckout (
			String paypalToken,
			Map<String,String> expressCheckoutProperties);

	Boolean doExpressCheckout (
			String paypalToken,
			String payerId,
			String amount,
			Map<String,String> expressCheckoutProperties);

}
