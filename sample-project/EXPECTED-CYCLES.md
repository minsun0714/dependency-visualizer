# 샘플 프로젝트 — 의존성 & 순환참조 정답지

분석기(Phase 2~4) 검증용. **필드 주입(`private final`) 기준** 클래스 간 의존 간선과,
Tarjan SCC로 나와야 하는 순환 덩어리를 손으로 적어둔 것.

## 패키지 구조

```
com.minsun.sample
├── user/       도메인
├── order/      도메인
├── product/    도메인
└── shared/     공용 sink (순환 없음)
```

## 클래스 간 의존 간선 (필드 주입 기준, 내부 타입만)

| From | To | 비고 |
|------|----|----|
| user.UserController   | user.UserService        | |
| user.UserService      | user.UserRepository     | |
| user.UserService      | order.OrderService      | **cross-domain, 사이클** |
| user.UserService      | shared.EmailSender      | |
| order.OrderController | order.OrderService      | |
| order.OrderService    | order.OrderRepository   | |
| order.OrderService    | user.UserService        | **cross-domain, 사이클** |
| order.OrderService    | product.ProductService  | 단방향 (사이클 아님) |
| order.OrderService    | shared.PriceCalculator  | |
| product.ProductController | product.ProductService | |
| product.ProductService    | product.ProductRepository | |
| product.ProductService    | product.InventoryService  | **same-package, 사이클** |
| product.ProductService    | shared.PriceCalculator    | |
| product.InventoryService  | product.ProductService    | **same-package, 사이클 (import 없음!)** |
| product.InventoryService  | product.ProductRepository | |

> 엔티티(User/Order/Product)와 Repository→엔티티 참조는 **필드 주입이 아니라 메서드 리턴/파라미터**라
> 필드 기준 추출(Phase 2 초기 스코프)에서는 간선으로 잡히지 않는다. 나중에 파라미터/리턴까지 확장하면 추가됨.

## 나와야 하는 순환 덩어리 (SCC, size ≥ 2)

- **SCC A (도메인 간):** `{ user.UserService, order.OrderService }`
- **SCC B (같은 패키지 내):** `{ product.ProductService, product.InventoryService }`

그 외 모든 클래스는 크기 1 SCC(순환 없음). shared 패키지는 나가는 간선이 없는 순수 sink.

## 핵심 검증 포인트

`product.InventoryService → product.ProductService` 는 **같은 패키지라 import 문이 없다.**
→ import 기반 추출이면 이 간선을 놓쳐 SCC B가 검출 안 됨.
→ SymbolSolver로 타입을 resolve 해야만 잡힌다. 이게 SymbolSolver가 필요한 결정적 이유.
