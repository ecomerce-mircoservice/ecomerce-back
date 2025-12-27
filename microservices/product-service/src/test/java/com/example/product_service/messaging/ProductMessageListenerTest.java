package com.example.product_service.messaging;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.product_service.dto.ProductStockUpdateEvent;
import com.example.product_service.service.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductMessageListenerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductMessageListener productMessageListener;

    @Test
    void handleStockUpdate_WhenReserve_ShouldCallReserveStock() {
        ProductStockUpdateEvent event = new ProductStockUpdateEvent();
        event.setProductId(1L);
        event.setQuantityChanged(5);
        event.setOperation("RESERVE");

        when(productService.reserveStock(1L, 5)).thenReturn(true);

        productMessageListener.handleStockUpdate(event);

        verify(productService, times(1)).reserveStock(1L, 5);
    }

    @Test
    void handleStockUpdate_WhenRelease_ShouldCallReleaseStock() {
        ProductStockUpdateEvent event = new ProductStockUpdateEvent();
        event.setProductId(1L);
        event.setQuantityChanged(5);
        event.setOperation("RELEASE");

        productMessageListener.handleStockUpdate(event);

        verify(productService, times(1)).releaseStock(1L, 5);
    }
}
