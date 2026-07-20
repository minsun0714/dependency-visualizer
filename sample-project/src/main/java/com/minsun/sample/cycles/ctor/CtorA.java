package com.minsun.sample.cycles.ctor;

/** 생성자 주입 순환: CtorA ↔ CtorB (생성자 파라미터). */
public class CtorA {

    private final CtorB b;

    public CtorA(CtorB b) {
        this.b = b;
    }
}
