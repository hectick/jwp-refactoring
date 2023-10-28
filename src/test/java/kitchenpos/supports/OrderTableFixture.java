package kitchenpos.supports;

import kitchenpos.order.service.dto.TableRequest;

public class OrderTableFixture {

    public static TableRequest createNotEmpty() {
        return new TableRequest(4, false);
    }

    public static TableRequest createEmpty() {
        return new TableRequest( true);
    }
}
