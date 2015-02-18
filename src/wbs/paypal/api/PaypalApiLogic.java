package wbs.paypal.api;

import wbs.paypal.model.PaypalAccountRec;

public
interface PaypalApiLogic {

	PaypalAccountData paypalAccountData (
			PaypalAccountRec paypalAccount);

}
