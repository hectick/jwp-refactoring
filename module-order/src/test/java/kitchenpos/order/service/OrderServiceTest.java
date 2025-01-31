package kitchenpos.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.Collections;
import java.util.List;
import kitchenpos.MenuFixture;
import kitchenpos.ProductFixture;
import kitchenpos.MenuGroupFixture;
import kitchenpos.menu.service.MenuGroupService;
import kitchenpos.menu.service.MenuService;
import kitchenpos.menu.service.dto.MenuGroupResponse;
import kitchenpos.menu.service.dto.MenuRequest;
import kitchenpos.menu.service.dto.MenuResponse;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.order.domain.repository.OrderRepository;
import kitchenpos.order.service.dto.OrderRequest;
import kitchenpos.order.service.dto.OrderResponse;
import kitchenpos.order.service.dto.OrderStatusRequest;
import kitchenpos.order.service.dto.TableResponse;
import kitchenpos.product.service.ProductService;
import kitchenpos.product.service.dto.ProductResponse;
import kitchenpos.supports.IntegrationTest;
import kitchenpos.OrderFixture;
import kitchenpos.OrderTableFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("주문 서비스 테스트")
@IntegrationTest
class OrderServiceTest {

    private static final long INVALID_ID = -1L;

    @Autowired
    private OrderService orderService;
    @Autowired
    private TableService tableService;
    @Autowired
    private MenuService menuService;
    @Autowired
    private MenuGroupService menuGroupService;
    @Autowired
    private ProductService productService;

    private MenuResponse setUpMenu() {
        final ProductResponse product = productService.create(ProductFixture.create());
        final MenuGroupResponse menuGroup = menuGroupService.create(MenuGroupFixture.create());

        final MenuRequest request = MenuFixture.of(menuGroup.getId(), List.of(product));
        return menuService.create(request);
    }

    @DisplayName("주문 목록을 조회할 수 있다")
    @Test
    void findAllOrders() {
        // given
        final TableResponse orderTable = tableService.create(OrderTableFixture.createNotEmpty());
        final OrderResponse order = orderService.create(OrderFixture.of(setUpMenu().getId(), orderTable.getId()));

        // when
        final List<OrderResponse> list = orderService.list();

        // then
        assertSoftly(softly -> {
            assertThat(list).hasSize(1);
            assertThat(list)
                    .usingRecursiveComparison()
                    .isEqualTo(List.of(order));
        });
    }

    @Nested
    @DisplayName("주문을 생성할 때")
    class Create {

        @DisplayName("조리 상태로 생성된다")
        @Test
        void success(@Autowired OrderRepository orderRepository) {
            // given
            final TableResponse orderTable = tableService.create(OrderTableFixture.createNotEmpty());
            final OrderRequest order = OrderFixture.of(setUpMenu().getId(), orderTable.getId());

            // when
            final OrderResponse savedOrder = orderService.create(order);

            // then
            final OrderLineItem snapshotEventValidation = orderRepository.findById(savedOrder.getId()).get()
                    .getOrderLineItems()
                    .get(0);
            assertSoftly(softly -> {
                assertThat(savedOrder.getId()).isPositive();
                assertThat(savedOrder.getOrderStatus()).isEqualTo("COOKING");
                assertThat(snapshotEventValidation.getName()).isNotBlank();
                assertThat(snapshotEventValidation.getPrice()).isNotNull();
            });
        }

        @DisplayName("주문할 메뉴와 수량 목록이 비어있으면 예외처리 한다")
        @Test
        void throwExceptionWhenOrderLineItemEmpty() {
            // given
            final TableResponse orderTable = tableService.create(OrderTableFixture.createNotEmpty());

            final OrderRequest request = new OrderRequest(orderTable.getId(), Collections.emptyList());

            // then
            assertThatThrownBy(() -> orderService.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("주문 품목이 없어 주문할 수 없습니다.");
        }

        @DisplayName("존재하지 않는 메뉴를 주문하면 예외처리 한다")
        @Test
        void throwExceptionWhenInvalidMenu() {
            // given
            final TableResponse orderTable = tableService.create(OrderTableFixture.createNotEmpty());
            final OrderRequest order = OrderFixture.of(INVALID_ID, orderTable.getId());

            // then
            assertThatThrownBy(() -> orderService.create(order))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 메뉴 또는 동일한 메뉴가 포함되어 있습니다.");
        }

        @DisplayName("존재하지 않는 주문 테이블로 주문하면 예외처리 한다")
        @Test
        void throwExceptionWhenInvalidOrderTable() {
            // given
            final OrderRequest order = OrderFixture.of(setUpMenu().getId(), INVALID_ID);

            // then
            assertThatThrownBy(() -> orderService.create(order))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 테이블입니다.");
        }

        @DisplayName("주문 테이블이 빈 테이블이면 예외처리 한다")
        @Test
        void throwExceptionWhenEmptyTable() {
            // given
            final TableResponse orderTable = tableService.create(OrderTableFixture.createEmpty());
            final OrderRequest order = OrderFixture.of(setUpMenu().getId(), orderTable.getId());

            // then
            assertThatThrownBy(() -> orderService.create(order))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("테이블이 비어있어 주문을 할 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("주문 상태를 변경할 때")
    class ChangeOrderStatus {

        @DisplayName("주문 상태는 조리 ➜ 식사 ➜ 계산완료 순서로 변경된다")
        @Test
        void success() {
            // given
            final TableResponse orderTable = tableService.create(OrderTableFixture.createNotEmpty());
            final OrderRequest order = OrderFixture.of(setUpMenu().getId(), orderTable.getId());

            // when, then (조리)
            final OrderResponse savedOrder = orderService.create(order);
            assertThat(savedOrder.getOrderStatus()).isEqualTo("COOKING");

            // when, then (식사)
            final OrderStatusRequest meal = OrderFixture.createMeal();
            final OrderResponse savedOrder2 = orderService.changeOrderStatus(savedOrder.getId(), meal);
            assertThat(savedOrder2.getOrderStatus()).isEqualTo("MEAL");

            // when, then (계산완료)
            final OrderStatusRequest completion = OrderFixture.createCompletion();
            final OrderResponse savedOrder3 = orderService.changeOrderStatus(savedOrder2.getId(), completion);
            assertThat(savedOrder3.getOrderStatus()).isEqualTo("COMPLETION");
        }

        @DisplayName("존재하지 않는 주문이면 예외처리 한다")
        @Test
        void throwExceptionWhenInvalidOrder() {
            // given
            final OrderStatusRequest change = OrderFixture.createMeal();

            // then
            assertThatThrownBy(() -> orderService.changeOrderStatus(INVALID_ID, change))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 주문입니다.");
        }

        @DisplayName("계산 완료 상태를 변경 하려하면 예외처리 한다")
        @Test
        void throwExceptionWhenChangeCompletionStatus() {
            // given
            final TableResponse orderTable = tableService.create(OrderTableFixture.createNotEmpty());
            final OrderRequest order = OrderFixture.of(setUpMenu().getId(), orderTable.getId());
            final OrderResponse savedOrder = orderService.create(order);
            final Long savedOrderId = savedOrder.getId();

            final OrderStatusRequest completion = OrderFixture.createCompletion();
            orderService.changeOrderStatus(savedOrderId, completion); // 계산 완료 상태로 변경

            // then
            assertThatThrownBy(() -> orderService.changeOrderStatus(savedOrderId, completion))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("계산 완료 상태를 변경할 수 없습니다.");
        }
    }
}
