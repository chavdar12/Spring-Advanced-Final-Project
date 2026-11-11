import {computed, Injectable, signal} from '@angular/core';

/** Body of a successful sign-in. */
export interface AuthResponse {
  token: string;
  username: string;
  roles: string[];
}

const STORAGE_KEY = 'trade-zone.session';

function restore(): AuthResponse | null {
  try {
    return JSON.parse(sessionStorage.getItem(STORAGE_KEY) ?? 'null');
  } catch {
    return null;
  }
}

/** Bearer token and identity for the current tab; sessionStorage so it dies with the tab. */
@Injectable({providedIn: 'root'})
export class Session {
  readonly isAdmin = computed(() => this.roles().includes('ROLE_ADMIN'));
  readonly isModerator = computed(() => this.roles().includes('ROLE_MODERATOR'));
  private readonly state = signal<AuthResponse | null>(restore());
  readonly token = computed(() => this.state()?.token ?? null);
  readonly username = computed(() => this.state()?.username ?? null);
  readonly roles = computed(() => this.state()?.roles ?? []);
  readonly loggedIn = computed(() => this.state() !== null);

  signIn(response: AuthResponse): void {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(response));
    this.state.set(response);
  }

  signOut(): void {
    sessionStorage.removeItem(STORAGE_KEY);
    this.state.set(null);
  }
}
