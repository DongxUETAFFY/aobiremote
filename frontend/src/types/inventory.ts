import type { ApiResponse } from '@/types/auth'

export type InventoryStatus = 'unsold' | 'sold'
export type InventoryChannel = 'xianyu' | 'tieba' | 'other'
export type InventoryCategory = 'magic' | 'obi'
export type SortType = 'buyTimeDesc' | 'buyPriceDesc'

export interface InventoryListItem {
  id: number
  itemName: string
  buyPrice: number
  buyTime: string
  channel: InventoryChannel
  category: InventoryCategory
  remark: string | null
  imageFileId: string | null
  status: InventoryStatus
  publicPosted: boolean
  createdAt: string
  updatedAt: string
}

export interface InventorySummary {
  totalBuyPrice: string
}

export interface InventoryPageResponse {
  items: InventoryListItem[]
  pageNo: number
  pageSize: number
  totalCount: number
  totalPages: number
  hasMore: boolean
  summary: InventorySummary
}

export interface InventoryDetail {
  id: number
  itemName: string
  buyPrice: number
  buyTime: string
  sellPrice: number | null
  sellTime: string | null
  tradeTime: string | null
  channel: InventoryChannel
  category: InventoryCategory
  status: InventoryStatus
  profitAmount: number | null
  publicPosted: boolean
  publicPostId: number | null
  publicPostedAt: string | null
  remark: string | null
  imageFileId: string | null
  createdAt: string
  updatedAt: string
}

export interface InventoryUpsertRequest {
  itemName: string
  buyPrice: number
  buyTime: string
  channel: InventoryChannel
  category: InventoryCategory
  remark?: string
  imageFileId?: string
  requestId?: string
}

export interface InventoryMarkSoldRequest {
  sellPrice: number
  sellTime: string
  requestId?: string
}

export interface InventoryTogglePublicRequest {
  price: number
  tradeTime: string
  direction: 'buy' | 'sell'
  remark?: string
  imageFileId?: string
  requestId?: string
}

export interface InventoryPageQuery {
  pageNo?: number
  pageSize?: number
  keyword?: string
  priceRange?: string
  sortType?: SortType
}

export type InventoryUpsertApiResponse = ApiResponse<{ itemId: number }>
export type InventoryTogglePublicApiResponse = ApiResponse<{ publicPosted: boolean; postId: number }>
export type InventoryPageApiResponse = ApiResponse<InventoryPageResponse>
export type InventoryDetailApiResponse = ApiResponse<InventoryDetail>
