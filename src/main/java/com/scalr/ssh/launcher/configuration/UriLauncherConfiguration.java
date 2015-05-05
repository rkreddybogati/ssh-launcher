package com.scalr.ssh.launcher.configuration;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

public class UriLauncherConfiguration implements LauncherConfigurationInterface {
    private HashMap<String, String> pairs;

    public UriLauncherConfiguration(URI uri) {
        pairs = new HashMap<String, String>();

        List<NameValuePair> params = URLEncodedUtils.parse(uri, "UTF-8");
        for (NameValuePair param : params) {
            pairs.put(param.getName(), param.getValue());
        }
    }

    @Override
    public String getOption(String optionName) {
        return pairs.get(optionName);
    }
}
