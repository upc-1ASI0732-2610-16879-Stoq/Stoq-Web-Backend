package com.inventiapp.stocktrack.inventory.application.internal.commandservices;

import com.inventiapp.stocktrack.inventory.domain.exceptions.ProductAlreadyExistsException;
import com.inventiapp.stocktrack.inventory.domain.model.commands.CreateProductCommand;
import com.inventiapp.stocktrack.inventory.infrastructure.internal.CategoryRepository;
import com.inventiapp.stocktrack.inventory.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.inventiapp.stocktrack.inventory.infrastructure.persistence.jpa.repositories.ProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCommandServiceImplUnitTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private ProductCommandServiceImpl productCommandService;

    @Test
    // US01 - Registrar producto nuevo: impedir producto duplicado.
    void shouldRejectDuplicateProductByNameAndProvider() {
        CreateProductCommand command = new CreateProductCommand(
                "Arroz",
                "Bolsa",
                "1",
                "10",
                5,
                8.0,
                true
        );

        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(providerRepository.existsById(10L)).thenReturn(true);
        when(productRepository.existsByNameAndProviderId("Arroz", "10")).thenReturn(true);

        assertThrows(ProductAlreadyExistsException.class, () -> productCommandService.handle(command));

        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
