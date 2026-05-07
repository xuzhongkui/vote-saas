# Implementation Plan: i18n Completion

## Overview

This implementation plan systematically adds internationalization support across the entire platform. The approach is incremental, starting with the core language switcher component, then progressively updating layouts and pages, and finally ensuring all notifications and validations use i18n.

## Tasks

- [x] 1. Create LanguageSwitcher component
  - Create `frontend/src/components/LanguageSwitcher.vue` with dropdown UI
  - Implement language selection handler using locale store
  - Add loading state during language switch
  - Add success/error message notifications
  - Style component to match platform design
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [x] 2. Integrate LanguageSwitcher into layouts
  - [x] 2.1 Add LanguageSwitcher to UserLayout
    - Import and place component in top navigation bar
    - Pass `userType="user"` prop
    - Position in top-right corner before user avatar
    - _Requirements: 1.1, 2.4_

  - [x] 2.2 Add LanguageSwitcher to MerchantLayout
    - Import and place component in top navigation bar
    - Pass `userType="merchant"` prop
    - Position in top-right corner before merchant info
    - _Requirements: 1.1_

  - [x] 2.3 Add LanguageSwitcher to AdminLayout
    - Import and place component in top navigation bar
    - Pass `userType="admin"` prop
    - Position in top-right corner before admin info
    - _Requirements: 1.1_

- [x] 3. Update authentication pages with i18n
  - [x] 3.1 Update Login page
    - Replace all hardcoded text with $t() calls
    - Update form labels, placeholders, buttons
    - Update validation messages
    - _Requirements: 9.1, 9.2_

  - [x] 3.2 Update Register page
    - Replace all hardcoded text with $t() calls
    - Update form labels, placeholders, buttons
    - Update validation messages
    - _Requirements: 9.3, 9.4_

  - [x] 3.3 Update ForgotPassword page
    - Replace all hardcoded text with $t() calls
    - Update form elements
    - _Requirements: 9.5_

  - [x] 3.4 Update MerchantRegister page
    - Replace all hardcoded text with $t() calls
    - Update form labels and business license upload text
    - _Requirements: 9.3, 9.4_

- [ ] 4. Update user-facing pages with i18n
  - [x] 4.1 Update Products page
    - Replace search placeholder, category selector with $t()
    - Update "No products" empty state message
    - Update pagination text
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

  - [x] 4.2 Update Cart page
    - Replace table headers with $t()
    - Update button labels (checkout, remove, clear)
    - Update summary labels (subtotal, total)
    - Update empty cart message
    - _Requirements: 7.1, 7.2, 7.3, 7.4_

  - [x] 4.3 Update Orders page
    - Replace table headers with $t()
    - Update order status labels
    - Update button labels and filter options
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

  - [x] 4.4 Update ProductDetail page
    - Replace product attribute labels with $t()
    - Update action buttons (Add to Cart, Buy Now)
    - Update SKU selector labels
    - Update after-sale rules to use locale-aware content (titleZh/titleEn, contentZh/contentEn)
    - _Requirements: 12.8_

  - [x] 4.5 Update Checkout page
    - Replace order summary labels with $t()
    - Update payment buttons and shipping info labels
    - _Requirements: 12.3_

  - [x] 4.6 Update Addresses page
    - Replace form labels with $t()
    - Update table headers and action buttons
    - _Requirements: 12.1_

  - [x] 4.7 Update Profile page
    - Replace form labels with $t()
    - Update section headings
    - _Requirements: 12.9_

  - [x] 4.8 Update MemberCenter page
    - Replace section labels with $t()
    - Update navigation items
    - Update statistics cards and consumption stats
    - Update recent orders table
    - _Requirements: 12.5_

  - [x] 4.9 Update OrderDetail page
    - Replace order information labels with $t()
    - Update status labels and action buttons
    - _Requirements: 12.6_

  - [x] 4.10 Update OrderPayment page
    - Replace payment method labels with $t()
    - Update payment instructions
    - _Requirements: 12.7_

  - [x] 4.11 Update BindMerchant page
    - Replace binding instructions with $t()
    - Update form labels
    - _Requirements: 12.2_

  - [x] 4.12 Update CustomerChat page
    - Replace chat interface labels with $t()
    - Update message input placeholder
    - _Requirements: 12.4_

