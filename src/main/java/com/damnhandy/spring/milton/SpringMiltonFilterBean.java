package com.damnhandy.spring.milton;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.ResourceFactory;
import io.milton.http.Response;
import io.milton.http.annotated.AnnotationResourceFactory;
import io.milton.http.template.JspViewResolver;
import io.milton.http.template.ViewResolver;
import io.milton.servlet.MiltonServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A refactoring of the {@link io.milton.servlet.SpringMiltonFilter} to be better suited to SpringBoot. The filter
 * implements the {@link GenericFilterBean} so that it doesn't have to be responsible for looking the context.
 */
public class SpringMiltonFilterBean extends GenericFilterBean {

    private static Logger log = LoggerFactory.getLogger(SpringMiltonFilterBean.class);

    @Autowired
    private MiltonProperties miltonProperties;

    @Autowired
    private HttpManagerBuilder httpManagerBuilder;

    private HttpManager httpManager;



    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        ResourceFactory rf = httpManagerBuilder.getMainResourceFactory();
        if (rf instanceof AnnotationResourceFactory) {
            AnnotationResourceFactory arf = (AnnotationResourceFactory) rf;
            if (arf.getViewResolver() == null) {
                ViewResolver viewResolver = new JspViewResolver(this.getServletContext());
                arf.setViewResolver(viewResolver);
            }
        }
        this.httpManager = httpManagerBuilder.buildHttpManager();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest hsr = (HttpServletRequest) request;
            String url = hsr.getRequestURI();
            // Allow certain paths to be excluded from Milton, these might be other servlets, for example
            if (miltonProperties.getExcludePaths() != null) {
                for (String s : miltonProperties.getExcludePaths()) {
                    if (url.startsWith(s)) {
                        log.trace("doFilter: is excluded path");
                        fc.doFilter(request, response);
                        return;
                    }
                }
            }
            log.trace("doFilter: begin milton processing");
            doMiltonProcessing(hsr, (HttpServletResponse) response);
        } else {
            log.trace("doFilter: request is not a supported type, continue with filter chain");
            fc.doFilter(request, response);
            return;
        }
    }

    private void doMiltonProcessing(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            MiltonServlet.setThreadlocals(req, resp);
            Request request = new io.milton.servlet.ServletRequest(req, this.getServletContext());
            Response response = new io.milton.servlet.ServletResponse(resp);
            httpManager.process(request, response);
        } finally {
            MiltonServlet.clearThreadlocals();
            //resp.getOutputStream().flush();
            resp.flushBuffer();
        }
    }

    public void destroy() {
        if (httpManager != null) {
            httpManager.shutdown();
        }
    }

}
