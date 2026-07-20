package com.minsun.sample.cycles.methodparam;

/** 메서드 시그니처 순환: MpA 는 MpB 를 파라미터로, MpB 는 MpA 를 반환. */
public class MpA {

    public void handle(MpB b) {
        // no-op
    }
}
