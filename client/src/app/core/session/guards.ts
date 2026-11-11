import {inject} from '@angular/core';
import {CanMatchFn, Router} from '@angular/router';
import {Session} from './session';

export const authGuard: CanMatchFn = () =>
  inject(Session).loggedIn() || inject(Router).createUrlTree(['/auth/signin']);

export const anonymousGuard: CanMatchFn = () =>
  !inject(Session).loggedIn() || inject(Router).createUrlTree(['/home']);

export const adminGuard: CanMatchFn = () =>
  inject(Session).isAdmin() || inject(Router).createUrlTree(['/home']);
