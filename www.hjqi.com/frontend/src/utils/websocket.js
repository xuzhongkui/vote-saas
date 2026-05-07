/**
 * WebSocket工具类
 * 用于实时通讯、订单状态推送、系统通知等
 */

class WebSocketClient {
  constructor(url, options = {}) {
    this.url = url
    this.options = options
    this.ws = null
    this.reconnectTimer = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = options.maxReconnectAttempts || 5
    this.reconnectInterval = options.reconnectInterval || 3000
    this.listeners = new Map()
    this.isConnected = false
    this.token = localStorage.getItem('token')
  }

  /**
   * 连接WebSocket
   */
  connect() {
    try {
      // 构建WebSocket URL，包含token
      const wsUrl = `${this.url}?token=${this.token || ''}`
      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = () => {
        console.log('WebSocket连接成功')
        this.isConnected = true
        this.reconnectAttempts = 0
        this.emit('open')
      }

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          this.emit('message', data)
          
          // 根据消息类型分发
          if (data.type) {
            this.emit(data.type, data)
          }
        } catch (error) {
          console.error('解析WebSocket消息失败:', error)
        }
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error)
        this.emit('error', error)
      }

      this.ws.onclose = () => {
        console.log('WebSocket连接关闭')
        this.isConnected = false
        this.emit('close')
        this.reconnect()
      }
    } catch (error) {
      console.error('WebSocket连接失败:', error)
      this.reconnect()
    }
  }

  /**
   * 重连
   */
  reconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('WebSocket重连次数已达上限')
      return
    }

    this.reconnectAttempts++
    console.log(`WebSocket重连中... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)

    this.reconnectTimer = setTimeout(() => {
      this.connect()
    }, this.reconnectInterval)
  }

  /**
   * 发送消息
   */
  send(data) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data))
      return true
    } else {
      console.warn('WebSocket未连接，无法发送消息')
      return false
    }
  }

  /**
   * 订阅事件
   */
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
  }

  /**
   * 取消订阅
   */
  off(event, callback) {
    if (this.listeners.has(event)) {
      const callbacks = this.listeners.get(event)
      const index = callbacks.indexOf(callback)
      if (index > -1) {
        callbacks.splice(index, 1)
      }
    }
  }

  /**
   * 触发事件
   */
  emit(event, data) {
    if (this.listeners.has(event)) {
      this.listeners.get(event).forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.error(`事件回调执行失败 [${event}]:`, error)
        }
      })
    }
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    if (this.ws) {
      this.ws.close()
      this.ws = null
    }

    this.isConnected = false
    this.listeners.clear()
  }
}

// 创建全局WebSocket实例
let wsClient = null

/**
 * 初始化WebSocket连接
 */
export function initWebSocket(userType, userId) {
  if (wsClient) {
    wsClient.disconnect()
  }

  // 根据用户类型和ID确定订阅主题
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  const baseUrl = `${protocol}//${host}/ws`
  
  wsClient = new WebSocketClient(baseUrl, {
    maxReconnectAttempts: 10,
    reconnectInterval: 3000
  })

  // 连接成功后订阅主题
  wsClient.on('open', () => {
    console.log('WebSocket已连接，订阅主题:', `/topic/${userType}/${userId}`)
  })

  wsClient.connect()
  return wsClient
}

/**
 * 获取WebSocket客户端实例
 */
export function getWebSocketClient() {
  return wsClient
}

/**
 * 断开WebSocket连接
 */
export function disconnectWebSocket() {
  if (wsClient) {
    wsClient.disconnect()
    wsClient = null
  }
}

export default WebSocketClient

