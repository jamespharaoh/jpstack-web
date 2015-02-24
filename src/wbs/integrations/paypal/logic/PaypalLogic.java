package wbs.integrations.paypal.logic;

import java.util.Map;

import wbs.integrations.paypal.model.PaypalAccountRec;

public
interface PaypalLogic {

	Map<String,String> expressCheckoutProperties (
			PaypalAccountRec paypalAccount);

}
