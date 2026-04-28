export function formatSalary(value) {
  const amount = Number(value || 0)
  return `¥${amount.toLocaleString('zh-CN')}/月`
}

export function formatSignedMoney(value) {
  const amount = Number(value || 0)
  const prefix = amount > 0 ? '+' : ''
  return `${prefix}${amount.toLocaleString('zh-CN')}`
}

export function formatDateTime(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return String(value).replace('T', ' ')
  }
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function formatSpendSeconds(value) {
  const seconds = Math.max(0, Number(value || 0))
  const minutes = Math.floor(seconds / 60)
  const rest = seconds % 60
  if (minutes === 0) return `${rest} 秒`
  return `${minutes} 分 ${String(rest).padStart(2, '0')} 秒`
}

export function normalizeArray(value) {
  if (Array.isArray(value)) return value
  if (value == null || value === '') return []
  return [value]
}

export function normalizeOptions(value) {
  if (!Array.isArray(value)) return []
  return value
    .filter(Boolean)
    .map((item, index) => ({
      id: String(item.id || String.fromCharCode(65 + index)).toUpperCase(),
      content: item.content || String(item),
      type: item.type || 'option',
    }))
}

export function scoreLevel(score) {
  const value = Number(score || 0)
  if (value >= 90) return 'excellent'
  if (value >= 75) return 'good'
  if (value >= 60) return 'pass'
  if (value >= 40) return 'risk'
  return 'danger'
}
