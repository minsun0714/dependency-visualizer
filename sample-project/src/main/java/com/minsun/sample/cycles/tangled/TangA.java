package com.minsun.sample.cycles.tangled;

/**
 * 얽힌 덩어리: TangA/TangB/TangC 가 여러 순환으로 얽혀 하나의 SCC(크기 3)를 이룬다.
 * 개별 순환은 여러 개(A→B→C→A, A→C→A, ...)지만 SCC 로는 1개로 묶인다.
 */
public class TangA {

    private TangB b;
    private TangC c;
}
