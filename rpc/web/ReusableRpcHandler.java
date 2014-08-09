package wbs.platform.rpc.web;

import wbs.platform.rpc.core.RpcHandler;


/**
 * Marker class to indicate an RpcHandler is reusable. A reusable one can be
 * made from a normal one by implementing a simply factory.
 */
public interface ReusableRpcHandler extends RpcHandler {
}
