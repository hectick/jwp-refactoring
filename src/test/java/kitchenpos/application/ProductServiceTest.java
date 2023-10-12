package kitchenpos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.math.BigDecimal;
import java.util.List;
import kitchenpos.domain.Product;
import kitchenpos.supports.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("상품 서비스 테스트")
@ServiceTest
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @DisplayName("상품을 생성할 수 있다")
    @ParameterizedTest
    @ValueSource(ints = {0, 10000})
    void createProduct(int price) {
        final Product product = new Product();
        product.setName("알리오 올리오");
        product.setPrice(BigDecimal.valueOf(price));

        final Product savedProduct = productService.create(product);

        assertSoftly(softly -> {
            assertThat(savedProduct.getId()).isNotNull();
            assertThat(savedProduct.getName()).isEqualTo(product.getName());
        });
    }

    @DisplayName("가격이 없으면 예외처리 한다")
    @Test
    void throwExceptionWhenPriceIsNull() {
        final Product product = new Product();
        product.setName("알리오 올리오");

        assertThatThrownBy(() -> productService.create(product))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("가격이 0 미만이면 예외처리 한다")
    @ParameterizedTest
    @ValueSource(ints = {-10000, -1})
    void throwExceptionWhenPriceIsLowerThanZero(int price) {
        final Product product = new Product();
        product.setName("알리오 올리오");
        product.setPrice(BigDecimal.valueOf(price));

        assertThatThrownBy(() -> productService.create(product))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상품 목록을 조회할 수 있다")
    @Test
    void findAllProducts() {
        productService.create(ProductFixture.create());
        productService.create(ProductFixture.create());

        final List<Product> list = productService.list();

        assertThat(list).hasSize(2);
    }
}
