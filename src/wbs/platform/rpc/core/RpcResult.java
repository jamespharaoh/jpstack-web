package wbs.platform.rpc.core;

import java.util.List;

import com.google.common.collect.ImmutableList;

public
class RpcResult {

	private final
	int status;

	private final
	String statusCode;

	private final
	List<String> statusMessages;

	private final
	RpcStructure rpcStruct;

	public
	RpcResult (
			String name,
			int newStatus,
			String newStatusCode,
			List<String> newStatusMessages,
			RpcElem... members) {

		status = newStatus;
		statusCode = newStatusCode;
		statusMessages = newStatusMessages;
		// extraParams = newExtraParams != null? newExtraParams : new
		// HashMap<String,Object> ();

		rpcStruct =
			Rpc.rpcStruct (name,
				Rpc.rpcElem ("status", status),
				Rpc.rpcElem ("status-code", statusCode),
				Rpc.rpcElem ("status-message", getStatusMessage ()),
				Rpc.rpcList ("status-messages", "status-message", statusMessages),
				Rpc.rpcElem ("success", getSuccess ()));

		for (RpcElem member : members)
			rpcStruct.add (member);

	}

	public
	RpcResult (
			String name,
			int newStatus,
			String newStatusCode,
			String newStatusMessage) {

		this (
			name,
			newStatus,
			newStatusCode,
			ImmutableList.<String>of (
				newStatusMessage));

	}

	public
	boolean getSuccess () {

		return (status & 0xf000) == 0x0000;

	}

	public
	int getStatus () {

		return status;

	}

	public
	String getStatusCode () {

		return statusCode;

	}

	public
	String getStatusMessage () {

		return statusMessages.get (
			0);

	}

	public
	List<String> getStatusMessages () {

		return statusMessages;

	}

	public
	int getHttpStatus () {

		return Rpc.statusToHttpStatus (
			status);

	}

	public
	RpcStructure getStruct () {

		return rpcStruct;

	}

}
