import {HttpErrorResponse} from '@angular/common/http';
import {Component, inject, signal} from '@angular/core';
import {form, FormField, FormRoot, required} from '@angular/forms/signals';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {Router, RouterLink} from '@angular/router';
import {firstValueFrom} from 'rxjs';
import {errorMessage} from '../../../core/http/problem';
import {Session} from '../../../core/session/session';
import {AuthApi, Credentials} from '../auth-api';

@Component({
  selector: 'app-login',
  imports: [
    FormField,
    FormRoot,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './login.html',
  styleUrl: '../auth-form.scss',
})
export default class Login {
  protected readonly error = signal('');
  protected readonly credentials = signal<Credentials>({username: '', password: ''});
  protected readonly form = form(
    this.credentials,
    (path) => {
      required(path.username, {message: 'Username is required'});
      required(path.password, {message: 'Password is required'});
    },
    {submission: {action: () => this.signIn()}},
  );
  private readonly authApi = inject(AuthApi);
  private readonly session = inject(Session);
  private readonly router = inject(Router);

  private async signIn(): Promise<undefined> {
    try {
      this.session.signIn(await firstValueFrom(this.authApi.signIn(this.credentials())));
      await this.router.navigate(['/home']);
    } catch (error) {
      const unauthorized = error instanceof HttpErrorResponse && error.status === 401;
      this.error.set(unauthorized ? 'Wrong username or password.' : errorMessage(error));
    }
    return undefined;
  }
}
