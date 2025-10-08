package com.amdocs.spx.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketTypeDTO {


    private Long eventId;
    private String typeName;
    private BigDecimal price;
    private Integer quantityAvailable;
    private Integer quantitySold;
    private Boolean isActive;
}
