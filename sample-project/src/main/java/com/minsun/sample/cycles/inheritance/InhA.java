package com.minsun.sample.cycles.inheritance;

/**
 * 상속+필드 순환: InhA extends InhB (상속), InhB 는 InhA 필드를 가짐 (필드).
 * 부모가 자식을 참조하는 안티패턴이 순환을 만든다.
 */
public class InhA extends InhB {
}
