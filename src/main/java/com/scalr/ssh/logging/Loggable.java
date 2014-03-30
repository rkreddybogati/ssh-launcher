package com.scalr.ssh.logging;

import java.util.logging.Logger;

abstract public class Loggable {
    private Logger _logger;

    protected Logger getLogger () {
        if (_logger == null) {
            _logger = Logger.getLogger(this.getClass().getName());
        }
        return _logger;
    }

}
