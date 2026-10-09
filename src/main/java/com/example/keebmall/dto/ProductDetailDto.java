package com.example.keebmall.dto;

import com.example.keebmall.domain.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter
@NoArgsConstructor
public class ProductDetailDto {
    private Long id;
    private String name;
    private String prodCtgCd;
    private String prodCtgNm;
    private String prodTypeCd;
    private String prodTypeNm;
    private String imgUrl;
    private int price;
    private int stock;
    private String prodStatus;
    private LocalDateTime crtdDate;
    private List<ProductOptionDto> productOptions;

    public ProductDetailDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.prodCtgCd = product.getProdCtgCd();
        this.prodCtgNm = product.getProdCtgNm();
        this.prodTypeCd = product.getProdTypeCd();
        this.prodTypeNm = product.getProdTypeNm();
        this.imgUrl = product.getImgUrl();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.prodStatus = product.getProdStatus();
        this.crtdDate = product.getCrtdDate();
        if (product.getProductOptions() != null) {
            this.productOptions = product.getProductOptions().stream()
                    .map(ProductOptionDto::new)
                    .collect(Collectors.toList());
        }
    }
}
