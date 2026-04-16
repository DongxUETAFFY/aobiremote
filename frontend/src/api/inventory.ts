import http from '@/api/http'
import type {
  InventoryPageApiResponse,
  InventoryDetailApiResponse,
  InventoryUpsertApiResponse,
  InventoryTogglePublicApiResponse,
  InventoryUpsertRequest,
  InventoryMarkSoldRequest,
  InventoryTogglePublicRequest,
  InventoryPageQuery,
} from '@/types/inventory'

export const getInventoryPage = async (query?: InventoryPageQuery) => {
  const { data } = await http.get<InventoryPageApiResponse>('/inventory/page', { params: query })
  return data
}

export const getInventoryDetail = async (id: number) => {
  const { data } = await http.get<InventoryDetailApiResponse>(`/inventory/${id}`)
  return data
}

export const createInventoryItem = async (payload: InventoryUpsertRequest) => {
  const { data } = await http.post<InventoryUpsertApiResponse>('/inventory', payload)
  return data
}

export const updateInventoryItem = async (id: number, payload: InventoryUpsertRequest) => {
  const { data } = await http.put<InventoryUpsertApiResponse>(`/inventory/${id}`, payload)
  return data
}

export const markInventorySold = async (id: number, payload: InventoryMarkSoldRequest) => {
  const { data } = await http.post<InventoryUpsertApiResponse>(`/inventory/${id}/mark-sold`, payload)
  return data
}

export const deleteInventoryItem = async (id: number, requestId: string) => {
  const { data } = await http.delete<InventoryUpsertApiResponse>(`/inventory/${id}`, {
    params: { requestId },
  })
  return data
}

export const toggleInventoryPublic = async (id: number, payload: InventoryTogglePublicRequest) => {
  const { data } = await http.post<InventoryTogglePublicApiResponse>(`/inventory/${id}/toggle-public`, payload)
  return data
}
