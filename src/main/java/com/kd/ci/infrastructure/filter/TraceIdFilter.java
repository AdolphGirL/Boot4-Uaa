package com.kd.ci.infrastructure.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;

/**
import io.opentelemetry.api.trace.Span;**/

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

/** 註冊為 filter，放在server層，非spring層
 * Servlet filter to include trace ID in response headers */
/** allows users to include the trace ID when reporting errors，後續好追蹤 */
@Component
public class TraceIdFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		/***
		if (response instanceof HttpServletResponse httpResponse) {
			String traceId = Span.current().getSpanContext().getTraceId();
			httpResponse.setHeader("X-Trace-Id", traceId);
		}***/
		chain.doFilter(request, response);
	}

}
