import type { ApiResponse } from '@/types/auth'

export type PublicPostChannel = 'xianyu' | 'tieba' | 'other'
export type PublicPostCategory = 'magic' | 'obi'
export type PublicPostDirection = 'buy' | 'sell'

export interface PublicPostListItem {
  id: number
  itemName: string
  price: number
  tradeTime: string
  direction: PublicPostDirection
  channel: PublicPostChannel
  category: PublicPostCategory
  remark: string | null
  imageFileId: string | null
  untrustedCount: number
  untrusted: boolean
  publisherName: string
  publisherAvatar: string
  mine: boolean
  createdAt: string
  updatedAt: string
}

export interface PublicPostPageResponse {
  items: PublicPostListItem[]
  pageNo: number
  pageSize: number
  totalCount: number
  totalPages: number
  hasMore: boolean
}

export interface PublicPostDetail {
  id: number
  sourceInventoryItemId: number | null
  itemName: string
  price: number
  tradeTime: string
  direction: PublicPostDirection
  channel: PublicPostChannel
  category: PublicPostCategory
  remark: string | null
  imageFileId: string | null
  untrustedCount: number
  untrusted: boolean
  publisherName: string
  publisherAvatar: string
  mine: boolean
  createdAt: string
  updatedAt: string
}

export interface PublicPostCreateRequest {
  itemName: string
  price: number
  tradeTime: string
  direction: PublicPostDirection
  channel: PublicPostChannel
  category: PublicPostCategory
  remark?: string
  imageFileId?: string
  requestId?: string
}

export interface PublicPostUpdateRequest {
  itemName: string
  price: number
  tradeTime: string
  direction: PublicPostDirection
  channel: PublicPostChannel
  category: PublicPostCategory
  remark?: string
  imageFileId?: string
  requestId?: string
}

export interface PublicPostPageQuery {
  pageNo?: number
  pageSize?: number
  scope?: 'all' | 'mine'
  direction?: PublicPostDirection
  channel?: PublicPostChannel
  category?: PublicPostCategory
  keyword?: string
  minPrice?: string
  maxPrice?: string
  sortType?: 'tradeTimeDesc' | 'priceDesc'
}

export interface PublicPostToggleUntrustedResponse {
  postId: number
  flagged: boolean
  untrustedCount: number
}

export type PublicPostUpsertApiResponse = ApiResponse<{ itemId: number }>
export type PublicPostPageApiResponse = ApiResponse<PublicPostPageResponse>
export type PublicPostDetailApiResponse = ApiResponse<PublicPostDetail>
export type PublicPostToggleUntrustedApiResponse = ApiResponse<PublicPostToggleUntrustedResponse>
