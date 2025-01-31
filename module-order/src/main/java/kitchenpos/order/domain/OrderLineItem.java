package kitchenpos.order.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import kitchenpos.common.vo.Price;

@Entity
public class OrderLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;
    @Embedded
    private MenuDetails menuDetails;
    @Column(nullable = false)
    private long quantity;

    protected OrderLineItem() {
    }

    public OrderLineItem(final long menuId, final long quantity) {
        this.menuDetails = new MenuDetails(menuId);
        this.quantity = quantity;
    }

    public Long getSeq() {
        return seq;
    }

    public long getMenuId() {
        return menuDetails.getMenuId();
    }

    public String getName() {
        return menuDetails.getName();
    }

    public Price getPrice() {
        return menuDetails.getPrice();
    }

    public MenuDetails getMenuDetails() {
        return menuDetails;
    }

    public long getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OrderLineItem that = (OrderLineItem) o;
        return Objects.equals(seq, that.seq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seq);
    }
}
