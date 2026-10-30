import { auth, cerrarSesion } from '../stores/auth'
import { toast } from '../stores/toast'
import router from '../router'

const BASE = import.meta.env.VITE_API_URL || '/api'

/** Error devuelto por la API con el mensaje amigable y los errores por campo. */
export class ApiError extends Error {
  constructor(estado, cuerpo) {
    super(cuerpo?.mensaje || 'Ocurrió un error inesperado.')
    this.estado = estado
    this.campos = cuerpo?.campos || {}
  }
}

function construirUrl(ruta, params) {
  const url = new URL(BASE + ruta, window.location.origin)
  if (params) {
    Object.entries(params).forEach(([k, v]) => {
      if (v !== null && v !== undefined && v !== '') url.searchParams.set(k, v)
    })
  }
  return BASE.startsWith('http') ? url.toString() : url.pathname + url.search
}

export async function api(ruta, { metodo = 'GET', cuerpo, params } = {}) {
  const headers = { Accept: 'application/json' }
  if (cuerpo !== undefined) headers['Content-Type'] = 'application/json'
  if (auth.token) headers.Authorization = `Bearer ${auth.token}`

  let respuesta
  try {
    respuesta = await fetch(construirUrl(ruta, params), {
      method: metodo,
      headers,
      body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined
    })
  } catch {
    throw new ApiError(0, {
      mensaje: 'No se pudo conectar con el servidor. Verifica que la API esté en ejecución (puerto 8080).'
    })
  }

  const texto = await respuesta.text()
  let datos = null
  if (texto) {
    try { datos = JSON.parse(texto) } catch { datos = { mensaje: texto } }
  }

  if (respuesta.status === 401 && auth.token && ruta !== '/auth/login') {
    cerrarSesion()
    toast.error('Tu sesión expiró. Inicia sesión de nuevo.')
    router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
  }
  if (!respuesta.ok) throw new ApiError(respuesta.status, datos)
  return datos
}

export const get = (ruta, params) => api(ruta, { params })
export const post = (ruta, cuerpo) => api(ruta, { metodo: 'POST', cuerpo: cuerpo ?? {} })
export const put = (ruta, cuerpo) => api(ruta, { metodo: 'PUT', cuerpo })
export const patch = (ruta) => api(ruta, { metodo: 'PATCH' })
export const del = (ruta) => api(ruta, { metodo: 'DELETE' })

/**
 * Muestra el error al usuario: los errores por campo se copian en `errores`
 * (para mostrarlos bajo cada campo) y el mensaje general en una notificación.
 */
export function mostrarError(e, errores) {
  if (errores) {
    Object.keys(errores).forEach(k => delete errores[k])
    Object.assign(errores, e?.campos || {})
  }
  toast.error(e?.message || 'Ocurrió un error inesperado.')
}
