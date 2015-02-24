package wbs.paypal.logic;

import java.util.Map;

import wbs.paypal.model.PaypalAccountRec;

public
interface PaypalLogic {

	Map<String,String> expressCheckoutProperties (
			PaypalAccountRec paypalAccount);

}
