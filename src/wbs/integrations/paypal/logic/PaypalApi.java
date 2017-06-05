package wbs.integrations.paypal.logic;

import java.util.Map;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;

public
interface PaypalApi {

	Optional <String> setExpressCheckout (
			TaskLogger parentTaskLogger,
			String currency,
			String amount,
			String returnUrl,
			String cancelUrl,
			String checkoutUrl,
			Map <String, String> expressCheckoutProperties)
		throws InterruptedException;

	Optional <String> getExpressCheckout (
			TaskLogger parentTaskLogger,
			String paypalToken,
			Map <String, String> expressCheckoutProperties)
		throws InterruptedException;

	Boolean doExpressCheckout (
			TaskLogger parentTaskLogger,
			String paypalToken,
			String payerId,
			String currency,
			String amount,
			Map <String, String> expressCheckoutProperties)
		throws InterruptedException;

}
