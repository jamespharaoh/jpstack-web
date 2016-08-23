package wbs.applications.imchat.model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.utils.TextualInterval;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode
@ToString
public
class ImChatCustomerSearch
	implements Serializable {

	Long imChatId;

	String code;
	String email;

	TextualInterval firstSession;
	TextualInterval lastSession;

	Order order =
		Order.timestampDesc;

	public static
	enum Order {
		timestampDesc,
		totalPurchaseDesc,
		balanceDesc;
	}

}
