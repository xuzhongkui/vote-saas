package com.jm.vote.service;

import com.jm.vote.entity.Merchant;
import com.jm.vote.entity.Product;
import com.jm.vote.repository.MerchantMapper;
import com.jm.vote.repository.ProductMapper;
import com.jm.vote.util.QrCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final QrCodeUtil qrCodeUtil;
    private final MerchantMapper merchantMapper;
    private final ProductMapper productMapper;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${file.upload.path:upload}")
    private String uploadPath;

    /**
     * 生成商家邀请码二维码（Base64格式）
     */
    public String generateMerchantInviteCodeQr(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null || merchant.getInviteCode() == null) {
            throw new RuntimeException("商家不存在或邀请码未生成");
        }
        
        // 用户注册页面，带邀请码参数
        String content = baseUrl + "/user/register?inviteCode=" + merchant.getInviteCode();
        return qrCodeUtil.generateQrCodeBase64(content);
    }

    /**
     * 生成并保存商家邀请码二维码到文件
     */
    @Transactional
    public String generateAndSaveMerchantInviteQrCode(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null || merchant.getInviteCode() == null) {
            throw new RuntimeException("商家不存在或邀请码未生成");
        }
        
        try {
            // 生成二维码内容
            String content = baseUrl + "/user/register?inviteCode=" + merchant.getInviteCode();
            
            // 生成二维码图片
            BufferedImage qrImage = qrCodeUtil.generateQrCodeImage(content, 300);
            
            // 创建保存目录
            Path qrCodeDir = Paths.get(uploadPath, "qrcode", "merchant");
            Files.createDirectories(qrCodeDir);
            
            // 保存文件
            String fileName = "invite_" + merchant.getId() + "_" + System.currentTimeMillis() + ".png";
            File qrFile = qrCodeDir.resolve(fileName).toFile();
            ImageIO.write(qrImage, "PNG", qrFile);
            
            // 生成访问URL
            String qrCodeUrl = "/upload/qrcode/merchant/" + fileName;
            
            // 更新商家二维码URL
            merchant.setQrCodeUrl(qrCodeUrl);
            merchant.setUpdatedAt(LocalDateTime.now());
            merchantMapper.updateById(merchant);
            
            log.info("商家邀请二维码生成成功: merchantId={}, url={}", merchantId, qrCodeUrl);
            
            return qrCodeUrl;
        } catch (IOException e) {
            log.error("保存二维码文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存二维码文件失败", e);
        } catch (Exception e) {
            log.error("生成二维码失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成二维码失败", e);
        }
    }

    /**
     * 生成商家推广二维码（用于邀请其他商家）
     */
    public String generateMerchantPromotionQr(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null || merchant.getPromotionCode() == null) {
            throw new RuntimeException("商家不存在或推广码未生成");
        }
        
        // 商家注册页面，带推广码参数
        String content = baseUrl + "/merchant/register?referrer=" + merchant.getPromotionCode();
        return qrCodeUtil.generateQrCodeBase64(content);
    }

    /**
     * 生成商品分享二维码
     */
    public String generateProductShareQr(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        String content = baseUrl + "/product/" + productId;
        return qrCodeUtil.generateQrCodeBase64(content);
    }

    /**
     * 生成店铺二维码
     */
    public String generateShopQr(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new RuntimeException("商家不存在");
        }
        
        String content = baseUrl + "/shop/" + merchantId;
        return qrCodeUtil.generateQrCodeBase64(content);
    }
}

