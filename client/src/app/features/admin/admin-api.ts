import {HttpClient, httpResource} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {AdminUser} from './admin-user';

@Injectable({providedIn: 'root'})
export class AdminApi {
  private readonly http = inject(HttpClient);

  users() {
    return httpResource<AdminUser[]>(() => '/api/admin/users', {defaultValue: []});
  }

  roles() {
    return httpResource<string[]>(() => '/api/admin/roles', {defaultValue: []});
  }

  changeRole(userId: number, role: string) {
    return this.http.patch<AdminUser>(`/api/admin/users/${userId}/role`, {role});
  }
}
