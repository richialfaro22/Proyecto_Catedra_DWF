import { createRouter, createWebHistory } from 'vue-router'
import { autenticado, auth } from '../stores/auth'
import { toast } from '../stores/toast'

const PERSONAL = ['ADMIN', 'RECEPCIONISTA']
const ADMIN = ['ADMIN']

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue'), meta: { publico: true } },
  { path: '/registro', name: 'registro', component: () => import('../views/RegistroView.vue'), meta: { publico: true } },

  { path: '/', name: 'buscar', component: () => import('../views/BuscarView.vue') },
  { path: '/reservar', name: 'reservar', component: () => import('../views/ReservarView.vue') },
  { path: '/reservas', name: 'reservas', component: () => import('../views/ReservasView.vue') },
  { path: '/reservas/:id', name: 'reserva', component: () => import('../views/ReservaDetalleView.vue'), props: true },
  { path: '/reservas/:id/pago', name: 'pago', component: () => import('../views/PagoView.vue'), props: true },
  { path: '/reservas/:id/modificar', name: 'modificar', component: () => import('../views/ModificarReservaView.vue'), props: true },
  { path: '/reclamos', name: 'reclamos', component: () => import('../views/ReclamosView.vue') },
  { path: '/perfil', name: 'perfil', component: () => import('../views/PerfilView.vue') },

  { path: '/admin/estadisticas', name: 'estadisticas', component: () => import('../views/admin/EstadisticasView.vue'), meta: { roles: PERSONAL } },
  { path: '/admin/hoteles', name: 'hoteles', component: () => import('../views/admin/HotelesView.vue'), meta: { roles: ADMIN } },
  { path: '/admin/tipos', name: 'tipos', component: () => import('../views/admin/TiposView.vue'), meta: { roles: ADMIN } },
  { path: '/admin/habitaciones', name: 'habitaciones', component: () => import('../views/admin/HabitacionesView.vue'), meta: { roles: PERSONAL } },
  { path: '/admin/tarifas', name: 'tarifas', component: () => import('../views/admin/TarifasView.vue'), meta: { roles: ADMIN } },
  { path: '/admin/servicios', name: 'servicios', component: () => import('../views/admin/ServiciosView.vue'), meta: { roles: ADMIN } },
  { path: '/admin/huespedes', name: 'huespedes', component: () => import('../views/admin/HuespedesView.vue'), meta: { roles: PERSONAL } },
  { path: '/admin/pagos', name: 'pagos', component: () => import('../views/admin/PagosView.vue'), meta: { roles: PERSONAL } },
  { path: '/admin/usuarios', name: 'usuarios', component: () => import('../views/admin/UsuariosView.vue'), meta: { roles: ADMIN } },
  { path: '/admin/empleados', name: 'empleados', component: () => import('../views/admin/EmpleadosView.vue'), meta: { roles: ADMIN } },
  { path: '/admin/catalogos', name: 'catalogos', component: () => import('../views/admin/CatalogosView.vue'), meta: { roles: ADMIN } },

  { path: '/:pathMatch(.*)*', name: 'no-encontrado', component: () => import('../views/NoEncontradoView.vue'), meta: { publico: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => {
  if (!to.meta.publico && !autenticado.value) {
    return { name: 'login', query: to.fullPath !== '/' ? { redirect: to.fullPath } : {} }
  }
  if ((to.name === 'login' || to.name === 'registro') && autenticado.value) {
    return { name: 'buscar' }
  }
  if (to.meta.roles && !to.meta.roles.includes(auth.rol)) {
    toast.error('No tienes permiso para acceder a esa sección.')
    return { name: 'buscar' }
  }
})

export default router
