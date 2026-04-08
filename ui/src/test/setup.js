import { vi } from 'vitest'

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn()
}
global.localStorage = localStorageMock

// Mock sessionStorage
const sessionStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn()
}
global.sessionStorage = sessionStorageMock

// Mock window.location
const originalLocation = window.location
delete window.location
window.location = {
  ...originalLocation,
  href: '',
  origin: 'http://localhost:3000',
  pathname: '/',
  search: '',
  hash: ''
}

// Reset mocks before each test
beforeEach(() => {
  localStorageMock.getItem.mockReset()
  localStorageMock.setItem.mockReset()
  localStorageMock.removeItem.mockReset()
  localStorageMock.clear.mockReset()
  sessionStorageMock.getItem.mockReset()
  sessionStorageMock.setItem.mockReset()
  sessionStorageMock.removeItem.mockReset()
  sessionStorageMock.clear.mockReset()
})

// Global test utilities
global.testUtils = {
  localStorageMock,
  sessionStorageMock
}