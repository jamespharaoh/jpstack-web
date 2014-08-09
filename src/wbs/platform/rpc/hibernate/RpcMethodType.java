package wbs.platform.rpc.hibernate;

import java.sql.Types;

import wbs.framework.database.RpcMethod;
import wbs.framework.hibernate.EnumUserType;

public
class RpcMethodType
	extends EnumUserType<String,RpcMethod> {

	{

		sqlType (Types.VARCHAR);
		enumClass (RpcMethod.class);

		add ("l", RpcMethod.pollOnly);
		add ("g", RpcMethod.httpGet);
		add ("p", RpcMethod.httpPost);
		add ("h", RpcMethod.php);
		add ("x", RpcMethod.xml);
		add ("s", RpcMethod.soap);

	}

}
