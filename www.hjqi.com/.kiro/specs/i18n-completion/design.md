# Design Document: i18n Completion

## Overview

This design document outlines the implementation approach for completing internationalization (i18n) across the entire e-commerce platform. The system already has a robust i18n infrastructure using Vue I18n with Chinese (zh-CN) and English (en-US) language files, a locale store for state management, and Element Plus locale integration.

The implementation will focus on:
1. Creating a reusable Language Switcher component
2. Integrating the language switcher into all layout components
3. Systematically replacing hardcoded text with translation keys across all pages
4. Ensuring all notifications, messages, and validation errors use i18n
5. Validating translation completeness

## Architecture

### Existing Infrastructure

The platform already has:
- **Vue I18n**: Configured with Composition API mode
- **Language Files**: `zh-CN.js` and `en-US.js` with comprehensive translations
- **Locale Store**: Pinia store managing language state and persistence
- **Element Plus Integration**: Dynamic locale switching for UI components
- **localStorage Persistence**: Language preference saved and restored

### Component Hierarchy

```
App.vue (ElConfigProvider with dynamic locale)
├── UserLayout
│   ├── LanguageSwitcher (new)
│   └── User Pages (Products, Cart, Orders, etc.)
├── MerchantLayout
│   ├── LanguageSwitcher (new)
│   └── Merchant Pages (Dashboard, Products, Orders, etc.)
└── AdminLayout
    ├── LanguageSwitcher (new)
    └── Admin Pages (Dashboard, Merchants, Bills, etc.)
```

### State Management Flow

```
User clicks language switcher
    ↓
LanguageSwitcher emits locale change
    ↓
LocaleStore.switchLocale(locale, saveToServer, userType)
    ↓
├── Update localStorage
├── Call i18n setLocale()
├── Update currentLocale ref
└── (Optional) Save to server API
    ↓
Vue I18n reactively updates all $t() calls
    ↓
Element Plus locale updates via computed property
```

## Components and Interfaces

### 1. LanguageSwitcher Component

**Purpose**: Reusable dropdown component for language selection

**Props**:
```typescript
interface Props {
  userType?: 'admin' | 'merchant' | 'user'  // Determines which API to call
  placement?: 'top' | 'bottom' | 'left' | 'right'  // Dropdown placement
  size?: 'large' | 'default' | 'small'  // Button size
}
```

**Template Structure**:
```vue
<el-dropdown @command="handleLanguageChange">
  <el-button :size="size">
    <el-icon><Globe /></el-icon>
    {{ currentLocaleInfo.label }}
  </el-button>
  <template #dropdown>
    <el-dropdown-menu>
      <el-dropdown-item 
        v-for="locale in localeOptions" 
        :key="locale.value"
        :command="locale.value"
        :disabled="locale.value === currentLocale"
      >
        {{ locale.flag }} {{ locale.label }}
      </el-dropdown-item>
    </el-dropdown-menu>
  </template>
</el-dropdown>
```

**Methods**:
- `handleLanguageChange(locale: string)`: Calls locale store to switch language
- Uses `useLocaleStore()` to access state and actions
- Shows loading state during API call
- Displays success/error message after switch

### 2. Translation Key Patterns

**Naming Convention**:
```
{section}.{subsection}.{key}

Examples:
- common.confirm
- menu.dashboard
- product.productName
- order.orderStatus
- validation.required
- error.networkError
```

**Usage in Templates**:
```vue
<!-- Static text -->
<h3>{{ $t('menu.dashboard') }}</h3>

<!-- With interpolation -->
<span>{{ $t('validation.minLength', { min: 6 }) }}</span>

<!-- In attributes -->
<el-input :placeholder="$t('common.pleaseInput')" />

<!-- In Element Plus components -->
<el-button>{{ $t('common.save') }}</el-button>
```

**Usage in Script**:
```javascript
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

// In methods
ElMessage.success(t('common.success'))

// In computed properties
const statusLabel = computed(() => t(`order.${status.value}`))
```

### 3. Layout Integration

**UserLayout**:
- Add LanguageSwitcher in top navigation bar
- Position: Top-right corner, before user avatar
- Pass `userType="user"` prop

