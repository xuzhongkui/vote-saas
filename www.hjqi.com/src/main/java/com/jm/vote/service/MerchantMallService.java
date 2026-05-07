package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jm.vote.dto.*;
import com.jm.vote.entity.*;
import com.jm.vote.repository.*;
import com.jm.vote.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantMallService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final MerchantConfigMapper merchantConfigMapper;
    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final SecurityUtil securityUtil;
    private final PromotionService promotionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Long getCurrentMerchantId() {
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "商家未登录");
        }
        return merchantId;
    }

    // ==================== 分类管理 ====================

    public List<CategoryDTO> getCategories() {
        Long merchantId = getCurrentMerchantId();
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getMerchantId, merchantId)
                        .eq(Category::getDeleted, 0)
                        .orderByAsc(Category::getSort)
        );
        return categories.stream().map(this::toCategoryDTO).collect(Collectors.toList());
    }

    public CategoryDTO createCategory(CreateCategoryRequest request) {
        Long merchantId = getCurrentMerchantId();
        Category category = new Category();
        category.setMerchantId(merchantId);
        category.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        category.setNameZh(request.getNameZh());
        category.setNameEn(request.getNameEn());
        category.setIcon(request.getIcon());
        category.setSort(request.getSort() != null ? request.getSort() : 0);
        category.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        category.setDeleted(0);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.insert(category);
        return toCategoryDTO(category);
    }

    public CategoryDTO updateCategory(Long id, UpdateCategoryRequest request) {
        Long merchantId = getCurrentMerchantId();
        Category category = categoryMapper.selectOne(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getId, id)
                        .eq(Category::getMerchantId, merchantId)
                        .eq(Category::getDeleted, 0)
        );
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "分类不存在");
        }
        if (request.getParentId() != null) category.setParentId(request.getParentId());
        if (request.getNameZh() != null) category.setNameZh(request.getNameZh());
        if (request.getNameEn() != null) category.setNameEn(request.getNameEn());
        if (request.getIcon() != null) category.setIcon(request.getIcon());
        if (request.getSort() != null) category.setSort(request.getSort());
        if (request.getStatus() != null) category.setStatus(request.getStatus());
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.updateById(category);
        return toCategoryDTO(category);
    }

    public void deleteCategory(Long id) {
        Long merchantId = getCurrentMerchantId();
        Category category = categoryMapper.selectOne(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getId, id)
                        .eq(Category::getMerchantId, merchantId)
                        .eq(Category::getDeleted, 0)
        );
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "分类不存在");
        }
        category.setDeleted(1);
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.updateById(category);
    }

    private CategoryDTO toCategoryDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setParentId(category.getParentId());
        dto.setNameZh(category.getNameZh());
        dto.setNameEn(category.getNameEn());
        dto.setIcon(category.getIcon());
        dto.setSort(category.getSort());
        dto.setStatus(category.getStatus());
        dto.setCreatedAt(category.getCreatedAt());
        return dto;
    }

    // ==================== 商品管理 ====================

    public Page<ProductListItemDTO> getProducts(int page, int size, Long categoryId, Integer status) {
        Long merchantId = getCurrentMerchantId();
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getMerchantId, merchantId)
                .eq(Product::getDeleted, 0);
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getCreatedAt);
        Page<Product> productPage = new Page<>(page, size);
        Page<Product> result = productMapper.selectPage(productPage, wrapper);
        Page<ProductListItemDTO> dtoPage = new Page<>(page, size, result.getTotal());
        dtoPage.setRecords(result.getRecords().stream().map(this::toProductListItemDTO).collect(Collectors.toList()));
        return dtoPage;
    }

    public ProductDetailDTO getProduct(Long id) {
        Long merchantId = getCurrentMerchantId();
        Product product = productMapper.selectOne(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getId, id)
                        .eq(Product::getMerchantId, merchantId)
                        .eq(Product::getDeleted, 0)
        );
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        ProductDetailDTO dto = toProductDetailDTO(product);
        // 加载SKU列表
        List<ProductSku> skus = productSkuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, id)
                        .eq(ProductSku::getDeleted, 0)
        );
        dto.setSkus(skus.stream().map(this::toProductSkuDTO).collect(Collectors.toList()));
        return dto;
    }

    @Transactional
    public ProductDetailDTO createProduct(CreateProductRequest request) {
        Long merchantId = getCurrentMerchantId();
        Product product = new Product();
        product.setMerchantId(merchantId);
        product.setNameZh(request.getNameZh());
        product.setSubtitleZh(request.getSubtitleZh());
        product.setCategoryId(request.getCategoryId());
        product.setBrandId(request.getBrandId());
        product.setOriginalPrice(request.getOriginalPrice());
        // 如果价格为空，设置为0（通常是因为有SKU，价格由SKU决定）
        product.setPrice(request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO);
        product.setStock(request.getStock() != null ? request.getStock() : 0);
        product.setSales(0);
        product.setMainImage(request.getMainImage());
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            try {
                product.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "图片格式错误");
            }
        }
        product.setDescriptionZh(request.getDescriptionZh());
        product.setShippingTemplateId(request.getShippingTemplateId());
        product.setAfterSaleRuleId(request.getAfterSaleRuleId());
        product.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        product.setDeleted(0);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.insert(product);
        return toProductDetailDTO(product);
    }

    @Transactional
    public ProductDetailDTO updateProduct(Long id, UpdateProductRequest request) {
        Long merchantId = getCurrentMerchantId();
        Product product = productMapper.selectOne(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getId, id)
                        .eq(Product::getMerchantId, merchantId)
                        .eq(Product::getDeleted, 0)
        );
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        if (request.getNameZh() != null) product.setNameZh(request.getNameZh());
        if (request.getSubtitleZh() != null) product.setSubtitleZh(request.getSubtitleZh());
        if (request.getCategoryId() != null) product.setCategoryId(request.getCategoryId());
        if (request.getBrandId() != null) product.setBrandId(request.getBrandId());
        if (request.getOriginalPrice() != null) product.setOriginalPrice(request.getOriginalPrice());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getMainImage() != null) product.setMainImage(request.getMainImage());
        if (request.getImages() != null) {
            try {
                product.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "图片格式错误");
            }
        }
        if (request.getDescriptionZh() != null) product.setDescriptionZh(request.getDescriptionZh());
        if (request.getShippingTemplateId() != null) product.setShippingTemplateId(request.getShippingTemplateId());
        if (request.getAfterSaleRuleId() != null) product.setAfterSaleRuleId(request.getAfterSaleRuleId());
        if (request.getStatus() != null) product.setStatus(request.getStatus());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
        return toProductDetailDTO(product);
    }

    public void deleteProduct(Long id) {
        Long merchantId = getCurrentMerchantId();
        Product product = productMapper.selectOne(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getId, id)
                        .eq(Product::getMerchantId, merchantId)
                        .eq(Product::getDeleted, 0)
        );
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        product.setDeleted(1);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
    }

    private ProductListItemDTO toProductListItemDTO(Product product) {
        ProductListItemDTO dto = new ProductListItemDTO();
        dto.setId(product.getId());
        dto.setCategoryId(product.getCategoryId());
        dto.setNameZh(product.getNameZh());
        dto.setNameEn(product.getNameEn());
        dto.setMainImage(product.getMainImage());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setSales(product.getSales());
        dto.setStatus(product.getStatus());
        dto.setIsRecommend(product.getIsRecommend());
        dto.setIsNew(product.getIsNew());
        dto.setIsHot(product.getIsHot());
        // 检查商品是否有SKU
        Long skuCount = productSkuMapper.selectCount(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, product.getId())
                        .eq(ProductSku::getDeleted, 0)
        );
        dto.setHasSkus(skuCount != null && skuCount > 0);
        return dto;
    }

    private ProductDetailDTO toProductDetailDTO(Product product) {
        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setId(product.getId());
        dto.setCategoryId(product.getCategoryId());
        dto.setBrandId(product.getBrandId());
        dto.setNameZh(product.getNameZh());
        dto.setNameEn(product.getNameEn());
        dto.setSubtitleZh(product.getSubtitleZh());
        dto.setSubtitleEn(product.getSubtitleEn());
        dto.setMainImage(product.getMainImage());
        if (product.getImages() != null) {
            try {
                dto.setImages(objectMapper.readValue(product.getImages(), new TypeReference<List<String>>() {}));
            } catch (Exception e) {
                dto.setImages(List.of());
            }
        }
        dto.setVideo(product.getVideo());
        dto.setDescriptionZh(product.getDescriptionZh());
        dto.setDescriptionEn(product.getDescriptionEn());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setSales(product.getSales());
        dto.setUnitZh(product.getUnitZh());
        dto.setUnitEn(product.getUnitEn());
        dto.setWeight(product.getWeight());
        dto.setShippingTemplateId(product.getShippingTemplateId());
        dto.setAfterSaleRuleId(product.getAfterSaleRuleId());
        dto.setStatus(product.getStatus());
        dto.setSort(product.getSort());
        dto.setIsRecommend(product.getIsRecommend());
        dto.setIsNew(product.getIsNew());
        dto.setIsHot(product.getIsHot());
        dto.setLimitCount(product.getLimitCount());
        return dto;
    }

    // ==================== SKU管理 ====================

    public List<ProductSkuDTO> getProductSkus(Long productId) {
        Long merchantId = getCurrentMerchantId();
        // 验证商品属于当前商家
        Product product = productMapper.selectOne(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getId, productId)
                        .eq(Product::getMerchantId, merchantId)
                        .eq(Product::getDeleted, 0)
        );
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        List<ProductSku> skus = productSkuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, productId)
                        .eq(ProductSku::getDeleted, 0)
        );
        return skus.stream().map(this::toProductSkuDTO).collect(Collectors.toList());
    }

    public ProductSkuDTO createProductSku(CreateProductSkuRequest request) {
        Long merchantId = getCurrentMerchantId();
        // 验证商品属于当前商家
        Product product = productMapper.selectOne(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getId, request.getProductId())
                        .eq(Product::getMerchantId, merchantId)
                        .eq(Product::getDeleted, 0)
        );
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        ProductSku sku = new ProductSku();
        sku.setMerchantId(merchantId);
        sku.setProductId(request.getProductId());
        // 如果SKU编号为空，自动生成
        if (request.getSkuCode() == null || request.getSkuCode().trim().isEmpty()) {
            // 生成格式：PRODUCT_{productId}_SKU_{timestamp}
            sku.setSkuCode("PRODUCT_" + request.getProductId() + "_SKU_" + System.currentTimeMillis());
        } else {
            sku.setSkuCode(request.getSkuCode().trim());
        }
        sku.setSpecNameZh(request.getSpecNameZh());
        sku.setSpecNameEn(request.getSpecNameEn());
        if (request.getSpecValues() != null) {
            try {
                sku.setSpecValues(objectMapper.writeValueAsString(request.getSpecValues()));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "规格值格式错误");
            }
        }
        sku.setImage(request.getImage());
        sku.setOriginalPrice(request.getOriginalPrice());
        sku.setPrice(request.getPrice());
        sku.setStock(request.getStock() != null ? request.getStock() : 0);
        sku.setSales(0);
        sku.setWeight(request.getWeight());
        sku.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        sku.setDeleted(0);
        sku.setCreatedAt(LocalDateTime.now());
        sku.setUpdatedAt(LocalDateTime.now());
        productSkuMapper.insert(sku);
        return toProductSkuDTO(sku);
    }

    public ProductSkuDTO updateProductSku(Long id, UpdateProductSkuRequest request) {
        Long merchantId = getCurrentMerchantId();
        ProductSku sku = productSkuMapper.selectOne(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getId, id)
                        .eq(ProductSku::getMerchantId, merchantId)
                        .eq(ProductSku::getDeleted, 0)
        );
        if (sku == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SKU不存在");
        }
        if (request.getSkuCode() != null) sku.setSkuCode(request.getSkuCode());
        if (request.getSpecNameZh() != null) sku.setSpecNameZh(request.getSpecNameZh());
        if (request.getSpecNameEn() != null) sku.setSpecNameEn(request.getSpecNameEn());
        if (request.getSpecValues() != null) {
            try {
                sku.setSpecValues(objectMapper.writeValueAsString(request.getSpecValues()));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "规格值格式错误");
            }
        }
        if (request.getImage() != null) sku.setImage(request.getImage());
        if (request.getOriginalPrice() != null) sku.setOriginalPrice(request.getOriginalPrice());
        if (request.getPrice() != null) sku.setPrice(request.getPrice());
        if (request.getStock() != null) sku.setStock(request.getStock());
        if (request.getWeight() != null) sku.setWeight(request.getWeight());
        if (request.getStatus() != null) sku.setStatus(request.getStatus());
        sku.setUpdatedAt(LocalDateTime.now());
        productSkuMapper.updateById(sku);
        return toProductSkuDTO(sku);
    }

    public void deleteProductSku(Long id) {
        Long merchantId = getCurrentMerchantId();
        ProductSku sku = productSkuMapper.selectOne(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getId, id)
                        .eq(ProductSku::getMerchantId, merchantId)
                        .eq(ProductSku::getDeleted, 0)
        );
        if (sku == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SKU不存在");
        }
        sku.setDeleted(1);
        sku.setUpdatedAt(LocalDateTime.now());
        productSkuMapper.updateById(sku);
    }

    private ProductSkuDTO toProductSkuDTO(ProductSku sku) {
        ProductSkuDTO dto = new ProductSkuDTO();
        dto.setId(sku.getId());
        dto.setProductId(sku.getProductId());
        dto.setSkuCode(sku.getSkuCode());
        dto.setSpecNameZh(sku.getSpecNameZh());
        dto.setSpecNameEn(sku.getSpecNameEn());
        // 设置skuName用于前端显示（优先使用中文名称）
        dto.setSkuName(sku.getSpecNameZh() != null && !sku.getSpecNameZh().isEmpty() 
                ? sku.getSpecNameZh() 
                : (sku.getSpecNameEn() != null ? sku.getSpecNameEn() : "默认规格"));
        if (sku.getSpecValues() != null) {
            try {
                dto.setSpecValues(objectMapper.readValue(sku.getSpecValues(), new TypeReference<Map<String, Object>>() {}));
            } catch (Exception e) {
                dto.setSpecValues(Map.of());
            }
        }
        dto.setImage(sku.getImage());
        dto.setOriginalPrice(sku.getOriginalPrice());
        dto.setPrice(sku.getPrice());
        dto.setStock(sku.getStock());
        dto.setSales(sku.getSales());
        dto.setWeight(sku.getWeight());
        dto.setStatus(sku.getStatus());
        dto.setCreatedAt(sku.getCreatedAt());
        dto.setUpdatedAt(sku.getUpdatedAt());
        return dto;
    }

    // ==================== 订单管理 ====================

    public Page<MerchantOrderListDTO> getOrders(int page, int size, String status) {
        Long merchantId = getCurrentMerchantId();
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getMerchantId, merchantId)
                .eq(Order::getDeleted, 0);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        Page<Order> orderPage = new Page<>(page, size);
        Page<Order> result = orderMapper.selectPage(orderPage, wrapper);
        Page<MerchantOrderListDTO> dtoPage = new Page<>(page, size, result.getTotal());
        dtoPage.setRecords(result.getRecords().stream().map(this::toMerchantOrderListDTO).collect(Collectors.toList()));
        return dtoPage;
    }

    public OrderDetailDTO getOrder(Long id) {
        Long merchantId = getCurrentMerchantId();
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getId, id)
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getDeleted, 0)
        );
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        OrderDetailDTO dto = toOrderDetailDTO(order);
        // 加载订单项
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, id)
                        .eq(OrderItem::getDeleted, 0)
        );
        dto.setItems(items.stream().map(this::toOrderItemDTO).collect(Collectors.toList()));
        return dto;
    }

    @Transactional
    public OrderDetailDTO confirmOrder(Long id, UpdateOrderStatusRequest request) {
        Long merchantId = getCurrentMerchantId();
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getId, id)
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getDeleted, 0)
        );
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "订单状态不正确");
        }
        order.setStatus("CONFIRMED");
        order.setConfirmedAt(LocalDateTime.now());
        if (request.getMerchantRemark() != null) {
            order.setMerchantRemark(request.getMerchantRemark());
        }
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 发送订单确认通知
        try {
            notificationService.sendOrderNotification(order.getMerchantId(), order.getId(), "ORDER_CONFIRMED", order);
        } catch (Exception e) {
            // 通知失败不影响订单处理
        }
        
        return getOrder(id);
    }

    @Transactional
    public OrderDetailDTO shipOrder(Long id, UpdateOrderStatusRequest request) {
        Long merchantId = getCurrentMerchantId();
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getId, id)
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getDeleted, 0)
        );
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        if (!"CONFIRMED".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "订单状态不正确");
        }
        order.setStatus("SHIPPING");
        order.setShippedAt(LocalDateTime.now());
        if (request.getMerchantRemark() != null) {
            order.setMerchantRemark(request.getMerchantRemark());
        }
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 发送订单发货通知
        try {
            notificationService.sendOrderNotification(order.getMerchantId(), order.getId(), "ORDER_SHIPPED", order);
        } catch (Exception e) {
            // 通知失败不影响订单处理
        }
        
        return getOrder(id);
    }

    @Transactional
    public OrderDetailDTO completeOrder(Long id) {
        Long merchantId = getCurrentMerchantId();
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getId, id)
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getDeleted, 0)
        );
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        if (!"SHIPPING".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "订单状态不正确");
        }
        order.setStatus("COMPLETED");
        order.setCompletedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 发送订单完成通知
        try {
            notificationService.sendOrderNotification(order.getMerchantId(), order.getId(), "ORDER_COMPLETED", order);
        } catch (Exception e) {
            // 通知失败不影响订单处理
        }
        
        return getOrder(id);
    }

    @Transactional
    public OrderDetailDTO updateOrderRemark(Long id, UpdateOrderStatusRequest request) {
        Long merchantId = getCurrentMerchantId();
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getId, id)
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getDeleted, 0)
        );
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        if (request.getMerchantRemark() != null) {
            order.setMerchantRemark(request.getMerchantRemark());
        }
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return getOrder(id);
    }

    @Transactional
    public OrderDetailDTO cancelOrder(Long id, UpdateOrderStatusRequest request) {
        Long merchantId = getCurrentMerchantId();
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getId, id)
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getDeleted, 0)
        );
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "订单状态不正确");
        }
        
        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, id)
                        .eq(OrderItem::getDeleted, 0)
        );
        for (OrderItem item : items) {
            if (item.getSkuId() != null && item.getSkuId() > 0) {
                // 有SKU，恢复SKU库存
                ProductSku sku = productSkuMapper.selectById(item.getSkuId());
                if (sku != null) {
                    sku.setStock(sku.getStock() + item.getQuantity());
                    sku.setSales(sku.getSales() != null && sku.getSales() >= item.getQuantity() 
                        ? sku.getSales() - item.getQuantity() : 0);
                    sku.setUpdatedAt(LocalDateTime.now());
                    productSkuMapper.updateById(sku);
                    log.info("恢复SKU库存: skuId={}, quantity={}, newStock={}", 
                        sku.getId(), item.getQuantity(), sku.getStock());
                }
            } else {
                // 无SKU，恢复商品库存
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    product.setSales(product.getSales() != null && product.getSales() >= item.getQuantity() 
                        ? product.getSales() - item.getQuantity() : 0);
                    product.setUpdatedAt(LocalDateTime.now());
                    productMapper.updateById(product);
                    log.info("恢复商品库存: productId={}, quantity={}, newStock={}", 
                        product.getId(), item.getQuantity(), product.getStock());
                }
            }
        }
        
        order.setStatus("CANCELLED");
        order.setCancelledAt(LocalDateTime.now());
        if (request.getCancelReason() != null) {
            order.setCancelReason(request.getCancelReason());
        }
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 发送订单取消通知
        try {
            notificationService.sendOrderNotification(order.getMerchantId(), order.getId(), "ORDER_CANCELLED", order);
        } catch (Exception e) {
            // 通知失败不影响订单处理
        }
        
        return getOrder(id);
    }

    /**
     * 删除订单（软删除）
     */
    @Transactional
    public void deleteOrder(Long id) {
        Long merchantId = getCurrentMerchantId();
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getId, id)
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getDeleted, 0)
        );
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        
        // 只有已取消或已完成的订单才能删除
        if (!"CANCELLED".equals(order.getStatus()) && !"COMPLETED".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只能删除已取消或已完成的订单");
        }
        
        order.setDeleted(1);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    private MerchantOrderListDTO toMerchantOrderListDTO(Order order) {
        MerchantOrderListDTO dto = new MerchantOrderListDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        
        // 查询用户名称
        if (order.getUserId() != null) {
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                dto.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        }
        
        dto.setReceiverName(order.getReceiverName());
        dto.setReceiverPhone(order.getReceiverPhone());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setProductCount(order.getProductCount());
        dto.setDeliveryType(order.getDeliveryType());
        dto.setExpectedDate(order.getExpectedDate());
        dto.setExpectedTime(order.getExpectedTime());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setConfirmedAt(order.getConfirmedAt());
        dto.setShippedAt(order.getShippedAt());
        dto.setCompletedAt(order.getCompletedAt());
        
        // 查询订单商品列表
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, order.getId())
                        .eq(OrderItem::getDeleted, 0)
        );
        dto.setItems(items.stream().map(this::toOrderItemDTO).collect(Collectors.toList()));
        
        return dto;
    }

    private OrderDetailDTO toOrderDetailDTO(Order order) {
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setStatus(order.getStatus());
        dto.setProductAmount(order.getProductAmount());
        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setProductCount(order.getProductCount());
        dto.setDeliveryType(order.getDeliveryType());
        dto.setReceiverName(order.getReceiverName());
        dto.setReceiverPhone(order.getReceiverPhone());
        dto.setReceiverAddress(order.getReceiverAddress());
        dto.setExpectedDate(order.getExpectedDate());
        dto.setExpectedTime(order.getExpectedTime());
        dto.setUserRemark(order.getUserRemark());
        dto.setMerchantRemark(order.getMerchantRemark());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setConfirmedAt(order.getConfirmedAt());
        dto.setShippedAt(order.getShippedAt());
        dto.setCompletedAt(order.getCompletedAt());
        dto.setCancelledAt(order.getCancelledAt());
        dto.setCancelReason(order.getCancelReason());
        return dto;
    }

    private OrderItemDTO toOrderItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setSkuId(item.getSkuId());
        dto.setProductName(item.getProductNameZh() != null ? item.getProductNameZh() : item.getProductNameEn());
        dto.setProductNameZh(item.getProductNameZh());
        dto.setProductNameEn(item.getProductNameEn());
        dto.setProductImage(item.getProductImage());
        dto.setSkuName(item.getSpecNameZh() != null ? item.getSpecNameZh() : item.getSpecNameEn());
        dto.setSpecNameZh(item.getSpecNameZh());
        dto.setSpecNameEn(item.getSpecNameEn());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }

    // ==================== 店铺配置管理 ====================

    public MerchantConfigDTO getMerchantConfig() {
        Long merchantId = getCurrentMerchantId();
        MerchantConfig config = merchantConfigMapper.selectOne(
                new LambdaQueryWrapper<MerchantConfig>()
                        .eq(MerchantConfig::getMerchantId, merchantId)
                        .eq(MerchantConfig::getDeleted, 0)
        );
        if (config == null) {
            // 如果不存在，创建一个默认配置
            config = new MerchantConfig();
            config.setMerchantId(merchantId);
            config.setMinOrderAmount(BigDecimal.ZERO);
            config.setDeliveryFee(BigDecimal.ZERO);
            config.setSupportPickup(true);
            config.setSupportDelivery(true);
            config.setAdvanceBookingDays(0);
            config.setDeleted(0);
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            merchantConfigMapper.insert(config);
        }
        return toMerchantConfigDTO(config);
    }

    @Transactional
    public MerchantConfigDTO updateMerchantConfig(UpdateMerchantConfigRequest request) {
        Long merchantId = getCurrentMerchantId();
        MerchantConfig config = merchantConfigMapper.selectOne(
                new LambdaQueryWrapper<MerchantConfig>()
                        .eq(MerchantConfig::getMerchantId, merchantId)
                        .eq(MerchantConfig::getDeleted, 0)
        );
        
        if (config == null) {
            config = new MerchantConfig();
            config.setMerchantId(merchantId);
            config.setDeleted(0);
            config.setCreatedAt(LocalDateTime.now());
        }
        
        if (request.getPickupAddressZh() != null) config.setPickupAddressZh(request.getPickupAddressZh());
        if (request.getPickupAddressEn() != null) config.setPickupAddressEn(request.getPickupAddressEn());
        if (request.getDeliveryAreaZh() != null) config.setDeliveryAreaZh(request.getDeliveryAreaZh());
        if (request.getDeliveryAreaEn() != null) config.setDeliveryAreaEn(request.getDeliveryAreaEn());
        if (request.getMinOrderAmount() != null) config.setMinOrderAmount(request.getMinOrderAmount());
        if (request.getDeliveryFee() != null) config.setDeliveryFee(request.getDeliveryFee());
        if (request.getFreeDeliveryAmount() != null) config.setFreeDeliveryAmount(request.getFreeDeliveryAmount());
        if (request.getSupportPickup() != null) config.setSupportPickup(request.getSupportPickup());
        if (request.getSupportDelivery() != null) config.setSupportDelivery(request.getSupportDelivery());
        if (request.getDeliveryTimeSlots() != null) {
            try {
                config.setDeliveryTimeSlots(objectMapper.writeValueAsString(request.getDeliveryTimeSlots()));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "配送时间段格式错误");
            }
        }
        if (request.getPickupTimeSlots() != null) {
            try {
                config.setPickupTimeSlots(objectMapper.writeValueAsString(request.getPickupTimeSlots()));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "自提时间段格式错误");
            }
        }
        if (request.getAdvanceBookingDays() != null) config.setAdvanceBookingDays(request.getAdvanceBookingDays());
        if (request.getMaxDeliveryDistance() != null) config.setMaxDeliveryDistance(request.getMaxDeliveryDistance());
        
        config.setUpdatedAt(LocalDateTime.now());
        if (config.getId() == null) {
            merchantConfigMapper.insert(config);
        } else {
            merchantConfigMapper.updateById(config);
        }
        return toMerchantConfigDTO(config);
    }

    private MerchantConfigDTO toMerchantConfigDTO(MerchantConfig config) {
        MerchantConfigDTO dto = new MerchantConfigDTO();
        dto.setId(config.getId());
        dto.setMerchantId(config.getMerchantId());
        dto.setPickupAddressZh(config.getPickupAddressZh());
        dto.setPickupAddressEn(config.getPickupAddressEn());
        dto.setDeliveryAreaZh(config.getDeliveryAreaZh());
        dto.setDeliveryAreaEn(config.getDeliveryAreaEn());
        dto.setMinOrderAmount(config.getMinOrderAmount());
        dto.setDeliveryFee(config.getDeliveryFee());
        dto.setFreeDeliveryAmount(config.getFreeDeliveryAmount());
        dto.setSupportPickup(config.getSupportPickup());
        dto.setSupportDelivery(config.getSupportDelivery());
        if (config.getDeliveryTimeSlots() != null) {
            try {
                dto.setDeliveryTimeSlots(objectMapper.readValue(config.getDeliveryTimeSlots(), new TypeReference<Map<String, Object>>() {}));
            } catch (Exception e) {
                dto.setDeliveryTimeSlots(Map.of());
            }
        }
        if (config.getPickupTimeSlots() != null) {
            try {
                dto.setPickupTimeSlots(objectMapper.readValue(config.getPickupTimeSlots(), new TypeReference<Map<String, Object>>() {}));
            } catch (Exception e) {
                dto.setPickupTimeSlots(Map.of());
            }
        }
        dto.setAdvanceBookingDays(config.getAdvanceBookingDays());
        dto.setMaxDeliveryDistance(config.getMaxDeliveryDistance());
        dto.setCreatedAt(config.getCreatedAt());
        dto.setUpdatedAt(config.getUpdatedAt());
        return dto;
    }

    // ==================== 店铺信息管理 ====================

    public MerchantInfoDTO getMerchantInfo() {
        Long merchantId = getCurrentMerchantId();
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null || merchant.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在");
        }
        return toMerchantInfoDTO(merchant);
    }

    @Transactional
    public MerchantInfoDTO updateMerchantInfo(UpdateMerchantInfoRequest request) {
        Long merchantId = getCurrentMerchantId();
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null || merchant.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在");
        }
        
        if (request.getShopNameZh() != null) merchant.setShopNameZh(request.getShopNameZh());
        if (request.getShopNameEn() != null) merchant.setShopNameEn(request.getShopNameEn());
        if (request.getLogo() != null) merchant.setLogo(request.getLogo());
        if (request.getBanner() != null) merchant.setBanner(request.getBanner());
        if (request.getDescriptionZh() != null) merchant.setDescriptionZh(request.getDescriptionZh());
        if (request.getDescriptionEn() != null) merchant.setDescriptionEn(request.getDescriptionEn());
        if (request.getContactName() != null) merchant.setContactName(request.getContactName());
        if (request.getContactPhone() != null) merchant.setContactPhone(request.getContactPhone());
        // businessHours 允许设置为空字符串（清空）
        if (request.getBusinessHours() != null) {
            merchant.setBusinessHours(request.getBusinessHours().trim().isEmpty() ? null : request.getBusinessHours().trim());
        }
        if (request.getServiceWechat() != null) {
            merchant.setServiceWechat(request.getServiceWechat().trim().isEmpty() ? null : request.getServiceWechat().trim());
        }
        if (request.getServicePhone() != null) {
            merchant.setServicePhone(request.getServicePhone().trim().isEmpty() ? null : request.getServicePhone().trim());
        }
        
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);
        return toMerchantInfoDTO(merchant);
    }

    private MerchantInfoDTO toMerchantInfoDTO(Merchant merchant) {
        MerchantInfoDTO dto = new MerchantInfoDTO();
        dto.setId(merchant.getId());
        dto.setUsername(merchant.getUsername());
        dto.setEmail(merchant.getEmail());
        dto.setPhone(merchant.getPhone());
        dto.setShopNameZh(merchant.getShopNameZh());
        dto.setShopNameEn(merchant.getShopNameEn());
        dto.setLogo(merchant.getLogo());
        dto.setBanner(merchant.getBanner());
        dto.setContactName(merchant.getContactName());
        dto.setContactPhone(merchant.getContactPhone());
        dto.setInviteCode(merchant.getInviteCode());
        dto.setPromotionCode(merchant.getPromotionCode());
        // 使用 PromotionService 生成最新的推广链接，确保使用最新的 baseUrl
        dto.setPromotionLink(promotionService.generatePromotionLink(merchant.getId()));
        dto.setPaymentStatus(merchant.getPaymentStatus());
        dto.setPaymentAmount(merchant.getPaymentAmount());
        dto.setPaymentTime(merchant.getPaymentTime());
        dto.setStatus(merchant.getStatus());
        dto.setServiceWechat(merchant.getServiceWechat());
        dto.setServicePhone(merchant.getServicePhone());
        dto.setBusinessHours(merchant.getBusinessHours());
        dto.setTotalReward(merchant.getTotalReward() != null ? merchant.getTotalReward() : BigDecimal.ZERO);
        dto.setAvailableReward(merchant.getAvailableReward() != null ? merchant.getAvailableReward() : BigDecimal.ZERO);
        return dto;
    }

    // ==================== 数据统计 ====================

    public MerchantStatisticsDTO getStatistics() {
        Long merchantId = getCurrentMerchantId();
        log.info("获取商家统计数据, merchantId: {}", merchantId);
        
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate monthStart = today.withDayOfMonth(1);
        
        MerchantStatisticsDTO stats = new MerchantStatisticsDTO();
        
        // 今日统计
        Long todayCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getDeleted, 0)
                        .ge(Order::getCreatedAt, today.atStartOfDay())
        );
        stats.setTodayOrderCount(todayCount);
        
        List<Order> todayOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getDeleted, 0)
                        .ge(Order::getCreatedAt, today.atStartOfDay())
        );
        BigDecimal todaySales = todayOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTodaySalesAmount(todaySales);
        
        // 本周统计
        Long weekCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getDeleted, 0)
                        .ge(Order::getCreatedAt, weekStart.atStartOfDay())
        );
        stats.setWeekOrderCount(weekCount);
        
        List<Order> weekOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getDeleted, 0)
                        .ge(Order::getCreatedAt, weekStart.atStartOfDay())
        );
        BigDecimal weekSales = weekOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setWeekSalesAmount(weekSales);
        
        // 本月统计
        Long monthCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getDeleted, 0)
                        .ge(Order::getCreatedAt, monthStart.atStartOfDay())
        );
        stats.setMonthOrderCount(monthCount);
        
        List<Order> monthOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getDeleted, 0)
                        .ge(Order::getCreatedAt, monthStart.atStartOfDay())
        );
        BigDecimal monthSales = monthOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setMonthSalesAmount(monthSales);
        
        // 总统计
        Long totalCount = orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getDeleted, 0)
        );
        stats.setTotalOrderCount(totalCount);
        
        List<Order> allOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getDeleted, 0)
        );
        BigDecimal totalSales = allOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalSalesAmount(totalSales);
        
        // 用户数
        Long userCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getMerchantId, merchantId)
                        .eq(User::getDeleted, 0)
                        .eq(User::getStatus, 1)
        );
        stats.setUserCount(userCount);
        
        // 商品数
        Long productCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getMerchantId, merchantId)
                        .eq(Product::getDeleted, 0)
        );
        stats.setProductCount(productCount);
        
        // 订单状态统计
        stats.setPendingOrderCount(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getStatus, "PENDING")
                        .eq(Order::getDeleted, 0)
        ));
        stats.setConfirmedOrderCount(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getStatus, "CONFIRMED")
                        .eq(Order::getDeleted, 0)
        ));
        stats.setShippingOrderCount(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getStatus, "SHIPPING")
                        .eq(Order::getDeleted, 0)
        ));
        stats.setCompletedOrderCount(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getStatus, "COMPLETED")
                        .eq(Order::getDeleted, 0)
        ));
        stats.setCancelledOrderCount(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getStatus, "CANCELLED")
                        .eq(Order::getDeleted, 0)
        ));
        
        log.info("商家统计数据结果 - merchantId: {}, 总订单: {}, 总销售额: {}, 商品数: {}, 用户数: {}", 
                merchantId, stats.getTotalOrderCount(), stats.getTotalSalesAmount(), 
                stats.getProductCount(), stats.getUserCount());
        
        return stats;
    }
}

