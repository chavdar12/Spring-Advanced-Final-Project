import {Routes} from '@angular/router';
import {adminGuard, anonymousGuard, authGuard} from './core/session/guards';

export const routes: Routes = [
  {path: '', pathMatch: 'full', redirectTo: 'home'},
  {path: 'home', loadComponent: () => import('./features/home/home')},
  {
    path: 'auth',
    canMatch: [anonymousGuard],
    children: [
      {path: 'signin', loadComponent: () => import('./features/auth/login/login')},
      {path: 'signup', loadComponent: () => import('./features/auth/register/register')},
      {path: '', pathMatch: 'full', redirectTo: 'signup'},
    ],
  },
  {
    path: 'advertisements',
    canMatch: [authGuard],
    children: [
      {
        path: 'category/:category',
        loadComponent: () => import('./features/advertisements/ad-list/ad-list'),
      },
      {
        path: 'details/:id',
        loadComponent: () => import('./features/advertisements/ad-details/ad-details'),
      },
      {path: '', pathMatch: 'full', redirectTo: 'category/All'},
    ],
  },
  {
    path: 'regions',
    canMatch: [authGuard],
    loadComponent: () => import('./features/regions/regions'),
  },
  {
    path: 'user/profile',
    canMatch: [authGuard],
    loadComponent: () => import('./features/profile/profile'),
  },
  {
    path: 'user/admin',
    canMatch: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/admin'),
  },
  {path: '**', redirectTo: 'home'},
];
