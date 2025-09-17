package mango.challenge.products.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotNull(message = "El nombre no puede ser nulo")
    private String name;

    @NotNull(message = "La descripción no puede ser nula")
    private String description;

}