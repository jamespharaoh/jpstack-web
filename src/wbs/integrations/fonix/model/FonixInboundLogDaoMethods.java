package wbs.integrations.fonix.model;

import java.util.List;

public
interface FonixInboundLogDaoMethods {

	List <Long> searchIds (
			FonixInboundLogSearch fonixInboundLogSearch);

}
