package com.jm.vote.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.*;
import com.jm.vote.entity.*;
import com.jm.vote.repository.*;
import com.jm.vote.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMallService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductSkuMapper productSkuMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserAddressMapper userAddressMapper;
    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;
    private final ShippingTemplateService shippingTemplateService;
    private final AfterSaleRuleService afterSaleRuleService;
    private final PromotionService promotionService;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        Claims claims = getClaimsFromRequest(request);
        Long userId = claims.get("userId", Long.class);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户未登录");
        }
        return userId;
    }

    private Long getMerchantIdFromRequest(HttpServletRequest request) {
        Claims claims = getClaimsFromRequest(request);
        Long merchantId = claims.get("merchantId", Long.class);
        if (merchantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户未绑定商家");
        }
        return merchantId;
    }

    private Claims getClaimsFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未提供认证信息");
        }
        String token = authHeader.substring(7);
        try {
            return jwtUtil.parseToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "无效的令牌");
        }
    }

    public List<CategoryDTO> listCategories(HttpServletRequest request) {
        Long merchantId = getMerchantIdFromRequest(request);
        List<Category> categories = categoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Category>()
                        .eq(Category::getMerchantId, merchantId)
                        .eq(Category::getStatus, 1)
                        .eq(Category::getDeleted, 0)
                        .orderByAsc(Category::getSort)
        );
        return categories.stream().map(c -> {
            CategoryDTO dto = new CategoryDTO();
            dto.setId(c.getId());
            dto.setParentId(c.getParentId());
            dto.setNameZh(c.getNameZh());
            dto.setNameEn(c.getNameEn());
            dto.setIcon(c.getIcon());
            return dto;
        }).collect(Collectors.toList());
    }

    public Page<ProductListItemDTO> pageProducts(HttpServletRequest request, Long categoryId, String keyword, int page, int size) {
        Long merchantId = getMerchantIdFromRequest(request);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                        .eq(Product::getMerchantId, merchantId)
                        .eq(Product::getStatus, 1)
                        .eq(Product::getDeleted, 0)
                        .eq(categoryId != null, Product::getCategoryId, categoryId);
        
        // 搜索关键词过滤（商品名称中英文）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Product::getNameZh, keyword.trim())
                    .or()
                    .like(Product::getNameEn, keyword.trim())
                    .or()
                    .like(Product::getSubtitleZh, keyword.trim())
                    .or()
                    .like(Product::getSubtitleEn, keyword.trim()));
        }
        
        wrapper.orderByAsc(Product::getSort);
        
        Page<Product> p = productMapper.selectPage(Page.of(page, size), wrapper);

        Page<ProductListItemDTO> result = new Page<>();
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setTotal(p.getTotal());
        result.setRecords(
                p.getRecords().stream().map(prod -> {
                    ProductListItemDTO dto = new ProductListItemDTO();
                    dto.setId(prod.getId());
                    dto.setCategoryId(prod.getCategoryId());
                    dto.setNameZh(prod.getNameZh());
                    dto.setNameEn(prod.getNameEn());
                    dto.setMainImage(prod.getMainImage());
                    dto.setPrice(prod.getPrice());
                    dto.setSales(prod.getSales());
                    dto.setIsRecommend(prod.getIsRecommend());
                    dto.setIsNew(prod.getIsNew());
                    dto.setIsHot(prod.getIsHot());
                    
                    // 加载SKU信息
                    List<ProductSku> skus = productSkuMapper.selectList(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductSku>()
                                    .eq(ProductSku::getProductId, prod.getId())
                                    .eq(ProductSku::getDeleted, 0)
                                    .eq(ProductSku::getStatus, 1) // 只加载启用的SKU
                                    .orderByAsc(ProductSku::getCreatedAt)
                    );
                    dto.setHasSkus(skus != null && !skus.isEmpty());
                    if (skus != null && !skus.isEmpty()) {
                        dto.setSkus(skus.stream().map(sku -> {
                            ProductSkuDTO skuDto = new ProductSkuDTO();
                            skuDto.setId(sku.getId());
                            skuDto.setProductId(sku.getProductId());
                            skuDto.setSkuCode(sku.getSkuCode());
                            skuDto.setSkuName(sku.getSpecNameZh() != null && !sku.getSpecNameZh().isEmpty() 
                                    ? sku.getSpecNameZh() 
                                    : (sku.getSpecNameEn() != null ? sku.getSpecNameEn() : "默认规格"));
                            skuDto.setSpecNameZh(sku.getSpecNameZh());
                            skuDto.setSpecNameEn(sku.getSpecNameEn());
                            skuDto.setPrice(sku.getPrice());
                            skuDto.setStock(sku.getStock());
                            skuDto.setImage(sku.getImage());
                            skuDto.setStatus(sku.getStatus());
                            return skuDto;
                        }).collect(Collectors.toList()));
                    }
                    
                    return dto;
                }).collect(Collectors.toList())
        );
        return result;
    }

    /**
     * 获取推荐商品（带merchantId过滤）
     */
    public Page<ProductListItemDTO> getRecommendProducts(HttpServletRequest request, int page, int size) {
        Long merchantId = getMerchantIdFromRequest(request);
        Page<Product> p = productMapper.selectPage(
                Page.of(page, size),
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                        .eq(Product::getMerchantId, merchantId)
                        .eq(Product::getStatus, 1)
                        .eq(Product::getDeleted, 0)
                        .eq(Product::getIsRecommend, true)
                        .orderByDesc(Product::getSales)
                        .orderByDesc(Product::getCreatedAt)
        );

        Page<ProductListItemDTO> result = new Page<>();
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setTotal(p.getTotal());
        result.setRecords(
                p.getRecords().stream().map(prod -> {
                    ProductListItemDTO dto = new ProductListItemDTO();
                    dto.setId(prod.getId());
                    dto.setCategoryId(prod.getCategoryId());
                    dto.setNameZh(prod.getNameZh());
                    dto.setNameEn(prod.getNameEn());
                    dto.setMainImage(prod.getMainImage());
                    dto.setPrice(prod.getPrice());
                    dto.setSales(prod.getSales());
                    dto.setIsRecommend(prod.getIsRecommend());
                    dto.setIsNew(prod.getIsNew());
                    dto.setIsHot(prod.getIsHot());
                    
                    // 加载SKU信息
                    List<ProductSku> skus = productSkuMapper.selectList(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductSku>()
                                    .eq(ProductSku::getProductId, prod.getId())
                                    .eq(ProductSku::getDeleted, 0)
                                    .eq(ProductSku::getStatus, 1) // 只加载启用的SKU
                                    .orderByAsc(ProductSku::getCreatedAt)
                    );
                    dto.setHasSkus(skus != null && !skus.isEmpty());
                    if (skus != null && !skus.isEmpty()) {
                        dto.setSkus(skus.stream().map(sku -> {
                            ProductSkuDTO skuDto = new ProductSkuDTO();
                            skuDto.setId(sku.getId());
                            skuDto.setProductId(sku.getProductId());
                            skuDto.setSkuCode(sku.getSkuCode());
                            skuDto.setSkuName(sku.getSpecNameZh() != null && !sku.getSpecNameZh().isEmpty() 
                                    ? sku.getSpecNameZh() 
                                    : (sku.getSpecNameEn() != null ? sku.getSpecNameEn() : "默认规格"));
                            skuDto.setSpecNameZh(sku.getSpecNameZh());
                            skuDto.setSpecNameEn(sku.getSpecNameEn());
                            skuDto.setPrice(sku.getPrice());
                            skuDto.setStock(sku.getStock());
                            skuDto.setImage(sku.getImage());
                            skuDto.setStatus(sku.getStatus());
                            return skuDto;
                        }).collect(Collectors.toList()));
                    }
                    
                    return dto;
                }).collect(Collectors.toList())
        );
        return result;
    }

    public ProductDetailDTO getProductDetail(HttpServletRequest request, Long productId) {
        Long merchantId = getMerchantIdFromRequest(request);
        Product prod = productMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                        .eq(Product::getId, productId)
                        .eq(Product::getMerchantId, merchantId)
                        .eq(Product::getStatus, 1)
                        .eq(Product::getDeleted, 0)
        );
        if (prod == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setId(prod.getId());
        dto.setCategoryId(prod.getCategoryId());
        dto.setNameZh(prod.getNameZh());
        dto.setNameEn(prod.getNameEn());
        dto.setSubtitleZh(prod.getSubtitleZh());
        dto.setSubtitleEn(prod.getSubtitleEn());
        dto.setMainImage(prod.getMainImage());
        // images 字段暂时设置为空列表，如果需要解析 JSON，需要添加 ObjectMapper
        if (prod.getImages() != null && !prod.getImages().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                dto.setImages(mapper.readValue(prod.getImages(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}));
            } catch (Exception e) {
                dto.setImages(List.of());
            }
        } else {
            dto.setImages(List.of());
        }
        dto.setVideo(prod.getVideo());
        dto.setDescriptionZh(prod.getDescriptionZh());
        dto.setDescriptionEn(prod.getDescriptionEn());
        dto.setPrice(prod.getPrice());
        dto.setStock(prod.getStock());
        dto.setUnitZh(prod.getUnitZh());
        dto.setUnitEn(prod.getUnitEn());
        dto.setShippingTemplateId(prod.getShippingTemplateId());
        dto.setAfterSaleRuleId(prod.getAfterSaleRuleId());
        dto.setWeight(prod.getWeight());
        dto.setOriginalPrice(prod.getOriginalPrice());
        dto.setSales(prod.getSales());
        
        // 加载SKU信息
        List<ProductSku> skus = productSkuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, productId)
                        .eq(ProductSku::getDeleted, 0)
                        .orderByAsc(ProductSku::getCreatedAt)
        );
        dto.setSkus(skus.stream().map(sku -> {
            ProductSkuDTO skuDto = new ProductSkuDTO();
            skuDto.setId(sku.getId());
            // 使用规格名称作为SKU名称
            skuDto.setSkuName(sku.getSpecNameZh() != null && !sku.getSpecNameZh().isEmpty() 
                    ? sku.getSpecNameZh() 
                    : (sku.getSpecNameEn() != null ? sku.getSpecNameEn() : "默认规格"));
            skuDto.setPrice(sku.getPrice());
            skuDto.setStock(sku.getStock());
            skuDto.setImage(sku.getImage());
            return skuDto;
        }).collect(Collectors.toList()));
        
        // 加载运费模板信息
        if (prod.getShippingTemplateId() != null) {
            try {
                ShippingTemplate template = shippingTemplateService.getTemplate(prod.getShippingTemplateId(), merchantId);
                ProductDetailDTO.ShippingTemplateInfo templateInfo = new ProductDetailDTO.ShippingTemplateInfo();
                templateInfo.setId(template.getId());
                templateInfo.setName(template.getName());
                templateInfo.setType(template.getType());
                templateInfo.setFreeShippingAmount(template.getFreeShippingAmount());
                templateInfo.setFreeShippingCondition(template.getFreeShippingCondition());
                templateInfo.setDefaultFee(template.getDefaultFee());
                
                // 加载运费规则
                List<ShippingTemplateRule> rules = shippingTemplateService.getTemplateRules(template.getId(), merchantId);
                templateInfo.setRules(rules.stream().map(rule -> {
                    ProductDetailDTO.ShippingTemplateRuleInfo ruleInfo = new ProductDetailDTO.ShippingTemplateRuleInfo();
                    ruleInfo.setRegionNames(rule.getRegionNames());
                    ruleInfo.setFirstFee(rule.getFirstFee());
                    ruleInfo.setContinueFee(rule.getContinueFee());
                    ruleInfo.setFirstWeight(rule.getFirstWeight());
                    ruleInfo.setContinueWeight(rule.getContinueWeight());
                    return ruleInfo;
                }).collect(Collectors.toList()));
                
                dto.setShippingTemplate(templateInfo);
            } catch (Exception e) {
                log.warn("加载运费模板失败: {}", e.getMessage());
            }
        }
        
        // 加载售后规则信息
        if (prod.getAfterSaleRuleId() != null) {
            try {
                List<AfterSaleRule> rules = afterSaleRuleService.getRules(merchantId, productId, null);
                AfterSaleRule rule = rules.stream()
                        .filter(r -> r.getId().equals(prod.getAfterSaleRuleId()))
                        .findFirst()
                        .orElse(null);
                if (rule == null) {
                    // 如果找不到商品特定的规则，尝试查找通用规则
                    rule = rules.stream()
                            .filter(r -> r.getProductId() == null)
                            .findFirst()
                            .orElse(null);
                }
                if (rule != null) {
                    ProductDetailDTO.AfterSaleRuleInfo ruleInfo = new ProductDetailDTO.AfterSaleRuleInfo();
                    ruleInfo.setId(rule.getId());
                    ruleInfo.setTitleZh(rule.getTitleZh());
                    ruleInfo.setTitleEn(rule.getTitleEn());
                    ruleInfo.setContentZh(rule.getContentZh());
                    ruleInfo.setContentEn(rule.getContentEn());
                    ruleInfo.setValidDays(rule.getValidDays());
                    ruleInfo.setRuleType(rule.getRuleType());
                    dto.setAfterSaleRule(ruleInfo);
                }
            } catch (Exception e) {
                log.warn("加载售后规则失败: {}", e.getMessage());
            }
        }
        
        return dto;
    }

    public List<CartItemDTO> listCartItems(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Long merchantId = getMerchantIdFromRequest(request);
        List<CartItem> items = cartItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getMerchantId, merchantId)
                        .eq(CartItem::getDeleted, 0)
                        .orderByDesc(CartItem::getCreatedAt)
        );

        return items.stream().map(ci -> {
            CartItemDTO dto = new CartItemDTO();
            dto.setId(ci.getId());
            dto.setProductId(ci.getProductId());
            // 将 skuId = 0 转换回 null（0 表示无 SKU）
            dto.setSkuId(ci.getSkuId() != null && ci.getSkuId() > 0 ? ci.getSkuId() : null);
            dto.setQuantity(ci.getQuantity());
            dto.setSelected(ci.getSelected());

            Product product = productMapper.selectById(ci.getProductId());
            if (product != null) {
                dto.setProductNameZh(product.getNameZh());
                dto.setProductNameEn(product.getNameEn());
                dto.setProductImage(product.getMainImage());
            }
            BigDecimal price = product != null ? product.getPrice() : BigDecimal.ZERO;
            // 只有当 skuId > 0 时才查询 SKU
            if (ci.getSkuId() != null && ci.getSkuId() > 0) {
                ProductSku sku = productSkuMapper.selectById(ci.getSkuId());
                if (sku != null) {
                    dto.setSpecNameZh(sku.getSpecNameZh());
                    dto.setSpecNameEn(sku.getSpecNameEn());
                    price = sku.getPrice();
                }
            }
            dto.setPrice(price);
            return dto;
        }).collect(Collectors.toList());
    }

    public void addOrUpdateCartItem(HttpServletRequest request, Long productId, Long skuId, Integer quantity) {
        Long userId = getUserIdFromRequest(request);
        Long merchantId = getMerchantIdFromRequest(request);

        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "数量必须大于0");
        }

        // 将 null 的 skuId 转换为 0，避免 MySQL 唯一索引对 NULL 值的特殊处理
        Long effectiveSkuId = (skuId == null || skuId == 0) ? 0L : skuId;

        // 先查询是否存在记录（包括已删除的，因为唯一索引包含已删除记录）
        CartItem existing = cartItemMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getMerchantId, merchantId)
                        .eq(CartItem::getProductId, productId)
                        .eq(CartItem::getSkuId, effectiveSkuId)
        );
        
        if (existing == null) {
            // 不存在记录，插入新记录
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setMerchantId(merchantId);
            item.setProductId(productId);
            item.setSkuId(effectiveSkuId);
            item.setQuantity(quantity);
            item.setSelected(true);
            item.setDeleted(0);
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            cartItemMapper.insert(item);
        } else if (existing.getDeleted() == 1) {
            // 存在已删除的记录，恢复并更新数量
            existing.setQuantity(quantity);
            existing.setSelected(true);
            existing.setDeleted(0);
            existing.setUpdatedAt(LocalDateTime.now());
            cartItemMapper.updateById(existing);
        } else {
            // 存在未删除的记录，累加数量
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setUpdatedAt(LocalDateTime.now());
            cartItemMapper.updateById(existing);
        }
    }

    public void updateCartItem(HttpServletRequest request, Long cartItemId, Integer quantity, Boolean selected) {
        Long userId = getUserIdFromRequest(request);
        Long merchantId = getMerchantIdFromRequest(request);
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getUserId().equals(userId) || !item.getMerchantId().equals(merchantId) || item.getDeleted() != 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "购物车项不存在");
        }
        if (quantity != null) {
            if (quantity <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "数量必须大于0");
            }
            item.setQuantity(quantity);
        }
        if (selected != null) {
            item.setSelected(selected);
        }
        item.setUpdatedAt(LocalDateTime.now());
        cartItemMapper.updateById(item);
    }

    public void deleteCartItem(HttpServletRequest request, Long cartItemId) {
        Long userId = getUserIdFromRequest(request);
        Long merchantId = getMerchantIdFromRequest(request);
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getUserId().equals(userId) || !item.getMerchantId().equals(merchantId) || item.getDeleted() != 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "购物车项不存在");
        }
        item.setDeleted(1);
        item.setUpdatedAt(LocalDateTime.now());
        cartItemMapper.updateById(item);
    }

    public OrderDetailDTO createOrder(HttpServletRequest request, CreateOrderRequest createReq) {
        Long userId = getUserIdFromRequest(request);
        Long merchantId = getMerchantIdFromRequest(request);

        List<CartItem> cartItems = cartItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getMerchantId, merchantId)
                        .eq(CartItem::getDeleted, 0)
                        .eq(CartItem::getSelected, true)
        );
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "没有选中的商品");
        }

        BigDecimal productAmount = BigDecimal.ZERO;
        int productCount = 0;
        Long shippingTemplateId = null;

        // 先计算金额，并获取运费模板ID
        for (CartItem item : cartItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null || product.getStatus() == null || product.getStatus() != 1 || product.getDeleted() != 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "包含已下架或不存在的商品");
            }
            BigDecimal price = product.getPrice();
            if (item.getSkuId() != null) {
                ProductSku sku = productSkuMapper.selectById(item.getSkuId());
                if (sku != null && sku.getStatus() == 1 && sku.getDeleted() == 0) {
                    price = sku.getPrice();
                }
            }
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
            productAmount = productAmount.add(subtotal);
            productCount += item.getQuantity();
            
            // 获取商品的运费模板ID（使用第一个有运费模板的商品）
            if (shippingTemplateId == null && product.getShippingTemplateId() != null) {
                shippingTemplateId = product.getShippingTemplateId();
            }
        }

        // 计算运费（只有同城配送才计算运费，自提不收运费）
        BigDecimal deliveryFee = BigDecimal.ZERO;
        Integer deliveryType = createReq.getDeliveryType();
        
        log.info("开始计算运费: deliveryType={}, shippingTemplateId={}, productAmount={}", 
                deliveryType, shippingTemplateId, productAmount);
        
        // deliveryType: 1-自提（免运费）, 2-同城配送（根据运费模板计算）
        if (deliveryType != null && deliveryType == 2) {
            if (shippingTemplateId != null) {
                try {
                    ShippingTemplate template = shippingTemplateService.getTemplate(shippingTemplateId, merchantId);
                    log.info("获取到运费模板: id={}, name={}, status={}, defaultFee={}, freeShippingAmount={}", 
                            template != null ? template.getId() : null,
                            template != null ? template.getName() : null,
                            template != null ? template.getStatus() : null,
                            template != null ? template.getDefaultFee() : null,
                            template != null ? template.getFreeShippingAmount() : null);
                    
                    if (template != null && template.getStatus() == 1) {
                        // 检查是否满足免运费条件
                        if (template.getFreeShippingAmount() != null && 
                            productAmount.compareTo(template.getFreeShippingAmount()) >= 0) {
                            deliveryFee = BigDecimal.ZERO; // 满足包邮条件
                            log.info("订单满足包邮条件: productAmount={}, freeShippingAmount={}", 
                                    productAmount, template.getFreeShippingAmount());
                        } else {
                            // 使用默认运费
                            deliveryFee = template.getDefaultFee() != null ? template.getDefaultFee() : BigDecimal.ZERO;
                            log.info("订单使用默认运费: deliveryFee={}", deliveryFee);
                        }
                    } else {
                        log.warn("运费模板不存在或未启用: templateId={}", shippingTemplateId);
                    }
                } catch (Exception e) {
                    log.warn("计算运费失败，使用默认运费0: {}", e.getMessage());
                }
            } else {
                log.warn("商品没有关联运费模板，使用默认运费0");
            }
        } else if (deliveryType != null && deliveryType == 1) {
            log.info("订单为自提方式，免运费");
        }
        
        log.info("最终运费: deliveryFee={}", deliveryFee);
        
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = productAmount.add(deliveryFee).subtract(discountAmount);

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setStatus("PENDING");
        order.setProductAmount(productAmount);
        order.setDeliveryFee(deliveryFee);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);
        order.setProductCount(productCount);
        order.setDeliveryType(createReq.getDeliveryType());
        order.setReceiverName(createReq.getReceiverName());
        order.setReceiverPhone(createReq.getReceiverPhone());
        order.setReceiverAddress(createReq.getReceiverAddress());
        order.setExpectedDate(createReq.getExpectedDate());
        // expectedTime 字段保留，但提货日期只精确到日期，不设置时间
        order.setExpectedTime(null);
        order.setUserRemark(createReq.getUserRemark());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setDeleted(0);

        orderMapper.insert(order);

        // 创建订单项并扣减库存
        for (CartItem item : cartItems) {
            Product product = productMapper.selectById(item.getProductId());
            ProductSku sku = item.getSkuId() != null ? productSkuMapper.selectById(item.getSkuId()) : null;
            BigDecimal price = sku != null ? sku.getPrice() : product.getPrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));

            // 检查库存并扣减
            if (sku != null) {
                // 有SKU，扣减SKU库存
                if (sku.getStock() < item.getQuantity()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "商品【" + product.getNameZh() + "】规格【" + sku.getSpecNameZh() + "】库存不足");
                }
                sku.setStock(sku.getStock() - item.getQuantity());
                sku.setSales(sku.getSales() != null ? sku.getSales() + item.getQuantity() : item.getQuantity());
                sku.setUpdatedAt(LocalDateTime.now());
                productSkuMapper.updateById(sku);
                log.info("扣减SKU库存: skuId={}, quantity={}, remainStock={}", 
                    sku.getId(), item.getQuantity(), sku.getStock());
            } else {
                // 无SKU，扣减商品库存
                if (product.getStock() < item.getQuantity()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "商品【" + product.getNameZh() + "】库存不足");
                }
                product.setStock(product.getStock() - item.getQuantity());
                product.setSales(product.getSales() != null ? product.getSales() + item.getQuantity() : item.getQuantity());
                product.setUpdatedAt(LocalDateTime.now());
                productMapper.updateById(product);
                log.info("扣减商品库存: productId={}, quantity={}, remainStock={}", 
                    product.getId(), item.getQuantity(), product.getStock());
            }

            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setProductId(item.getProductId());
            oi.setSkuId(item.getSkuId());
            oi.setProductNameZh(product.getNameZh());
            oi.setProductNameEn(product.getNameEn());
            oi.setProductImage(product.getMainImage());
            if (sku != null) {
                oi.setSpecNameZh(sku.getSpecNameZh());
                oi.setSpecNameEn(sku.getSpecNameEn());
            }
            oi.setPrice(price);
            oi.setQuantity(item.getQuantity());
            oi.setSubtotal(subtotal);
            oi.setCreatedAt(LocalDateTime.now());
            oi.setUpdatedAt(LocalDateTime.now());
            oi.setDeleted(0);
            orderItemMapper.insert(oi);
        }

        // 清理购物车
        for (CartItem item : cartItems) {
            item.setDeleted(1);
            item.setUpdatedAt(LocalDateTime.now());
            cartItemMapper.updateById(item);
        }

        // 发送新订单通知
        try {
            notificationService.sendOrderNotification(order.getMerchantId(), order.getId(), "ORDER_NEW", order);
        } catch (Exception e) {
            // 通知失败不影响订单创建
        }

        return getOrderDetail(request, order.getId());
    }

    private String generateOrderNo() {
        return "O" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    /**
     * 获取用户订单列表
     * 换绑后：显示所有历史订单（包括之前商家的），但之前商家的订单标记为只读
     */
    public List<OrderSummaryDTO> listUserOrders(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Long currentMerchantId = getMerchantIdFromRequest(request);
        
        // 查询用户的所有订单（不限制merchantId，以显示历史订单）
        List<Order> orders = orderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .eq(Order::getDeleted, 0)
                        .orderByDesc(Order::getCreatedAt)
        );
        
        return orders.stream().map(o -> {
            OrderSummaryDTO dto = new OrderSummaryDTO();
            dto.setId(o.getId());
            dto.setOrderNo(o.getOrderNo());
            dto.setStatus(o.getStatus());
            dto.setTotalAmount(o.getTotalAmount());
            dto.setProductCount(o.getProductCount());
            dto.setCreatedAt(o.getCreatedAt());
            // 如果订单的merchantId不等于当前merchantId，标记为只读（换绑前的订单）
            dto.setReadOnly(!o.getMerchantId().equals(currentMerchantId));
            
            // 查询订单商品列表
            List<OrderItem> items = orderItemMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem>()
                            .eq(OrderItem::getOrderId, o.getId())
                            .eq(OrderItem::getDeleted, 0)
            );
            List<OrderItemDTO> itemDtos = items.stream().map(oi -> {
                OrderItemDTO idto = new OrderItemDTO();
                idto.setId(oi.getId());
                idto.setProductId(oi.getProductId());
                idto.setSkuId(oi.getSkuId());
                idto.setProductName(oi.getProductNameZh() != null ? oi.getProductNameZh() : oi.getProductNameEn());
                idto.setProductNameZh(oi.getProductNameZh());
                idto.setProductNameEn(oi.getProductNameEn());
                idto.setProductImage(oi.getProductImage());
                idto.setSkuName(oi.getSpecNameZh() != null ? oi.getSpecNameZh() : oi.getSpecNameEn());
                idto.setSpecNameZh(oi.getSpecNameZh());
                idto.setSpecNameEn(oi.getSpecNameEn());
                idto.setPrice(oi.getPrice());
                idto.setQuantity(oi.getQuantity());
                idto.setSubtotal(oi.getSubtotal());
                return idto;
            }).collect(Collectors.toList());
            dto.setItems(itemDtos);
            
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 获取订单详情
     * 换绑后：允许查看历史订单（包括之前商家的），但标记为只读
     */
    public OrderDetailDTO getOrderDetail(HttpServletRequest request, Long orderId) {
        Long userId = getUserIdFromRequest(request);
        Long currentMerchantId = getMerchantIdFromRequest(request);
        Order order = orderMapper.selectById(orderId);
        
        // 验证订单属于该用户
        if (order == null || order.getDeleted() != 0 || !order.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        
        // 判断是否为只读订单（换绑前的订单）
        boolean isReadOnly = !order.getMerchantId().equals(currentMerchantId);
        
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setMerchantId(order.getMerchantId());
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
        dto.setReadOnly(isReadOnly); // 标记是否只读

        List<OrderItem> items = orderItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, order.getId())
                        .eq(OrderItem::getDeleted, 0)
        );
        List<OrderItemDTO> itemDtos = items.stream().map(oi -> {
            OrderItemDTO idto = new OrderItemDTO();
            idto.setId(oi.getId());
            idto.setProductId(oi.getProductId());
            idto.setSkuId(oi.getSkuId());
            idto.setProductName(oi.getProductNameZh() != null ? oi.getProductNameZh() : oi.getProductNameEn());
            idto.setProductNameZh(oi.getProductNameZh());
            idto.setProductNameEn(oi.getProductNameEn());
            idto.setProductImage(oi.getProductImage());
            idto.setSkuName(oi.getSpecNameZh() != null ? oi.getSpecNameZh() : oi.getSpecNameEn());
            idto.setSpecNameZh(oi.getSpecNameZh());
            idto.setSpecNameEn(oi.getSpecNameEn());
            idto.setPrice(oi.getPrice());
            idto.setQuantity(oi.getQuantity());
            idto.setSubtotal(oi.getSubtotal());
            return idto;
        }).collect(Collectors.toList());
        dto.setItems(itemDtos);
        return dto;
    }

    // ==================== 地址管理 ====================

    public List<AddressDTO> listAddresses(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<UserAddress> addresses = userAddressMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, userId)
                        .eq(UserAddress::getDeleted, 0)
                        .orderByDesc(UserAddress::getIsDefault)
                        .orderByDesc(UserAddress::getCreatedAt)
        );
        return addresses.stream().map(this::toAddressDTO).collect(Collectors.toList());
    }

    public AddressDTO getAddress(HttpServletRequest request, Long id) {
        Long userId = getUserIdFromRequest(request);
        UserAddress address = userAddressMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getId, id)
                        .eq(UserAddress::getUserId, userId)
                        .eq(UserAddress::getDeleted, 0)
        );
        if (address == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "地址不存在");
        }
        return toAddressDTO(address);
    }

    @org.springframework.transaction.annotation.Transactional
    public AddressDTO createAddress(HttpServletRequest request, CreateAddressRequest req) {
        Long userId = getUserIdFromRequest(request);
        
        // 如果设置为默认地址，先取消其他默认地址
        if (Boolean.TRUE.equals(req.getIsDefault())) {
            List<UserAddress> defaultAddresses = userAddressMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserAddress>()
                            .eq(UserAddress::getUserId, userId)
                            .eq(UserAddress::getIsDefault, true)
                            .eq(UserAddress::getDeleted, 0)
            );
            for (UserAddress addr : defaultAddresses) {
                addr.setIsDefault(false);
                userAddressMapper.updateById(addr);
            }
        }
        
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setReceiverName(req.getReceiverName());
        address.setReceiverPhone(req.getReceiverPhone());
        address.setProvince(req.getProvince());
        address.setCity(req.getCity());
        address.setDistrict(req.getDistrict());
        address.setDetailAddress(req.getDetailAddress());
        address.setFullAddress(req.getFullAddress());
        address.setPostalCode(req.getPostalCode());
        address.setIsDefault(req.getIsDefault() != null ? req.getIsDefault() : false);
        address.setTag(req.getTag());
        address.setDeleted(0);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        userAddressMapper.insert(address);
        return toAddressDTO(address);
    }

    @org.springframework.transaction.annotation.Transactional
    public AddressDTO updateAddress(HttpServletRequest request, Long id, UpdateAddressRequest req) {
        Long userId = getUserIdFromRequest(request);
        UserAddress address = userAddressMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getId, id)
                        .eq(UserAddress::getUserId, userId)
                        .eq(UserAddress::getDeleted, 0)
        );
        if (address == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "地址不存在");
        }
        
        // 如果设置为默认地址，先取消其他默认地址
        if (Boolean.TRUE.equals(req.getIsDefault())) {
            List<UserAddress> defaultAddresses = userAddressMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserAddress>()
                            .eq(UserAddress::getUserId, userId)
                            .eq(UserAddress::getIsDefault, true)
                            .eq(UserAddress::getDeleted, 0)
                            .ne(UserAddress::getId, id)
            );
            for (UserAddress addr : defaultAddresses) {
                addr.setIsDefault(false);
                userAddressMapper.updateById(addr);
            }
        }
        
        if (req.getReceiverName() != null) address.setReceiverName(req.getReceiverName());
        if (req.getReceiverPhone() != null) address.setReceiverPhone(req.getReceiverPhone());
        if (req.getProvince() != null) address.setProvince(req.getProvince());
        if (req.getCity() != null) address.setCity(req.getCity());
        if (req.getDistrict() != null) address.setDistrict(req.getDistrict());
        if (req.getDetailAddress() != null) address.setDetailAddress(req.getDetailAddress());
        if (req.getFullAddress() != null) address.setFullAddress(req.getFullAddress());
        if (req.getPostalCode() != null) address.setPostalCode(req.getPostalCode());
        if (req.getIsDefault() != null) address.setIsDefault(req.getIsDefault());
        if (req.getTag() != null) address.setTag(req.getTag());
        address.setUpdatedAt(LocalDateTime.now());
        userAddressMapper.updateById(address);
        return toAddressDTO(address);
    }

    public void deleteAddress(HttpServletRequest request, Long id) {
        Long userId = getUserIdFromRequest(request);
        UserAddress address = userAddressMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getId, id)
                        .eq(UserAddress::getUserId, userId)
                        .eq(UserAddress::getDeleted, 0)
        );
        if (address == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "地址不存在");
        }
        address.setDeleted(1);
        address.setUpdatedAt(LocalDateTime.now());
        userAddressMapper.updateById(address);
    }

    private AddressDTO toAddressDTO(UserAddress address) {
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setReceiverName(address.getReceiverName());
        dto.setReceiverPhone(address.getReceiverPhone());
        dto.setProvince(address.getProvince());
        dto.setCity(address.getCity());
        dto.setDistrict(address.getDistrict());
        dto.setDetailAddress(address.getDetailAddress());
        dto.setFullAddress(address.getFullAddress());
        dto.setPostalCode(address.getPostalCode());
        dto.setIsDefault(address.getIsDefault());
        dto.setTag(address.getTag());
        dto.setCreatedAt(address.getCreatedAt());
        dto.setUpdatedAt(address.getUpdatedAt());
        return dto;
    }

    // ==================== 个人资料管理 ====================

    public UserInfoDTO updateProfile(HttpServletRequest request, UpdateProfileRequest req) {
        Long userId = getUserIdFromRequest(request);
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        
        if (req.getNickname() != null) user.setNickname(req.getNickname());
        if (req.getRealName() != null) user.setRealName(req.getRealName());
        if (req.getAvatar() != null) user.setAvatar(req.getAvatar());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getLanguage() != null) user.setLanguage(req.getLanguage());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setRealName(user.getRealName());
        dto.setAvatar(user.getAvatar());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setMerchantId(user.getMerchantId());
        dto.setLanguage(user.getLanguage());
        return dto;
    }

    public UserInfoDTO getProfile(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        
        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setRealName(user.getRealName());
        dto.setAvatar(user.getAvatar());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setMerchantId(user.getMerchantId());
        dto.setLanguage(user.getLanguage());
        return dto;
    }

    // ==================== 绑定商家（首次登录）====================

    @org.springframework.transaction.annotation.Transactional
    public BindMerchantResponse bindMerchant(HttpServletRequest request, ChangeMerchantRequest req) {
        Long userId = getUserIdFromRequest(request);
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        
        // 如果已经绑定商家，不允许再次绑定
        if (user.getMerchantId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "您已绑定商家，如需更换请使用更换商家功能");
        }
        
        // 验证邀请码
        Merchant merchant = merchantMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getInviteCode, req.getInviteCode())
                        .eq(Merchant::getStatus, 1) // 必须是已审核通过的商家
                        .eq(Merchant::getDeleted, 0)
        );
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "邀请码无效或商家未审核通过");
        }
        
        // 绑定商家
        user.setMerchantId(merchant.getId());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        // 生成新的 token（包含更新后的 merchantId）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("merchantId", user.getMerchantId());
        claims.put("type", "USER");
        String newToken = jwtUtil.generateToken(user.getUsername(), claims);
        
        // 返回更新后的用户信息和新 token
        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setMerchantId(user.getMerchantId());
        dto.setLanguage(user.getLanguage());
        
        BindMerchantResponse response = new BindMerchantResponse();
        response.setToken(newToken);
        response.setUserInfo(dto);
        
        return response;
    }

    /**
     * 换绑商家（用户可随时申请，无需审核）
     * 换绑后：
     * 1. 原订单只读（保留历史订单，但标记为只读）
     * 2. 新商家重新可见商品（商品查询基于merchantId，自动生效）
     * 3. 历史客服不可再联系（聊天记录基于merchantId，自动隔离）
     */
    @org.springframework.transaction.annotation.Transactional
    public BindMerchantResponse changeMerchant(HttpServletRequest request, ChangeMerchantRequest req) {
        Long userId = getUserIdFromRequest(request);
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        
        Long oldMerchantId = user.getMerchantId();
        
        // 验证邀请码
        Merchant merchant = merchantMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getInviteCode, req.getInviteCode())
                        .eq(Merchant::getStatus, 1) // 必须是已审核通过的商家
                        .eq(Merchant::getDeleted, 0)
        );
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "邀请码无效或商家未审核通过");
        }
        
        // 如果换绑到同一个商家，直接返回
        if (oldMerchantId != null && oldMerchantId.equals(merchant.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "您已经绑定该商家，无需重复绑定");
        }
        
        // 换绑商家（无需审核，直接生效）
        user.setMerchantId(merchant.getId());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("用户换绑商家成功: userId={}, oldMerchantId={}, newMerchantId={}", 
                userId, oldMerchantId, merchant.getId());
        
        // 注意：
        // 1. 原订单只读：订单查询时会基于merchantId过滤，但用户仍可查看所有历史订单（包括之前商家的）
        //    在订单详情中标记为只读（通过判断订单merchantId是否等于当前merchantId）
        // 2. 新商家商品可见：商品查询基于merchantId，换绑后自动生效
        // 3. 历史客服不可联系：聊天记录基于merchantId，换绑后历史聊天记录自动隔离
        
        // 生成新的 token（包含更新后的 merchantId）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("merchantId", user.getMerchantId());
        claims.put("type", "USER");
        String newToken = jwtUtil.generateToken(user.getUsername(), claims);
        
        // 返回更新后的用户信息和新 token
        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setMerchantId(user.getMerchantId());
        dto.setLanguage(user.getLanguage());
        
        BindMerchantResponse response = new BindMerchantResponse();
        response.setToken(newToken);
        response.setUserInfo(dto);
        return response;
    }

    // ==================== 模拟支付 ====================

    @org.springframework.transaction.annotation.Transactional
    public OrderDetailDTO mockPayOrder(HttpServletRequest request, Long orderId) {
        Long userId = getUserIdFromRequest(request);
        Long merchantId = getMerchantIdFromRequest(request);
        Order order = orderMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                        .eq(Order::getId, orderId)
                        .eq(Order::getUserId, userId)
                        .eq(Order::getMerchantId, merchantId)
                        .eq(Order::getDeleted, 0)
        );
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "订单状态不正确，无法支付");
        }
        
        // 模拟支付：直接将订单状态改为已确认
        order.setStatus("CONFIRMED");
        order.setConfirmedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        
        return getOrderDetail(request, orderId);
    }

    // ==================== 用户确认收货 ====================

    @org.springframework.transaction.annotation.Transactional
    public OrderDetailDTO confirmReceive(HttpServletRequest request, Long orderId) {
        Long userId = getUserIdFromRequest(request);
        Order order = orderMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                        .eq(Order::getId, orderId)
                        .eq(Order::getUserId, userId)
                        .eq(Order::getDeleted, 0)
        );
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        if (!"SHIPPING".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "订单状态不正确，无法确认收货");
        }
        
        // 用户确认收货：将订单状态改为已完成
        order.setStatus("COMPLETED");
        order.setCompletedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 发送订单完成通知给商家
        try {
            notificationService.sendOrderNotification(order.getMerchantId(), order.getId(), "ORDER_COMPLETED", order);
        } catch (Exception e) {
            // 通知失败不影响订单处理
        }
        
        // 记录用户消费推广奖励
        try {
            promotionService.recordUserConsumptionReward(userId, orderId, order.getTotalAmount());
            log.info("用户消费推广奖励已记录: userId={}, orderId={}, amount={}", userId, orderId, order.getTotalAmount());
        } catch (Exception e) {
            log.error("记录用户消费推广奖励失败: userId={}, orderId={}, error={}", userId, orderId, e.getMessage(), e);
            // 奖励记录失败不影响订单完成
        }
        
        // 直接构建返回的 DTO，避免调用 getOrderDetail 时的 merchantId 校验问题
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setMerchantId(order.getMerchantId());
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
        dto.setCompletedAt(order.getCompletedAt());
        dto.setReadOnly(false);

        List<OrderItem> items = orderItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, order.getId())
                        .eq(OrderItem::getDeleted, 0)
        );
        List<OrderItemDTO> itemDtos = items.stream().map(oi -> {
            OrderItemDTO idto = new OrderItemDTO();
            idto.setId(oi.getId());
            idto.setProductId(oi.getProductId());
            idto.setSkuId(oi.getSkuId());
            idto.setProductName(oi.getProductNameZh() != null ? oi.getProductNameZh() : oi.getProductNameEn());
            idto.setProductNameZh(oi.getProductNameZh());
            idto.setProductNameEn(oi.getProductNameEn());
            idto.setProductImage(oi.getProductImage());
            idto.setSkuName(oi.getSpecNameZh() != null ? oi.getSpecNameZh() : oi.getSpecNameEn());
            idto.setSpecNameZh(oi.getSpecNameZh());
            idto.setSpecNameEn(oi.getSpecNameEn());
            idto.setPrice(oi.getPrice());
            idto.setQuantity(oi.getQuantity());
            idto.setSubtotal(oi.getSubtotal());
            return idto;
        }).collect(Collectors.toList());
        dto.setItems(itemDtos);
        
        return dto;
    }

    // ==================== 用户取消订单 ====================

    @org.springframework.transaction.annotation.Transactional
    public OrderDetailDTO userCancelOrder(HttpServletRequest request, Long orderId, String cancelReason) {
        Long userId = getUserIdFromRequest(request);
        Order order = orderMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                        .eq(Order::getId, orderId)
                        .eq(Order::getUserId, userId)
                        .eq(Order::getDeleted, 0)
        );
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");
        }
        
        // 用户只能取消待支付(PENDING)或已确认(CONFIRMED)状态的订单
        if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "订单状态不正确，无法取消");
        }
        
        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
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
                    log.info("用户取消订单-恢复SKU库存: skuId={}, quantity={}, newStock={}", 
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
                    log.info("用户取消订单-恢复商品库存: productId={}, quantity={}, newStock={}", 
                        product.getId(), item.getQuantity(), product.getStock());
                }
            }
        }
        
        // 取消订单
        order.setStatus("CANCELLED");
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(cancelReason != null ? cancelReason : "用户取消");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 发送订单取消通知给商家
        try {
            notificationService.sendOrderNotification(order.getMerchantId(), order.getId(), "ORDER_CANCELLED", order);
        } catch (Exception e) {
            // 通知失败不影响订单处理
        }
        
        // 直接构建返回的 DTO
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setMerchantId(order.getMerchantId());
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
        dto.setCancelReason(order.getCancelReason());
        dto.setCancelledAt(order.getCancelledAt());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setReadOnly(false);

        List<OrderItemDTO> itemDtos = items.stream().map(oi -> {
            OrderItemDTO idto = new OrderItemDTO();
            idto.setId(oi.getId());
            idto.setProductId(oi.getProductId());
            idto.setSkuId(oi.getSkuId());
            idto.setProductName(oi.getProductNameZh() != null ? oi.getProductNameZh() : oi.getProductNameEn());
            idto.setProductNameZh(oi.getProductNameZh());
            idto.setProductNameEn(oi.getProductNameEn());
            idto.setProductImage(oi.getProductImage());
            idto.setSkuName(oi.getSpecNameZh() != null ? oi.getSpecNameZh() : oi.getSpecNameEn());
            idto.setSpecNameZh(oi.getSpecNameZh());
            idto.setSpecNameEn(oi.getSpecNameEn());
            idto.setPrice(oi.getPrice());
            idto.setQuantity(oi.getQuantity());
            idto.setSubtotal(oi.getSubtotal());
            return idto;
        }).collect(Collectors.toList());
        dto.setItems(itemDtos);
        
        return dto;
    }

    // ==================== 获取商家信息（公开接口）====================

    public Map<String, Object> getMerchantInfo(Long merchantId) {
        Merchant merchant = merchantMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getId, merchantId)
                        .eq(Merchant::getStatus, 1) // 必须是已审核通过的商家
                        .eq(Merchant::getDeleted, 0)
        );
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在或未审核通过");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", merchant.getId());
        result.put("shopName", merchant.getShopNameZh() != null ? merchant.getShopNameZh() : merchant.getShopNameEn());
        result.put("shopNameZh", merchant.getShopNameZh());
        result.put("shopNameEn", merchant.getShopNameEn());
        result.put("logo", merchant.getLogo());
        result.put("description", merchant.getDescriptionZh() != null ? merchant.getDescriptionZh() : merchant.getDescriptionEn());
        result.put("descriptionZh", merchant.getDescriptionZh());
        result.put("descriptionEn", merchant.getDescriptionEn());
        result.put("contactName", merchant.getContactName());
        result.put("contactPhone", merchant.getContactPhone());
        result.put("email", merchant.getEmail());
        result.put("businessHours", merchant.getBusinessHours());
        result.put("serviceWechat", merchant.getServiceWechat());
        result.put("servicePhone", merchant.getServicePhone());
        return result;
    }
}


