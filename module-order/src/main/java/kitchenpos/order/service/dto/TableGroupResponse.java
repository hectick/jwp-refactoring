package kitchenpos.order.service.dto;

import java.time.LocalDateTime;
import java.util.List;
import kitchenpos.order.domain.OrderTable;
import kitchenpos.order.domain.TableGroup;

public class TableGroupResponse {

    private final long id;
    private final LocalDateTime createdDate;
    private final List<TableResponse> orderTables;

    private TableGroupResponse(final long id, final LocalDateTime createdDate, final List<TableResponse> orderTables) {
        this.id = id;
        this.createdDate = createdDate;
        this.orderTables = orderTables;
    }

    public static TableGroupResponse from(final TableGroup tableGroup, final List<OrderTable> orderTables) {
        return new TableGroupResponse(
                tableGroup.getId(),
                tableGroup.getCreatedDate(),
                TableResponse.from(orderTables)
        );
    }

    public long getId() {
        return id;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public List<TableResponse> getOrderTables() {
        return orderTables;
    }
}
