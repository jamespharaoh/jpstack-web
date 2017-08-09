package wbs.framework.servlet;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import wbs.framework.logging.CloseableTaskLogger;

public
interface ComponentFilterChain {

	void doFilter (
			CloseableTaskLogger parentTaskLogger,
			ServletRequest request,
			ServletResponse response);

}
