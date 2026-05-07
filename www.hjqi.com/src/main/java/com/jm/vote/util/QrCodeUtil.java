package com.jm.vote.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class QrCodeUtil {

    private static final int DEFAULT_SIZE = 300;
    private static final String FORMAT = "PNG";

    /**
     * 生成二维码并返回Base64编码的图片
     */
    public String generateQrCodeBase64(String content) {
        return generateQrCodeBase64(content, DEFAULT_SIZE);
    }

    /**
     * 生成指定大小的二维码并返回Base64编码的图片
     */
    public String generateQrCodeBase64(String content, int size) {
        try {
            BufferedImage image = generateQrCodeImage(content, size);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, FORMAT, outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("生成二维码失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成二维码失败", e);
        }
    }

    /**
     * 生成二维码图片
     */
    public BufferedImage generateQrCodeImage(String content, int size) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, size, size);
        graphics.setColor(Color.BLACK);

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (bitMatrix.get(x, y)) {
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        }

        graphics.dispose();
        return image;
    }
}

