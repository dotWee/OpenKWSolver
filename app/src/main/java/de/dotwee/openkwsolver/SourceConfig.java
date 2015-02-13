package de.dotwee.openkwsolver;

public class SourceConfig {
    public static final String URL = "http://www.9kw.eu:80/index.cgi";

    public static final String URL_PARAMETER_NOCAPTCHA = "&nocaptcha=1";

    public static final String URL_PARAMETER_SOURCE = "&source=androidopenkws";
    public static final String URL_PARAMETER_TYPE_CONFIRM = ""; // &confirm=1

    public static final String URL_PARAMETER_CAPTCHA_NEW = "?action=usercaptchanew";
    public static final String URL_PARAMETER_CAPTCHA_SHOW = "?action=usercaptchashow";
    public static final String URL_PARAMETER_CAPTCHA_SKIP = "?action=usercaptchaskip";
    public static final String URL_PARAMETER_CAPTCHA_ANSWER = "?action=usercaptchacorrect";

    public static final String URL_PARAMETER_SERVER_CKECK = "?action=userservercheck";
    public static final String URL_PARAMETER_SERVER_BALANCE = "?action=usercaptchaguthaben";
}
