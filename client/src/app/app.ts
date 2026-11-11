import {Component, inject} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatToolbarModule} from '@angular/material/toolbar';
import {Router, RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {Loading} from './core/http/loading';
import {Session} from './core/session/session';
import {ProfileApi} from './features/profile/profile-api';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly session = inject(Session);
  protected readonly loading = inject(Loading);
  protected readonly year = new Date().getFullYear();
  private readonly profileApi = inject(ProfileApi);
  private readonly router = inject(Router);

  protected logout(): void {
    // tell peers we went offline; sign out regardless of the outcome
    this.profileApi.disconnect().subscribe({error: () => undefined});
    this.session.signOut();
    void this.router.navigate(['/auth/signin']);
  }
}
