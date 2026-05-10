package br.com.ctkd.mail.controller;

public record EmailRequest(String destination, String name, String accessLink) {}
