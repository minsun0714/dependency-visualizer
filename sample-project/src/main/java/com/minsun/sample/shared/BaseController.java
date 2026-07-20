package com.minsun.sample.shared;

/**
 * 공통 컨트롤러 베이스 — 상속(extends) 간선 예시.
 * 각 도메인 컨트롤러가 이 클래스를 상속한다.
 */
public abstract class BaseController {

    protected String health() {
        return "ok";
    }
}
