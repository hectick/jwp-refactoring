package kitchenpos.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import kitchenpos.application.dto.request.OrderRequest;
import kitchenpos.application.dto.request.OrderStatusRequest;
import kitchenpos.application.dto.response.OrderResponse;
import kitchenpos.domain.Menu;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.repository.MenuRepository;
import kitchenpos.repository.OrderLineItemRepository;
import kitchenpos.repository.OrderRepository;
import kitchenpos.repository.OrderTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final OrderLineItemRepository orderLineItemRepository;
    private final OrderTableRepository orderTableRepository;

    public OrderService(final MenuRepository menuRepository, final OrderRepository orderRepository,
                        final OrderLineItemRepository orderLineItemRepository,
                        final OrderTableRepository orderTableRepository) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.orderLineItemRepository = orderLineItemRepository;
        this.orderTableRepository = orderTableRepository;
    }

    @Transactional
    public OrderResponse create(final OrderRequest request) {
        final Order order = new Order(findOrderTableById(request.getOrderTableId()), LocalDateTime.now());
        final List<OrderLineItem> orderLineItems = request.getOrderLineItems().stream()
                .map(each -> new OrderLineItem(findMenuById(each.getMenuId()), each.getQuantity()))
                .collect(Collectors.toList());
        order.addOrderLineItem(orderLineItems);

        orderRepository.save(order);
        orderLineItemRepository.saveAll(orderLineItems);
        return OrderResponse.from(order);
    }

    private OrderTable findOrderTableById(final long id) {
        return orderTableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테이블입니다."));
    }

    private Menu findMenuById(final long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴가 포함되어 있습니다."));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list() {
        return OrderResponse.from(orderRepository.findAll());
    }

    @Transactional
    public OrderResponse changeOrderStatus(final Long orderId, final OrderStatusRequest request) {
        final Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        order.updateStatus(OrderStatus.valueOf(request.getOrderStatus()));
        return OrderResponse.from(order);
    }
}
