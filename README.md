# 키친포스

## 용어 사전

| 한글명 | 영문명 | 설명                                      |
| --- | --- |-----------------------------------------|
| 상품 | product | 메뉴를 관리하는 기준이 되는 데이터                     |
| 메뉴 그룹 | menu group | 메뉴 묶음, 분류                               |
| 메뉴 | menu | 메뉴 그룹에 속하는 실제 주문 가능 단위                  |
| 메뉴 상품 | menu product | 메뉴에 속하는 수량이 있는 상품                       |
| 금액 | amount | 가격 * 수량                                 |
| 주문 테이블 | order table | 매장에서 주문이 발생하는 영역                        |
| 빈 테이블 | empty table | 주문을 등록할 수 없는 주문 테이블, 손님 수를 변경할 수 없는 테이블 |
| 주문 | order | 매장에서 발생하는 주문                            |
| 주문 상태 | order status | 주문은 조리 ➜ 식사 ➜ 계산 완료 순서로 진행된다.           |
| 방문한 손님 수 | number of guests | 필수 사항은 아니며 주문은 0명으로 등록할 수 있다.           |
| 단체 지정 | table group | 통합 계산을 위해 개별 주문 테이블을 그룹화하는 기능           |
| 주문 항목 | order line item | 주문에 속하는 수량이 있는 메뉴                       |
| 매장 식사 | eat in | 포장하지 않고 매장에서 식사하는 것                     |

## 기능 요구 사항

### 흐름

- 메뉴
  - 상품 생성, 메뉴 그룹 생성 ➜ 메뉴 생성
- 주문
  - 개별 주문
    - 주문 테이블 생성 ➜ 주문 생성 ➜ 주문 상태 전환 ➜ 빈 테이블로 전환
  - 단체 주문
    - 빈 주문 테이블 생성 ➜ 단체 지정 및 테이블 상태 전환 ➜ 주문 생성 ➜ 주문 상태 전환 ➜ 빈 테이블로 전환 ➜ 단체 지정 해제

### 상품

- [x] `이름`, `가격` 정보를 갖는다.
- [x] 상품을 생성할 수 있다.
  - [x] 가격이 0 미만일 수 없다.
  - [x] 가격이 반드시 있어야 한다.
- [x] 상품 목록을 조회할 수 있다.

### 메뉴 그룹

- [x] `이름` 정보를 갖는다.
  - 이름 ex) 추천 메뉴, 순살파닭두마리메뉴
- [x] 메뉴 그룹을 생성할 수 있다.
- [x] 메뉴 그룹 목록을 조회할 수 있다.

### 메뉴

- [x] `이름`, `가격`, `메뉴 그룹`, `메뉴에 속한 상품과 수량 목록` 정보를 갖는다.
- [x] 메뉴를 생성할 수 있다.
  - [x] 가격이 0 미만일 수 없다.
  - [x] 가격이 반드시 있어야 한다.
  - [x] 존재하지 않는 메뉴 그룹이면 안된다.
  - [x] 존재하지 않는 상품이면 안된다.
  - [x] `메뉴 가격`이 `상품 * 수량`의 총 합보다 클 수 없다.
- [x] 메뉴 목록을 조회할 수 있다.

### 주문

- [x] `주문 테이블`,`주문 상태`, `메뉴와 수량 목록` 정보를 갖는다.
- [x] 주문을 생성할 수 있다.
  - [x] 주문 시 조리 상태로 생성된다.
  - [x] 주문할 메뉴와 수량 목록이 비어있으면 안된다.
  - [x] 존재하지 않는 메뉴를 주문하면 안된다.
  - [x] 존재하지 않는 주문 테이블로 주문하면 안된다.
  - [x] 주문 테이블이 빈 테이블이면 안된다.
- [x] 주문 목록을 조회할 수 있다.
- [x] 주문 상태를 변경할 수 있다.
  - [x] 주문 상태는 `조리` ➜ `식사` ➜ `계산완료` 순서로 변경된다.
  - [x] 존재하지 않는 주문이면 안된다.
  - [x] 계산 완료 상태를 변경하면 안된다.

### 단체 지정

- [x] 단체 지정을 할 `주문 테이블 목록` 정보를 갖는다.
- [x] 단체 지정을 할 수 있다.
  - [x] 단체 지정이 될 때 테이블 상태가 `비어있음` ➜ `비어있지 않음`으로 바뀐다.
  - [x] 주문 테이블 목록이 비어있으면 안된다.
  - [x] 주문 테이블 목록의 사이즈가 2 미만이면 안된다.
  - [x] 존재하지 않는 주문 테이블이 포함되어 있으면 안된다.
  - [x] 비어있지 않은 테이블이 포함되어 있으면 안된다.
  - [x] 이미 단체 지정이 된 테이블이 포함되어 있으면 안된다.
- [x] 단체 지정을 해제할 수 있다.
  - [x] 주문 상태가 계산 완료가 아닌 테이블이 포함되어 있으면 안된다.

### 테이블

- [x] `손님 수`, `테이블이 비어있는지 여부` 정보를 갖는다.
- [x] 테이블을 생성할 수 있다.
- [x] 테이블의 손님 수를 변경할 수 있다.
  - [x] 테이블이 존재하지 않으면 안된다.
  - [x] 손님 수를 0명 미만으로 변경할 수 없다.
  - [x] 빈 테이블을 변경할 수 없다.
- [x] 비어 있지 않은 테이블을 빈 테이블로 변경할 수 있다.
  - [x] 테이블이 존재하지 않으면 안된다.
  - [x] 단체 지정된 테이블을 변경할 수 없다.
  - [x] 주문 상태가 계산 완료가 아닌 테이블을 변경할 수 없다.
- [x] 테이블 목록을 조회할 수 있다.
