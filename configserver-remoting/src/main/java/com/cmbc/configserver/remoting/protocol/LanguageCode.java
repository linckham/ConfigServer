package com.cmbc.configserver.remoting.protocol;

public enum LanguageCode {
    JAVA(0, "JAVA"), CPP(1, "C Plus Plus"),
    DOTNET(2, ".Net"), PYTHON(3, "Python"), DELPHI(4, "Delphi"),
    ERLANG(5, "Erlang"), RUBY(6, "Ruby"), GO(7, "Go"), SCALA(8, "Scala"), OTHER(9, "Other");
    private int code;
    private String lauguage;

    LanguageCode(int code, String lauguage) {
        this.code = code;
        this.lauguage = lauguage;
    }

    public int getCode() {
        return this.code;
    }

    public String getLauguage() {
        return this.lauguage;
    }
}