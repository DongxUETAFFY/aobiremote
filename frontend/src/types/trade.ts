import type { ApiResponse } from '@/types/auth'

export type TradeStatus = 'unsold' | 'sold'
export type TradeChannel = 'xianyu' | 'tieba' | 'other'
export type TradeCategory = 'magic' | 'obi'
export type SortType = 'sellTimeDesc' | 'profitDesc'

export interface TradeListItem {
  id: number
  itemName: string
  buyPrice: number
  sellPrice: number
  profitAmount: number
  buyTime: string
  sellTime: string
  channel: TradeChannel
  category: TradeCategory
  remark: string | null
  imageFileId: string | null
  publicPosted: boolean
  createdAt: string
  updatedAt: string
}

export interface TradeSummary {
  totalBuyAmount: string
  totalSellAmount: string
  totalProfit: string
  totalLoss: string
}

export interface TradePageResponse {
  items: TradeListItem[]
  pageNo: number
  pageSize: number
  totalCount: number
  totalPages: number
  hasMore: boolean
  summary: TradeSummary
}

export interface TradeDetail {
  id: number
  itemName: string
  buyPrice: number
  sellPrice: number
  profitAmount: number
  buyTime: string
  sellTime: string
  tradeTime: string
  channel: TradeChannel
  category: TradeCategory
  remark: string | null
  imageFileId: string | null
  publicPosted: boolean
  publicPostId: number | null
  publicPostedAt: string | null
  createdAt: string
  updatedAt: string
}

export interface TradeUpsertRequest {
  itemName: string
  buyPrice: number
  buyTime: string
  sellPrice: number
  sellTime: string
  channel: TradeChannel
  category: TradeCategory
  remark?: string
  imageFileId?: string
  requestId?: string
}

export interface TradeTogglePublicRequest {
  price: number
  tradeTime: string
  direction: 'buy' | 'sell'
  remark?: string
  imageFileId?: string
  requestId?: string
}

export interface TradePageQuery {
  pageNo?: number
  pageSize?: number
  scope?: 'all' | 'profit' | 'loss'
  sortType?: SortType
}

export type TradeUpsertApiResponse = ApiResponse<{ itemId: number }>
export type TradeTogglePublicApiResponse = ApiResponse<{ publicPosted: boolean; postId: number }>
export type TradePageApiResponse = ApiResponse<TradePageResponse>
export type TradeDetailApiResponse = ApiResponse<TradeDetail>
