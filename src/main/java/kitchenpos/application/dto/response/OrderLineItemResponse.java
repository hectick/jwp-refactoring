package kitchenpos.application.dto.response;

import java.util.List;
import java.util.stream.Collectors;
import kitchenpos.domain.OrderLineItem;

public class OrderLineItemResponse {

    private final long seq;
    private final long orderId;
    private final long menuId;
    private final long quantity;

    private OrderLineItemResponse(final long seq, final long orderId, final long menuId, final long quantity) {
        this.seq = seq;
        this.orderId = orderId;
        this.menuId = menuId;
        this.quantity = quantity;
    }

    public static List<OrderLineItemResponse> from(final List<OrderLineItem> orderLineItems) {
        return orderLineItems.stream().map(each -> new OrderLineItemResponse(
                each.getSeq(),
                each.getOrder().getId(),
                each.getMenu().getId(),
                each.getQuantity()
        )).collect(Collectors.toList());
    }

    public long getSeq() {
        return seq;
    }

    public long getOrderId() {
        return orderId;
    }

    public long getMenuId() {
        return menuId;
    }

    public long getQuantity() {
        return quantity;
    }
}
