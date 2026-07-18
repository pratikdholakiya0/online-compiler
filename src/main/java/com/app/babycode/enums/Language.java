package com.app.babycode.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {

    PYTHON("python", ".py"),
    JAVA("java", ".java"),
    CPP("g++", ".cpp"),
    C("gcc", ".c"),
    JAVASCRIPT("node", ".js");

    private final String command;
    private final String extension;
}