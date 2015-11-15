package wbs.framework.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("responseFilter")
public
class ResponseFilter
	implements Filter {

	@Override
	public
	void doFilter (
			ServletRequest request,
			ServletResponse response,
			FilterChain chain)
		throws
			ServletException,
			IOException {

		RequestContextImplementation.servletResponseThreadLocal.set (
			(HttpServletResponse)
			response);

		chain.doFilter (
			request,
			response);

	}

	@Override
	public
	void destroy () {
	}

	@Override
	public
	void init (
			FilterConfig filterConfig)
		throws ServletException {

	}

}
