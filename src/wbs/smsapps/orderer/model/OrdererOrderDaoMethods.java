package wbs.smsapps.orderer.model;

import java.util.List;

import wbs.sms.number.core.model.NumberRec;

public
interface OrdererOrderDaoMethods {

	List<OrdererOrderRec> find (
			OrdererRec orderer,
			NumberRec number);

}