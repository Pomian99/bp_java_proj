package org.example.service;

import org.example.model.OrderItem;
import org.example.model.Product;
import org.example.model.ProductType;
import org.example.model.discount.AppliedDiscount;
import org.example.model.discount.Discount;
import org.example.model.discount.FixedAdjustment;
import org.example.model.discount.PercentageAdjustment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountManagerTest {

    @Test
    void getBestDiscount_picksDiscountWithLowestResultingTotal_amongApplicableOnes() {
        DiscountManager manager = new DiscountManager();

        Product product = Product.builder()
                .type(ProductType.SMARTPHONE)
                .name("Phone")
                .basePrice(BigDecimal.valueOf(1000))
                .availableQuantity(5)
                .build();
        OrderItem item = new OrderItem(product, List.of(), 1);

        Discount smallFixedDiscount = new Discount("10 PLN off", items -> true,
                new FixedAdjustment(BigDecimal.TEN), Instant.now().plusSeconds(3600));
        Discount bigPercentageDiscount = new Discount("20% off", items -> true,
                new PercentageAdjustment(BigDecimal.valueOf(20)), Instant.now().plusSeconds(3600));
        Discount expiredDiscount = new Discount("50% off (expired)", items -> true,
                new PercentageAdjustment(BigDecimal.valueOf(50)), Instant.now().minusSeconds(1));

        manager.addDiscount(smallFixedDiscount);
        manager.addDiscount(bigPercentageDiscount);
        manager.addDiscount(expiredDiscount);

        Optional<AppliedDiscount> best = manager.getBestDiscount(List.of(item));

        assertThat(best).isPresent();
        assertThat(best.get().description()).isEqualTo("20% off");
        assertThat(best.get().amount()).isEqualByComparingTo(BigDecimal.valueOf(200));
    }
}
