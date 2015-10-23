package com.concur.babel.test.service;

public class EchoServiceImpl implements EchoService.Iface {
    @Override
    public String echo(String message) {
        return message;
    }
}
