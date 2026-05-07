import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const cartItems = ref([])

  const cartCount = computed(() => {
    return cartItems.value.reduce((total, item) => total + item.quantity, 0)
  })

  const selectedItems = computed(() => {
    return cartItems.value.filter(item => item.selected)
  })

  const totalAmount = computed(() => {
    return selectedItems.value.reduce((total, item) => {
      const price = item.skuPrice || item.productPrice
      return total + price * item.quantity
    }, 0)
  })

  function setCartItems(items) {
    cartItems.value = items
  }

  function addCartItem(item) {
    const existItem = cartItems.value.find(i => i.id === item.id)
    if (existItem) {
      existItem.quantity += item.quantity
    } else {
      cartItems.value.push(item)
    }
  }

  function updateCartItem(id, updates) {
    const item = cartItems.value.find(i => i.id === id)
    if (item) {
      Object.assign(item, updates)
    }
  }

  function removeCartItem(id) {
    const index = cartItems.value.findIndex(i => i.id === id)
    if (index > -1) {
      cartItems.value.splice(index, 1)
    }
  }

  function clearCart() {
    cartItems.value = []
  }

  return {
    cartItems,
    cartCount,
    selectedItems,
    totalAmount,
    setCartItems,
    addCartItem,
    updateCartItem,
    removeCartItem,
    clearCart
  }
})

