import { Routes } from '@angular/router';
import { Homecomponent } from './components/homecomponent/homecomponent';
import { adminGuard, authGuard, cartNotEmptyGuard, guestGuard } from './core/guards';

export const routes: Routes = [
  { path: '', component: Homecomponent, title: 'T-Style' },
  {
    path: 'coleccion',
    loadComponent: () => import('./components/catalogcomponent/catalogcomponent').then((m) => m.Catalogcomponent),
    title: 'Colección · T-Style',
  },
  {
    path: 'producto/:id',
    loadComponent: () => import('./components/productcomponent/productcomponent').then((m) => m.Productcomponent),
    title: 'Producto · T-Style',
  },
  {
    path: 'carrito',
    loadComponent: () => import('./components/cartcomponent/cartcomponent').then((m) => m.Cartcomponent),
    title: 'Carrito · T-Style',
  },
  {
    path: 'checkout',
    canActivate: [authGuard, cartNotEmptyGuard],
    loadComponent: () => import('./components/checkoutcomponent/checkoutcomponent').then((m) => m.Checkoutcomponent),
    title: 'Checkout · T-Style',
  },
  {
    path: 'videos',
    loadComponent: () => import('./components/videocomponent/videocomponent').then((m) => m.Videocomponent),
    title: 'T-Style TV',
  },
  {
    path: 'contacto',
    loadComponent: () => import('./components/contactcomponent/contactcomponent').then((m) => m.Contactcomponent),
    title: 'Contacto · T-Style',
  },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./components/logincomponent/logincomponent').then((m) => m.Logincomponent),
    title: 'Iniciar sesión · T-Style',
  },
  {
    path: 'registro',
    canActivate: [guestGuard],
    loadComponent: () => import('./components/registercomponent/registercomponent').then((m) => m.Registercomponent),
    title: 'Crear cuenta · T-Style',
  },
  {
    path: 'verificar',
    loadComponent: () => import('./components/verifycomponent/verifycomponent').then((m) => m.Verifycomponent),
    title: 'Verificar correo · T-Style',
  },
  {
    path: 'recuperar',
    loadComponent: () => import('./components/recovercomponent/recovercomponent').then((m) => m.Recovercomponent),
    title: 'Recuperar contraseña · T-Style',
  },
  {
    path: 'perfil',
    canActivate: [authGuard],
    loadComponent: () => import('./components/profilecomponent/profilecomponent').then((m) => m.Profilecomponent),
    title: 'Mi perfil · T-Style',
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () => import('./components/admincomponent/admincomponent').then((m) => m.Admincomponent),
    title: 'Administración · T-Style',
  },
  {
    path: '**',
    loadComponent: () => import('./components/notfoundcomponent/notfoundcomponent').then((m) => m.Notfoundcomponent),
    title: 'Página no encontrada · T-Style',
  },
];
