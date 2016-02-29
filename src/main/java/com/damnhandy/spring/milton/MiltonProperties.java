package com.damnhandy.spring.milton;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties required by Milton.
 */
@Component
@ConfigurationProperties(prefix = "milton",exceptionIfInvalid = true,ignoreUnknownFields = true)
public class MiltonProperties {

    /**
     * Resources with this as the first part of their path will not be served
     * from Milton. Instead, this filter will allow filter processing to
     * continue so they will be served by JSP or a servlet
     */
    List<String> excludePaths = new ArrayList<>();

    /**
     *
     */
    String filesystemRoot;

    public String getFilesystemRoot() {
        return filesystemRoot;
    }

    public void setFilesystemRoot(String filesystemRoot) {
        this.filesystemRoot = filesystemRoot;
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }

}
