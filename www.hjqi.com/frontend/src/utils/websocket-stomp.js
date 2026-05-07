/**
 * WebSocket STOMP客户端
 * 使用SockJS和STOMP协议连接Spring WebSocket
 */

import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

let stompClient = null
let isConnected = false
let pendingSubscriptions = [] // 待处理的订阅队列
let subscriptionCallbacks = {} // 存储订阅回调
let activeSubscriptions = {} // 已激活的订阅（防止重复订阅）
let currentUserType = null
let currentUserId = null

/**
 * 初始化STOMP客户端
 */
export function initStompClient(userType, userId) {
  if (stompClient && stompClient.connected) {
    console.log('STOMP客户端已连接，跳过初始化')
    return stompClient
  }

  currentUserType = userType
  currentUserId = userId

  const token = localStorage.getItem('token')
  const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:'
  const host = window.location.host
  const socketUrl = `${protocol}//${host}/ws`

  console.log('初始化STOMP客户端:', { userType, userId, socketUrl })

  // 创建SockJS连接
  const socket = new SockJS(socketUrl)
  
  // 创建STOMP客户端
  stompClient = new Client({
    webSocketFactory: () => socket,
    connectHeaders: {
      Authorization: `Bearer ${token}`
    },
    debug: (str) => {
      if (str.includes('ERROR') || str.includes('CONNECTED') || str.includes('SUBSCRIBE')) {
        console.log('STOMP:', str)
      }
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    onConnect: (frame) => {
      console.log('STOMP连接成功:', frame)
      isConnected = true
      
      // 先处理待处理的订阅（这样回调函数会先注册）
      processPendingSubscriptions()
      
      // 订阅用户专属主题（通用通知）
      if (userType && userId) {
        console.log(`订阅主题: /topic/${userType}/${userId}`)
        doSubscribe(`/topic/${userType}/${userId}`)
        // 聊天主题由各组件自行订阅，这里不再重复订阅
      }
    },
    onStompError: (frame) => {
      console.error('STOMP错误:', frame)
      isConnected = false
    },
    onWebSocketClose: () => {
      console.log('WebSocket连接关闭')
      isConnected = false
    },
    onDisconnect: () => {
      console.log('STOMP断开连接')
      isConnected = false
    }
  })

  stompClient.activate()
  return stompClient
}

/**
 * 处理待处理的订阅
 */
function processPendingSubscriptions() {
  while (pendingSubscriptions.length > 0) {
    const { topic, callback } = pendingSubscriptions.shift()
    doSubscribe(topic, callback)
  }
}

/**
 * 实际执行订阅
 */
function doSubscribe(topic, callback) {
  if (!stompClient || !stompClient.connected) {
    console.log('doSubscribe: 客户端未连接，无法订阅', topic)
    return null
  }

  // 存储回调
  if (callback) {
    if (!subscriptionCallbacks[topic]) {
      subscriptionCallbacks[topic] = []
    }
    subscriptionCallbacks[topic].push(callback)
    console.log(`已注册回调函数: ${topic}, 当前回调数: ${subscriptionCallbacks[topic].length}`)
  }

  // 如果已经订阅过该主题，不重复订阅，只添加回调
  if (activeSubscriptions[topic]) {
    console.log(`主题已订阅，跳过重复订阅: ${topic}`)
    return activeSubscriptions[topic]
  }

  console.log(`正在订阅主题: ${topic}`)
  const subscription = stompClient.subscribe(topic, (message) => {
    try {
      const data = JSON.parse(message.body)
      console.log('收到WebSocket消息:', topic, data)
      
      // 调用所有注册的回调
      const callbacks = subscriptionCallbacks[topic] || []
      console.log(`主题 ${topic} 有 ${callbacks.length} 个回调函数`)
      callbacks.forEach((cb, index) => {
        try {
          console.log(`执行回调 ${index + 1}/${callbacks.length}`)
          cb(data)
        } catch (e) {
          console.error('回调执行失败:', e)
        }
      })
    } catch (error) {
      console.error('解析消息失败:', error)
    }
  })
  
  activeSubscriptions[topic] = subscription
  console.log(`订阅成功: ${topic}`)
  return subscription
}

/**
 * 订阅主题（支持延迟订阅）
 */
export function subscribeToTopic(topic, callback) {
  if (!stompClient || !isConnected) {
    // 如果未连接，加入待处理队列
    console.log('STOMP未连接，加入待处理订阅队列:', topic)
    pendingSubscriptions.push({ topic, callback })
    return null
  }

  return doSubscribe(topic, callback)
}

/**
 * 发送消息到指定目的地
 */
export function sendMessage(destination, body, headers = {}) {
  if (!stompClient || !stompClient.connected) {
    console.warn('STOMP客户端未连接，无法发送消息')
    return false
  }

  stompClient.publish({
    destination,
    body: JSON.stringify(body),
    headers
  })
  return true
}

/**
 * 断开连接
 */
export function disconnectStomp() {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
  }
  isConnected = false
  pendingSubscriptions = []
  subscriptionCallbacks = {}
  activeSubscriptions = {}
}

/**
 * 获取STOMP客户端实例
 */
export function getStompClient() {
  return stompClient
}

export default {
  initStompClient,
  subscribeToTopic,
  sendMessage,
  disconnectStomp,
  getStompClient
}

