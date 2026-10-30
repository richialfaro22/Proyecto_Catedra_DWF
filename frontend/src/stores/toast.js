import { reactive } from 'vue'

let siguienteId = 1
export const toasts = reactive([])

function agregar(tipo, mensaje, duracion = 4500) {
  const id = siguienteId++
  toasts.push({ id, tipo, mensaje })
  setTimeout(() => cerrar(id), duracion)
}

export function cerrar(id) {
  const i = toasts.findIndex(t => t.id === id)
  if (i >= 0) toasts.splice(i, 1)
}

export const toast = {
  exito: (m) => agregar('exito', m),
  error: (m) => agregar('error', m, 7000),
  info: (m) => agregar('info', m)
}
