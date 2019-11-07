package net.cyweb.CoinUtils.Exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogException extends Exception {

    private Logger logger = LoggerFactory.getLogger(LogException.class);

    public LogException(String message) {
        super(message);
        logger.error(message);
    }
}
