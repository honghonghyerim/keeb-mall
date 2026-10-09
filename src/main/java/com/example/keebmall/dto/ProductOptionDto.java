package com.example.keebmall.dto;


import com.example.keebmall.domain.ProductOption;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class ProductOptionDto {

    private Long id;
    private String kbdColor;
    private String kbdLayout;
    private int stock;

    public ProductOptionDto(ProductOption productOption) {
        this.id = productOption.getId();
        this.kbdColor = productOption.getKbdColor();
        this.kbdLayout = productOption.getKbdLayout();
        this.stock = productOption.getStock();
    }

}
