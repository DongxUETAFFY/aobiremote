import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    appName: 'Aobi Helper Web',
    version: '0.1.0',
  }),
})
