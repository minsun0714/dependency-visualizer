package com.minsun.analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 소스 루트의 {@code package ...;} 선언들에서 공통 최상위 패키지를 추론한다.
 *
 * <p>플러그인에서 basePackage 를 명시하지 않았을 때의 기본값으로 쓴다.
 * 예: 모든 파일이 {@code com.acme.order.*}, {@code com.acme.user.*} 면 {@code "com.acme"}.
 * 소스가 없거나 공통 접두어가 없으면(서로 다른 최상위 패키지) 빈 문자열을 반환한다 —
 * 이 경우 호출측이 명시 설정을 요구하는 게 안전하다.
 */
public final class BasePackageInferrer {

    private static final Pattern PACKAGE_DECL =
        Pattern.compile("(?m)^\\s*package\\s+([\\w.]+)\\s*;");

    private BasePackageInferrer() {
    }

    /** 소스 루트를 훑어 공통 패키지 접두어를 추론한다. 실패 시 {@code ""}. */
    public static String infer(Path sourceRoot) throws IOException {
        List<String> packages = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            for (Path file : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                declaredPackage(file).ifPresent(packages::add);
            }
        }
        return commonPackagePrefix(packages);
    }

    /** 점(.)으로 구분된 이름들의 세그먼트 단위 공통 접두어. 공통이 없으면 {@code ""}. */
    public static String commonPackagePrefix(Collection<String> dottedNames) {
        List<String[]> segmented = new ArrayList<>();
        for (String name : dottedNames) {
            segmented.add(name.split("\\."));
        }
        return commonPrefix(segmented);
    }

    private static java.util.Optional<String> declaredPackage(Path file) throws IOException {
        Matcher m = PACKAGE_DECL.matcher(Files.readString(file));
        return m.find() ? java.util.Optional.of(m.group(1)) : java.util.Optional.empty();
    }

    /** 여러 패키지 세그먼트 배열의 가장 긴 공통 선두 세그먼트들을 점(.)으로 합친다. */
    private static String commonPrefix(List<String[]> packages) {
        if (packages.isEmpty()) {
            return "";
        }
        String[] first = packages.get(0);
        int common = first.length;
        for (String[] pkg : packages) {
            int i = 0;
            while (i < common && i < pkg.length && first[i].equals(pkg[i])) {
                i++;
            }
            common = i;
            if (common == 0) {
                return "";
            }
        }
        return String.join(".", java.util.Arrays.copyOf(first, common));
    }
}