- [x] 5. Update merchant-facing pages with i18n
  - [x] 5.1 Update merchant Dashboard page
    - Replace statistic labels with $t()
    - Update section headings (Order Status, Sales Statistics)
    - Update table headers for recent orders
    - Update merchant info labels (invite code, promotion code, payment status)
    - _Requirements: 3.1, 3.2, 3.3_

  - [x] 5.2 Update merchant Products page
    - Replace table column headers with $t()
    - Update form labels and placeholders
    - Update button labels (Add Product, Edit, Delete)
    - Update status labels (On Sale, Off Sale)
    - _Requirements: 4.1, 4.2, 4.3, 4.4_

  - [x] 5.3 Update merchant Orders page
    - Replace table column headers with $t()
    - Update filter labels and options
    - Update order status labels
    - Update action buttons (Ship, Cancel)
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

  - [x] 5.4 Update Brands page
    - Replace form labels with $t()
    - Update table headers
    - _Requirements: 11.1_

  - [x] 5.5 Update Categories page
    - Replace category labels with $t()
    - Update action buttons
    - _Requirements: 11.2_

  - [x] 5.6 Update Bills page
    - Replace bill status labels with $t()
    - Update payment labels
    - _Requirements: 11.3_

  - [x] 5.7 Update Invoices page
    - Replace invoice fields with $t()
    - Update download action labels
    - _Requirements: 11.4_

  - [x] 5.8 Update Tickets page
    - Replace ticket status labels with $t()
    - Update reply action labels
    - _Requirements: 11.5_

  - [x] 5.9 Update Config page
    - Replace store settings labels with $t()
    - Update form fields
    - _Requirements: 11.6_

  - [x] 5.10 Update CustomerService page
    - Replace chat interface labels with $t()
    - Update message templates
    - _Requirements: 11.7_

  - [x] 5.11 Update ShippingTemplates page
    - Replace shipping rule labels with $t()
    - Update form fields
    - _Requirements: 11.8_

  - [x] 5.12 Update AfterSaleRules page
    - Replace policy labels with $t()
    - Update form fields
    - _Requirements: 11.9_

  - [x] 5.13 Update NotificationConfigs page
    - Replace notification type labels with $t()
    - Update configuration fields
    - _Requirements: 11.10_

  - [x] 5.14 Update Emails page
    - Replace email template labels with $t()
    - Update form fields
    - _Requirements: 11.11_

  - [x] 5.15 Update MerchantPayment page
    - Replace payment status labels with $t()
    - Update action labels
    - _Requirements: 11.12_

  - [x] 5.16 Update MerchantPromotion page
    - Replace promotion labels with $t()
    - Update statistics labels
    - _Requirements: 11.13_

- [x] 6. Update admin-facing pages with i18n
  - [x] 6.1 Update admin Dashboard page
    - Replace statistics labels with $t()
    - Update section headings
    - _Requirements: 10.1_

  - [x] 6.2 Update Merchants management page
    - Replace table headers with $t()
    - Update filter and action labels
    - _Requirements: 10.2_

  - [x] 6.3 Update Bills management page
    - Replace bill labels with $t()
    - Update status indicators
    - _Requirements: 10.3_

  - [x] 6.4 Update Tickets management page
    - Replace ticket labels with $t()
    - Update status and action labels
    - _Requirements: 10.4_

  - [x] 6.5 Update Contents management page
    - Replace content type labels with $t()
    - Update publishing controls
    - _Requirements: 10.5_

  - [x] 6.6 Update Activities management page
    - Replace activity type labels with $t()
    - Update status labels
    - _Requirements: 10.6_

  - [x] 6.7 Update SystemConfigs page
    - Replace configuration labels with $t()
    - Update descriptions
    - _Requirements: 10.7_

  - [x] 6.8 Update PlatformConfig page
    - Replace settings labels with $t()
    - Update form fields
    - _Requirements: 10.8_

  - [x] 6.9 Update OperationLogs page
    - Replace log type labels with $t()
    - Update action labels
    - _Requirements: 10.9_

  - [x] 6.10 Update RiskControl page
    - Replace rule labels with $t()
    - Update control fields
    - _Requirements: 10.10_

  - [x] 6.11 Update AppDownload page
    - Replace platform labels with $t()
    - Update instructions
    - _Requirements: 10.11_

  - [x] 6.12 Update LanguageSettings page
    - Replace language option labels with $t()
    - Update descriptions
    - _Requirements: 10.12_

  - [x] 6.13 Update MerchantCustomerService page
    - Replace chat interface labels with $t()
    - Update merchant selection labels
    - _Requirements: 10.1_

  - [x] 6.14 Update MerchantSettings page
    - Replace merchant configuration labels with $t()
    - Update form fields
    - _Requirements: 10.1_

  - [x] 6.15 Update Withdrawals page
    - Replace withdrawal status labels with $t()
    - Update action buttons
    - _Requirements: 10.1_

