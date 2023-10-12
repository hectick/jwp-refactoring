package kitchenpos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.Product;
import kitchenpos.domain.TableGroup;
import kitchenpos.supports.MenuGroupFixture;
import kitchenpos.supports.OrderTableFixture;
import kitchenpos.supports.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("테이블 서비스 테스트")
@ServiceTest
class TableServiceTest {

    @Autowired
    private TableService tableService;
    @Autowired
    private TableGroupService tableGroupService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductService productService;
    @Autowired
    private MenuService menuService;
    @Autowired
    private MenuGroupService menuGroupService;

    @Nested
    @DisplayName("테이블을 생성할 때")
    class Create {

        @DisplayName("정상적으로 생성된다")
        @Test
        void success() {
            final OrderTable savedOrderTable = tableService.create(OrderTableFixture.createEmpty());

            assertThat(savedOrderTable.getId()).isNotNull();
        }
    }

    @Nested
    @DisplayName("테이블의 손님 수를 변경 할 때")
    class ChangeNumberOfGuests {

        @DisplayName("정상적으로 변경된다")
        @Test
        void success() {
            // given
            final OrderTable orderTable = tableService.create(OrderTableFixture.createNotEmpty());

            final OrderTable change = new OrderTable();
            change.setNumberOfGuests(2);
            final Long orderTableId = orderTable.getId();

            // when
            final OrderTable savedOrderTable = tableService.changeNumberOfGuests(orderTableId, change);

            // then
            assertThat(savedOrderTable.getNumberOfGuests()).isEqualTo(change.getNumberOfGuests());
        }

        @DisplayName("테이블이 존재하지 않으면 예외처리 한다")
        @Test
        void throwExceptionWhenInvalidOrderTable() {
            // given
            final OrderTable change = new OrderTable();
            change.setNumberOfGuests(2);

            // then
            assertThatThrownBy(() -> tableService.changeNumberOfGuests(-1L, change))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("손님 수를 0명 미만으로 변경하려 하면 예외처리 한다")
        @Test
        void throwExceptionWhenInvalidNumberOfGuests() {
            // given
            final OrderTable orderTable = tableService.create(OrderTableFixture.createNotEmpty());

            final OrderTable change = new OrderTable();
            change.setNumberOfGuests(-1);
            final Long orderTableId = orderTable.getId();

            // then
            assertThatThrownBy(() -> tableService.changeNumberOfGuests(orderTableId, change))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("빈 테이블을 변경하려하면 예외처리 한다")
        @Test
        void throwExceptionWhenEmptyTable() {
            // given
            final OrderTable orderTable = tableService.create(OrderTableFixture.createEmpty());

            final OrderTable change = new OrderTable();
            change.setNumberOfGuests(2);
            final Long orderTableId = orderTable.getId();

            // then
            assertThatThrownBy(() -> tableService.changeNumberOfGuests(orderTableId, change))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("빈 테이블로 변경할 때")
    class ChangeEmpty {

        @DisplayName("정상적으로 변경된다")
        @Test
        void success() {
            // given
            final OrderTable orderTable = tableService.create(OrderTableFixture.createNotEmpty());

            final OrderTable change = OrderTableFixture.createEmpty();
            final Long orderTableId = orderTable.getId();

            // when
            final OrderTable savedOrderTable = tableService.changeEmpty(orderTableId, change);

            // then
            assertThat(savedOrderTable.isEmpty()).isTrue();
        }

        @DisplayName("테이블이 존재하지 않으면 예외처리 한다")
        @Test
        void throwExceptionWhenInvalidOrderTable() {
            // given
            final OrderTable change = OrderTableFixture.createEmpty();

            // then
            assertThatThrownBy(() -> tableService.changeEmpty(-1L, change))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("단체 지정된 테이블을 변경하려 하면 예외처리 한다")
        @Test
        void throwExceptionWhenTableGroupExist() {
            // given
            final OrderTable orderTable1 = tableService.create(OrderTableFixture.createEmpty());
            final OrderTable orderTable2 = tableService.create(OrderTableFixture.createEmpty());

            final TableGroup tableGroup = new TableGroup();
            tableGroup.setOrderTables(List.of(orderTable1, orderTable2));
            tableGroupService.create(tableGroup);

            final OrderTable change = OrderTableFixture.createEmpty();
            final Long orderTableId = orderTable1.getId();

            // then
            assertThatThrownBy(() -> tableService.changeEmpty(orderTableId, change))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("주문 상태가 계산 완료가 아니면 예외처리 한다")
        @ParameterizedTest
        @ValueSource(strings = {"MEAL", "COOKING"})
        void throwExceptionWhenIllegalOrderStatus(String orderStatus) {
            // given
            final Product product = productService.create(ProductFixture.create());
            final MenuGroup menuGroup = menuGroupService.create(MenuGroupFixture.create());
            final MenuProduct menuProduct = new MenuProduct();
            menuProduct.setQuantity(2);
            menuProduct.setProductId(product.getId());

            final Menu menu = new Menu();
            menu.setPrice(product.getPrice().multiply(BigDecimal.valueOf(2)).subtract(BigDecimal.ONE));
            menu.setName("상품+상품");
            menu.setMenuGroupId(menuGroup.getId());
            menu.setMenuProducts(List.of(menuProduct));
            final Menu savedMenu = menuService.create(menu);

            final OrderTable orderTable = tableService.create(OrderTableFixture.createNotEmpty());

            final OrderLineItem orderLineItem = new OrderLineItem();
            orderLineItem.setMenuId(savedMenu.getId());
            orderLineItem.setQuantity(1L);

            final Order order = new Order();
            order.setOrderTableId(orderTable.getId());
            order.setOrderLineItems(List.of(orderLineItem));
            final Order savedOrder = orderService.create(order);

            final Order change1 = new Order();
            change1.setOrderStatus(orderStatus);

            orderService.changeOrderStatus(savedOrder.getId(), change1);

            final OrderTable change = OrderTableFixture.createEmpty();
            final Long orderTableId = orderTable.getId();

            // then
            assertThatThrownBy(() -> tableService.changeEmpty(orderTableId, change))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
