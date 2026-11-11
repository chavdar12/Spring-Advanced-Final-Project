import {HttpClient, httpResource} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {ProfileInfo, UserProfile} from './user-profile';

const BASE = '/api/profile';

/** The signed-in user's own profile. */
@Injectable({providedIn: 'root'})
export class ProfileApi {
  private readonly http = inject(HttpClient);

  get() {
    return httpResource<UserProfile>(() => BASE);
  }

  update(info: ProfileInfo) {
    return this.http.patch<UserProfile>(BASE, info);
  }

  changePassword(oldPassword: string, newPassword: string) {
    return this.http.patch<void>(`${BASE}/password`, {oldPassword, newPassword});
  }

  changePicture(file: File) {
    const body = new FormData();
    body.append('image', file, file.name);
    return this.http.put<UserProfile>(`${BASE}/picture`, body);
  }

  disconnect() {
    return this.http.post<void>(`${BASE}/disconnect`, null);
  }
}