**MerchantLayout**:
- Add LanguageSwitcher in top navigation bar
- Position: Top-right corner, before merchant info
- Pass `userType="merchant"` prop

**AdminLayout**:
- Add LanguageSwitcher in top navigation bar
- Position: Top-right corner, before admin info
- Pass `userType="admin"` prop

## Data Models

### Locale Store State

```typescript
interface LocaleState {
  currentLocale: Ref<string>  // 'zh-CN' | 'en-US'
  loading: Ref<boolean>
  currentLocaleInfo: ComputedRef<LocaleOption>
  isChinese: ComputedRef<boolean>
  isEnglish: ComputedRef<boolean>
  localeOptions: LocaleOption[]
}

interface LocaleOption {
  value: string  // 'zh-CN' | 'en-US'
  label: string  // '简体中文' | 'English'
  flag: string   // '🇨🇳' | '🇺🇸'
}
```

### Translation File Structure

```typescript
interface TranslationSchema {
  common: CommonTranslations
  menu: MenuTranslations
  auth: AuthTranslations
  merchant: MerchantTranslations
  product: ProductTranslations
  order: OrderTranslations
  cart: CartTranslations
  bill: BillTranslations
  ticket: TicketTranslations
  content: ContentTranslations
  activity: ActivityTranslations
  system: SystemTranslations
  language: LanguageTranslations
  statistics: StatisticsTranslations
  validation: ValidationTranslations
  error: ErrorTranslations
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Language Persistence Round-Trip
*For any* valid locale value ('zh-CN' or 'en-US'), when a user selects that language, reloads the page, the restored language should equal the selected language.
**Validates: Requirements 1.4, 1.5**

### Property 2: Locale Change Reactivity
*For any* page component and any locale change event, all text elements using $t() should update to display content in the new language within one render cycle.
**Validates: Requirements 2.3, 3.4, 4.5, 5.5, 6.5, 7.5, 8.5, 9.6, 10.13, 11.14, 12.10**

### Property 3: Notification Language Consistency
*For any* notification type (success, error, warning, info) triggered after a locale change, the notification message should display in the currently selected language.
**Validates: Requirements 13.9**

### Property 4: Validation Message Language Consistency
*For any* form validation error triggered after a locale change, the validation message should display in the currently selected language.
**Validates: Requirements 14.9**

### Property 5: Translation Key Fallback
*For any* missing translation key, when accessed via $t(), the system should return the translation key itself as a string (not undefined or error).
**Validates: Requirements 15.7**

### Property 6: Language Options Display
*For any* language switcher instance, when clicked, it should display exactly two language options with correct labels and flags.
**Validates: Requirements 1.2**

### Property 7: Locale Store Synchronization
*For any* language selection action, the locale store's currentLocale, localStorage value, and i18n global locale should all be equal after the action completes.
**Validates: Requirements 1.3, 1.4**

## Error Handling

### Missing Translation Keys

**Strategy**: Graceful degradation with logging

```javascript
// Vue I18n automatically returns the key if translation is missing
// Add custom missing handler for development logging
const i18n = createI18n({
  // ... config
  missing: (locale, key) => {
    if (import.meta.env.DEV) {
      console.warn(`[i18n] Missing translation: ${key} for locale: ${locale}`)
    }
    return key  // Return key as fallback
  }
})
```

### API Failures

**Scenario**: Server API call to save language preference fails

**Handling**:
1. Still update local state and localStorage
2. Show warning message to user
3. Log error for debugging
4. Retry on next language change

```javascript
async function switchLocale(locale, saveToServer, userType) {
  try {
    if (saveToServer) {
      await request.put(getApiPath(userType), { locale })
    }
    // Update local state
    currentLocale.value = locale
    setI18nLocale(locale)
    ElMessage.success(t('language.switchSuccess'))
  } catch (error) {
    // Still update local state even if API fails
    currentLocale.value = locale
    setI18nLocale(locale)
    ElMessage.warning(t('language.switchFailed') + ', ' + t('common.tip'))
    console.error('Save language preference failed:', error)
  }
}
```

### Invalid Locale Values

**Scenario**: Invalid locale value in localStorage or API response

**Handling**:
1. Validate locale against `localeOptions`
2. Fall back to default 'zh-CN'
3. Log warning

```javascript
function validateLocale(locale) {
  const valid = localeOptions.find(opt => opt.value === locale)
  if (!valid) {
    console.warn(`Invalid locale: ${locale}, falling back to zh-CN`)
    return 'zh-CN'
  }
  return locale
}
```

## Testing Strategy

### Unit Tests

**Focus**: Component behavior and store logic

**Test Cases**:
1. LanguageSwitcher renders with correct current language
2. LanguageSwitcher displays all available language options
3. Locale store switchLocale updates state correctly
4. Locale store persists to localStorage
5. Locale store validates invalid locale values
6. Translation key fallback returns key string
7. Missing translation handler logs in development

**Tools**: Vitest, Vue Test Utils

### Property-Based Tests

**Focus**: Universal properties across all inputs

**Test Cases**:
1. **Property 1**: Language persistence round-trip
   - Generate random locale values
   - Save, reload, verify restoration
   
2. **Property 2**: Locale change reactivity
   - Generate random locale changes
   - Verify all $t() calls update

3. **Property 3**: Notification language consistency
   - Generate random notification types
   - Change locale, trigger notification
   - Verify message language matches locale

4. **Property 4**: Validation message language consistency
   - Generate random validation errors
   - Change locale, trigger validation
   - Verify message language matches locale

5. **Property 5**: Translation key fallback
   - Generate random non-existent keys
   - Verify $t() returns key string

6. **Property 6**: Language options display
   - Verify exactly 2 options with correct data

7. **Property 7**: Locale store synchronization
   - Generate random locale changes
   - Verify all three sources match

**Tools**: fast-check (JavaScript property-based testing library)

**Configuration**: Minimum 100 iterations per property test

### Integration Tests

**Focus**: End-to-end language switching flow

**Test Cases**:
1. User switches language in UserLayout
   - Verify navigation menu updates
   - Verify page content updates
   - Verify localStorage updated
   
2. Merchant switches language in MerchantLayout
   - Verify dashboard statistics labels update
   - Verify product table headers update
   
3. Admin switches language in AdminLayout
   - Verify admin menu updates
   - Verify system config labels update

4. Language persists across page navigation
   - Switch language on page A
   - Navigate to page B
   - Verify language remains

5. Notifications display in correct language
   - Switch language
   - Trigger success/error message
   - Verify message language

**Tools**: Playwright or Cypress for E2E testing

### Manual Testing Checklist

**Per Page**:
- [ ] All text uses translation keys (no hardcoded Chinese/English)
- [ ] Language switcher visible and functional
- [ ] Switching language updates all text immediately
- [ ] No console warnings about missing keys
- [ ] Element Plus components (date picker, pagination) update locale

**Cross-Page**:
- [ ] Language preference persists across navigation
- [ ] Language preference persists across page reload
- [ ] Notifications display in correct language
- [ ] Form validations display in correct language

### Translation Completeness Validation

**Automated Script**:
```javascript
// scripts/validate-translations.js
import zhCN from '../frontend/src/i18n/zh-CN.js'
import enUS from '../frontend/src/i18n/en-US.js'

function getAllKeys(obj, prefix = '') {
  let keys = []
  for (const [key, value] of Object.entries(obj)) {
    const fullKey = prefix ? `${prefix}.${key}` : key
    if (typeof value === 'object' && value !== null) {
      keys = keys.concat(getAllKeys(value, fullKey))
    } else {
      keys.push(fullKey)
    }
  }
  return keys
}

const zhKeys = new Set(getAllKeys(zhCN))
const enKeys = new Set(getAllKeys(enUS))

// Find missing keys
const missingInEn = [...zhKeys].filter(k => !enKeys.has(k))
const missingInZh = [...enKeys].filter(k => !zhKeys.has(k))

if (missingInEn.length > 0) {
  console.error('Missing in en-US:', missingInEn)
}
if (missingInZh.length > 0) {
  console.error('Missing in zh-CN:', missingInZh)
}

if (missingInEn.length === 0 && missingInZh.length === 0) {
  console.log('✓ All translation keys are present in both languages')
}
```

Run this script as part of CI/CD pipeline to ensure translation completeness.
