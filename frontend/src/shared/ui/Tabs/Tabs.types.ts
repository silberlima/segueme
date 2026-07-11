export interface TabItem {
  id: string
  label: string
}

export interface TabsProps {
  tabs: TabItem[]
  activeTabId: string
  onChange: (tabId: string) => void
}
