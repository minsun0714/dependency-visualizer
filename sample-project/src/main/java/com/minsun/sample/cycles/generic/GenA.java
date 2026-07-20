package com.minsun.sample.cycles.generic;

import java.util.List;

/** 제네릭 인자 순환: GenA 는 List&lt;GenB&gt; 를, GenB 는 GenA 필드를 가짐. */
public class GenA {

    private List<GenB> items;
}
