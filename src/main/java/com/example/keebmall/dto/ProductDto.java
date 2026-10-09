package com.example.keebmall.dto;

import com.example.keebmall.domain.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class ProductDto {
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

    public ProductDto(Product product) {
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
    }

}
