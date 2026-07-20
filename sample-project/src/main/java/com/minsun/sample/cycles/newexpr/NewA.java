package com.minsun.sample.cycles.newexpr;

/** 객체 생성 순환: NewA 는 new NewB() 를, NewB 는 new NewA() 를 생성. */
public class NewA {

    public void go() {
        new NewB();
    }
}
