import http from '@/api/http'
import type {
  TradePageApiResponse,
  TradeDetailApiResponse,
  TradeUpsertApiResponse,
  TradeTogglePublicApiResponse,
  TradeBatchActionRequest,
  TradeUpsertRequest,
  TradeTogglePublicRequest,
  TradePageQuery,
} from '@/types/trade'

export const getTradePage = async (query?: TradePageQuery) => {
  const { data } = await http.get<TradePageApiResponse>('/trade/page', { params: query })
  return data
}

export const getTradeDetail = async (id: number) => {
  const { data } = await http.get<TradeDetailApiResponse>(`/trade/${id}`)
  return data
}

export const createTradeItem = async (payload: TradeUpsertRequest) => {
  const { data } = await http.post<TradeUpsertApiResponse>('/trade', payload)
  return data
}

export const updateTradeItem = async (id: number, payload: TradeUpsertRequest) => {
  const { data } = await http.put<TradeUpsertApiResponse>(`/trade/${id}`, payload)
  return data
}

export const deleteTradeItem = async (id: number, requestId: string) => {
  const { data } = await http.delete<TradeUpsertApiResponse>(`/trade/${id}`, {
    params: { requestId },
  })
  return data
}

export const toggleTradePublic = async (id: number, payload: TradeTogglePublicRequest) => {
  const { data } = await http.post<TradeTogglePublicApiResponse>(`/trade/${id}/toggle-public`, payload)
  return data
}

export const batchDeleteTradeItems = async (payload: TradeBatchActionRequest) => {
  const { data } = await http.post<TradeUpsertApiResponse>('/trade/batch-delete', payload)
  return data
}

export const batchToggleTradePublic = async (payload: TradeBatchActionRequest) => {
  const { data } = await http.post<TradeUpsertApiResponse>('/trade/batch-toggle-public', payload)
  return data
}