- [ ] 7. Update all notifications and messages with i18n
  - [ ] 7.1 Update success messages
    - Find all `ElMessage.success()` calls
    - Replace hardcoded strings with `t()` calls
    - _Requirements: 13.1_

  - [ ] 7.2 Update error messages
    - Find all `ElMessage.error()` calls
    - Replace hardcoded strings with `t()` calls
    - _Requirements: 13.2_

  - [ ] 7.3 Update warning messages
    - Find all `ElMessage.warning()` calls
    - Replace hardcoded strings with `t()` calls
    - _Requirements: 13.3_

  - [ ] 7.4 Update info messages
    - Find all `ElMessage.info()` calls
    - Replace hardcoded strings with `t()` calls
    - _Requirements: 13.4_

  - [ ] 7.5 Update confirm dialogs
    - Find all `ElMessageBox.confirm()` calls
    - Replace hardcoded strings with `t()` calls
    - _Requirements: 13.5_

  - [ ] 7.6 Update notifications
    - Find all `ElNotification()` calls
    - Replace hardcoded strings with `t()` calls
    - _Requirements: 13.6_

  - [ ] 7.7 Update loading text
    - Find all `v-loading` directives with text
    - Replace hardcoded strings with `t()` calls
    - _Requirements: 13.7_

  - [ ] 7.8 Update empty state descriptions
    - Find all `el-empty` components
    - Replace hardcoded descriptions with `t()` calls
    - _Requirements: 13.8_

- [ ] 8. Update form validation messages with i18n
  - [ ] 8.1 Create validation message helper
    - Create utility function to generate validation rules with i18n
    - Support required, email, phone, minLength, maxLength, etc.
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7_

  - [ ] 8.2 Update all form validation rules
    - Replace hardcoded validation messages with helper function
    - Apply to all forms across user, merchant, and admin pages
    - _Requirements: 14.8_

- [ ] 9. Add missing translation keys
  - [ ] 9.1 Audit zh-CN.js for completeness
    - Review all pages and identify missing keys
    - Add missing keys to zh-CN.js
    - _Requirements: 15.1, 15.3, 15.5_

  - [ ] 9.2 Audit en-US.js for completeness
    - Translate all keys from zh-CN.js to English
    - Ensure key structure matches zh-CN.js
    - _Requirements: 15.2, 15.4, 15.6_

  - [ ] 9.3 Create translation validation script
    - Create `scripts/validate-translations.js`
    - Implement key comparison logic
    - Add to package.json scripts
    - _Requirements: 15.7, 15.8_

- [ ] 10. Checkpoint - Verify all pages use i18n
  - Manually test each page by switching language
  - Verify no hardcoded Chinese/English text remains
  - Verify all notifications display in correct language
  - Verify all validations display in correct language
  - Run translation validation script
  - Ask user if any issues arise

- [ ] 11. Update layout navigation menus with i18n
  - [ ] 11.1 Update UserLayout navigation
    - Replace menu item labels with $t()
    - Update user dropdown menu items
    - _Requirements: 2.1, 2.2_

  - [ ] 11.2 Update MerchantLayout navigation
    - Replace menu item labels with $t()
    - Update merchant dropdown menu items
    - _Requirements: 2.1, 2.2_

  - [ ] 11.3 Update AdminLayout navigation
    - Replace menu item labels with $t()
    - Update admin dropdown menu items
    - _Requirements: 2.1, 2.2_

- [ ] 12. Final checkpoint - Complete testing
  - Test language persistence across page reloads
  - Test language persistence across navigation
  - Test all notification types in both languages
  - Test all form validations in both languages
  - Verify Element Plus components (date picker, pagination) update locale
  - Run translation validation script
  - Ensure all tests pass, ask the user if questions arise

## Notes

- All tasks involve modifying existing Vue components to use i18n
- The `$t()` function is available in templates via Vue I18n
- The `useI18n()` composable provides `t()` function in script setup
- Translation keys follow the pattern: `{section}.{subsection}.{key}`
- Each page update should be tested by switching language to verify reactivity
- The locale store already handles persistence and API calls
- Element Plus locale is already configured in App.vue
- No new backend APIs are needed (locale store already has API integration)
