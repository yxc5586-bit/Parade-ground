const STORAGE_PREFIX = 'parade-ground:pending-task:'

function buildTaskId() {
  const random = Math.random().toString(36).slice(2, 10)
  return `${Date.now()}-${random}`
}

function storageKey(id) {
  return `${STORAGE_PREFIX}${id}`
}

export function createPendingTask({ type, payload = {}, from = '' }) {
  const task = {
    id: buildTaskId(),
    type,
    payload,
    from,
    createdAt: Date.now(),
  }

  window.sessionStorage.setItem(storageKey(task.id), JSON.stringify(task))
  return task
}

export function consumePendingTask(id) {
  if (!id) return null

  const key = storageKey(id)
  const raw = window.sessionStorage.getItem(key)
  window.sessionStorage.removeItem(key)

  if (!raw) return null

  try {
    return JSON.parse(raw)
  } catch (error) {
    return null
  }
}
