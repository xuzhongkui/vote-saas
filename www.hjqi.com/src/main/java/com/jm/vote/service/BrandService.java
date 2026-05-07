package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.entity.Brand;
import com.jm.vote.repository.BrandMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandMapper brandMapper;

    /**
     * 创建品牌
     */
    @Transactional
    public Brand createBrand(Long merchantId, String nameZh, String nameEn, String logo, String descriptionZh, String descriptionEn) {
        Brand brand = Brand.builder()
                .merchantId(merchantId)
                .nameZh(nameZh)
                .nameEn(nameEn)
                .logo(logo)
                .descriptionZh(descriptionZh)
                .descriptionEn(descriptionEn)
                .status(1)
                .sort(0)
                .deleted(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        brandMapper.insert(brand);
        return brand;
    }

    /**
     * 获取品牌列表
     */
    public Page<Brand> getBrands(Long merchantId, int page, int size) {
        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Brand::getMerchantId, merchantId)
                .eq(Brand::getDeleted, 0)
                .eq(Brand::getStatus, 1)
                .orderByAsc(Brand::getSort)
                .orderByDesc(Brand::getCreatedAt);
        
        return brandMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 获取所有品牌（不分页）
     */
    public List<Brand> getAllBrands(Long merchantId) {
        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Brand::getMerchantId, merchantId)
                .eq(Brand::getDeleted, 0)
                .eq(Brand::getStatus, 1)
                .orderByAsc(Brand::getSort);
        
        return brandMapper.selectList(wrapper);
    }

    /**
     * 更新品牌
     */
    @Transactional
    public Brand updateBrand(Long id, Long merchantId, String nameZh, String nameEn, String logo, String descriptionZh, String descriptionEn, Integer sort, Integer status) {
        Brand brand = brandMapper.selectById(id);
        if (brand == null || !brand.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("品牌不存在或无权限");
        }
        
        if (nameZh != null) brand.setNameZh(nameZh);
        if (nameEn != null) brand.setNameEn(nameEn);
        if (logo != null) brand.setLogo(logo);
        if (descriptionZh != null) brand.setDescriptionZh(descriptionZh);
        if (descriptionEn != null) brand.setDescriptionEn(descriptionEn);
        if (sort != null) brand.setSort(sort);
        if (status != null) brand.setStatus(status);
        
        brand.setUpdatedAt(LocalDateTime.now());
        brandMapper.updateById(brand);
        return brand;
    }

    /**
     * 删除品牌（软删除）
     */
    @Transactional
    public void deleteBrand(Long id, Long merchantId) {
        Brand brand = brandMapper.selectById(id);
        if (brand == null || !brand.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("品牌不存在或无权限");
        }
        
        brand.setDeleted(1);
        brand.setUpdatedAt(LocalDateTime.now());
        brandMapper.updateById(brand);
    }
}

