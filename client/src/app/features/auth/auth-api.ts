import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {AuthResponse} from '../../core/session/session';

export interface Credentials {
  username: string;
  password: string;
}

export interface Registration extends Credentials {
  email: string;
}

@Injectable({providedIn: 'root'})
export class AuthApi {
  private readonly http = inject(HttpClient);

  signIn(credentials: Credentials) {
    return this.http.post<AuthResponse>('/api/auth/signin', credentials);
  }

  signUp(registration: Registration) {
    return this.http.post<void>('/api/auth/signup', registration);
  }
}
