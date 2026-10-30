import { reactive, computed } from 'vue'

const CLAVE = 'hotel.sesion'

function cargar() {
  try {
    const s = JSON.parse(localStorage.getItem(CLAVE) || 'null')
    if (s && s.expira > Date.now()) return s
  } catch { /* sesión corrupta */ }
  return null
}

const inicial = cargar()

export const auth = reactive({
  token: inicial?.token || null,
  alias: inicial?.alias || null,
  rol: inicial?.rol || null,
  expira: inicial?.expira || 0
})

export function iniciarSesion(respuesta) {
  auth.token = respuesta.token
  auth.alias = respuesta.alias
  auth.rol = respuesta.rol
  auth.expira = Date.now() + respuesta.expiraEnSegundos * 1000
  try { localStorage.setItem(CLAVE, JSON.stringify({ ...auth })) } catch { /* sin almacenamiento */ }
}

export function cerrarSesion() {
  auth.token = null
  auth.alias = null
  auth.rol = null
  auth.expira = 0
  try { localStorage.removeItem(CLAVE) } catch { /* sin almacenamiento */ }
}

export const autenticado = computed(() => !!auth.token && auth.expira > Date.now())
export const esAdmin = computed(() => auth.rol === 'ADMIN')
export const esPersonal = computed(() => auth.rol === 'ADMIN' || auth.rol === 'RECEPCIONISTA')
export const esHuesped = computed(() => auth.rol === 'HUESPED')
