package com.scalr.ssh.launcher.configuration;

import javax.servlet.http.HttpServletRequest;

public class ServletRequestLauncherConfiguration implements LauncherConfigurationInterface {

    private HttpServletRequest request;

    public ServletRequestLauncherConfiguration (HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String getOption(String optionName) {
        return request.getParameter(optionName);
    }
}
