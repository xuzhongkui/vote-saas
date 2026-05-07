package com.jm.vote.service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.jm.vote.entity.Merchant;
import com.jm.vote.entity.MerchantInvoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PDF生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfService {

    @Value("${file.upload.path:upload}")
    private String uploadPath;

    /**
     * 生成发票PDF并保存到文件
     */
    public String generateInvoicePdf(MerchantInvoice invoice, Merchant merchant) {
        try {
            // 生成HTML内容
            String htmlContent = generateInvoiceHtml(invoice, merchant);
            
            // 转换为PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(htmlContent, outputStream);
            byte[] pdfBytes = outputStream.toByteArray();
            
            // 创建保存目录
            Path invoiceDir = Paths.get(uploadPath, "invoices");
            Files.createDirectories(invoiceDir);
            
            // 保存文件
            String fileName = "invoice_" + invoice.getInvoiceNo() + ".pdf";
            Path filePath = invoiceDir.resolve(fileName);
            Files.write(filePath, pdfBytes);
            
            // 生成访问URL
            String pdfUrl = "/upload/invoices/" + fileName;
            
            log.info("发票PDF生成成功: invoiceNo={}, path={}", invoice.getInvoiceNo(), pdfUrl);
            
            return pdfUrl;
        } catch (Exception e) {
            log.error("生成发票PDF失败: invoiceNo={}, error={}", invoice.getInvoiceNo(), e.getMessage(), e);
            throw new RuntimeException("生成发票PDF失败", e);
        }
    }

    /**
     * 生成发票HTML内容 - 简化版本，兼容iText
     */
    private String generateInvoiceHtml(MerchantInvoice invoice, Merchant merchant) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'/>");
        html.append("<style>");
        
        // 使用iText完全支持的CSS
        html.append("body { font-family: SimSun; font-size: 12pt; margin: 20px; }");
        html.append("h1 { text-align: center; font-size: 24pt; margin: 20px 0; }");
        html.append("h2 { font-size: 14pt; margin: 15px 0 10px 0; border-bottom: 2px solid #000; padding-bottom: 5px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 15px 0; }");
        html.append("th, td { border: 1px solid #000; padding: 8px; }");
        html.append("th { background-color: #e0e0e0; font-weight: bold; text-align: center; }");
        html.append(".text-center { text-align: center; }");
        html.append(".text-right { text-align: right; }");
        html.append(".info-table td { border: none; padding: 5px 10px; }");
        html.append(".info-label { font-weight: bold; width: 150px; }");
        html.append(".total-box { border: 2px solid #000; padding: 15px; margin: 20px 0; }");
        html.append(".total-row { margin: 8px 0; font-size: 14pt; }");
        html.append(".grand-total { font-size: 16pt; font-weight: bold; margin-top: 15px; padding-top: 15px; border-top: 2px solid #000; }");
        html.append(".footer { margin-top: 30px; padding-top: 15px; border-top: 1px solid #000; text-align: center; font-size: 10pt; color: #666; }");
        html.append(".header-line { border-bottom: 3px solid #000; padding-bottom: 15px; margin-bottom: 20px; }");
        html.append(".invoice-no { text-align: center; font-size: 14pt; font-weight: bold; margin: 10px 0; }");
        
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        // 头部
        html.append("<div class='header-line'>");
        html.append("<h1>增值税发票</h1>");
        html.append("<div class='text-center'>Value Added Tax Invoice</div>");
        html.append("<div class='invoice-no'>发票编号：").append(invoice.getInvoiceNo()).append("</div>");
        html.append("</div>");
        
        // 发票信息
        html.append("<h2>发票信息</h2>");
        html.append("<table class='info-table'>");
        html.append("<tr><td class='info-label'>开票日期：</td><td>").append(invoice.getInvoiceDate().format(dateFormatter)).append("</td></tr>");
        html.append("<tr><td class='info-label'>发票类型：</td><td>").append(getInvoiceTypeName(invoice.getInvoiceType())).append("</td></tr>");
        html.append("<tr><td class='info-label'>发票状态：</td><td>").append(getStatusName(invoice.getStatus())).append("</td></tr>");
        html.append("</table>");
        
        // 购买方信息
        html.append("<h2>购买方信息</h2>");
        html.append("<table class='info-table'>");
        html.append("<tr><td class='info-label'>名称：</td><td>").append(merchant.getShopNameZh() != null ? merchant.getShopNameZh() : merchant.getUsername()).append("</td></tr>");
        if (invoice.getInvoiceTitle() != null && !invoice.getInvoiceTitle().isEmpty()) {
            html.append("<tr><td class='info-label'>发票抬头：</td><td>").append(invoice.getInvoiceTitle()).append("</td></tr>");
        }
        if (invoice.getTaxNumber() != null && !invoice.getTaxNumber().isEmpty()) {
            html.append("<tr><td class='info-label'>纳税人识别号：</td><td>").append(invoice.getTaxNumber()).append("</td></tr>");
        }
        if (merchant.getContactName() != null && !merchant.getContactName().isEmpty()) {
            html.append("<tr><td class='info-label'>联系人：</td><td>").append(merchant.getContactName()).append("</td></tr>");
        }
        if (merchant.getContactPhone() != null && !merchant.getContactPhone().isEmpty()) {
            html.append("<tr><td class='info-label'>联系电话：</td><td>").append(merchant.getContactPhone()).append("</td></tr>");
        }
        if (merchant.getEmail() != null && !merchant.getEmail().isEmpty()) {
            html.append("<tr><td class='info-label'>电子邮箱：</td><td>").append(merchant.getEmail()).append("</td></tr>");
        }
        html.append("</table>");
        
        // 商品明细
        html.append("<h2>商品明细</h2>");
        html.append("<table>");
        html.append("<thead>");
        html.append("<tr>");
        html.append("<th style='width: 40%;'>项目名称</th>");
        html.append("<th style='width: 15%;'>金额（元）</th>");
        html.append("<th style='width: 15%;'>税率</th>");
        html.append("<th style='width: 15%;'>税额（元）</th>");
        html.append("<th style='width: 15%;'>合计（元）</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");
        html.append("<tr>");
        html.append("<td>").append(getInvoiceTypeName(invoice.getInvoiceType())).append("</td>");
        html.append("<td class='text-right'>").append(formatAmount(invoice.getAmount())).append("</td>");
        
        // 计算税率
        BigDecimal taxRate = BigDecimal.ZERO;
        if (invoice.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            taxRate = invoice.getTaxAmount().divide(invoice.getAmount(), 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        html.append("<td class='text-center'>").append(formatAmount(taxRate)).append("%</td>");
        html.append("<td class='text-right'>").append(formatAmount(invoice.getTaxAmount())).append("</td>");
        html.append("<td class='text-right'><strong>").append(formatAmount(invoice.getTotalAmount())).append("</strong></td>");
        html.append("</tr>");
        html.append("</tbody>");
        html.append("</table>");
        
        // 合计
        html.append("<div class='total-box'>");
        html.append("<div class='total-row'>金额小计：¥ ").append(formatAmount(invoice.getAmount())).append("</div>");
        html.append("<div class='total-row'>税额小计：¥ ").append(formatAmount(invoice.getTaxAmount())).append("</div>");
        html.append("<div class='grand-total'>价税合计：¥ ").append(formatAmount(invoice.getTotalAmount())).append("</div>");
        html.append("</div>");
        
        // 备注
        if (invoice.getRemark() != null && !invoice.getRemark().isEmpty()) {
            html.append("<h2>备注信息</h2>");
            html.append("<p>").append(invoice.getRemark()).append("</p>");
        }
        
        // 页脚
        html.append("<div class='footer'>");
        html.append("<p><strong>本发票由系统自动生成，具有法律效力</strong></p>");
        html.append("<p>如有疑问，请联系客服</p>");
        html.append("<p>打印时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"))).append("</p>");
        html.append("</div>");
        
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
    
    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "待生成";
            case 1:
                return "已生成";
            case 2:
                return "已发送";
            case 3:
                return "已作废";
            default:
                return "未知";
        }
    }

    /**
     * 获取发票类型名称
     */
    private String getInvoiceTypeName(String type) {
        if (type == null) {
            return "其他费用";
        }
        switch (type) {
            case "REGISTRATION":
                return "商家入驻费用";
            case "SUBSCRIPTION":
                return "订阅费用";
            case "SERVICE_FEE":
                return "服务费";
            case "PLATFORM_FEE":
                return "平台费";
            case "OTHER":
                return "其他费用";
            default:
                return type;
        }
    }

    /**
     * 格式化金额
     */
    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return String.format("%.2f", amount);
    }
}
