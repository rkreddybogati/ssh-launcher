package com.scalr.ssh.launcher.configuration;

import org.apache.http.NameValuePair;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class NameValuePairLauncherConfiguration implements LauncherConfigurationInterface {

    private List<NameValuePair> parameters;

    public NameValuePairLauncherConfiguration(List<NameValuePair> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String getOption(String optionName) {
        // This is somewhat inefficient, but it's not a very big deal here.
        for (NameValuePair pair : parameters) {
            if (pair.getName().equals(optionName)) {
                return pair.getValue();
            }
        }
        return null;
    }
}
