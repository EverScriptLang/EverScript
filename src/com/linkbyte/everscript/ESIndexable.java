package com.linkbyte.everscript;

interface ESIndexable {
    Object get(Token token, Object index);
    void set(Token token, Object index, Object item);
    int length();
}
