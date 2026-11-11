import {Component, inject, signal} from '@angular/core';
import {
  email,
  form,
  FormField,
  FormRoot,
  pattern,
  required,
  validate,
} from '@angular/forms/signals';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {Router, RouterLink} from '@angular/router';
import {firstValueFrom} from 'rxjs';
import {errorMessage} from '../../../core/http/problem';
import {Notify} from '../../../core/notify/notify';
import {AuthApi} from '../auth-api';

@Component({
  selector: 'app-register',
  imports: [
    FormField,
    FormRoot,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './register.html',
  styleUrl: '../auth-form.scss',
})
export default class Register {
  protected readonly error = signal('');
  protected readonly account = signal({username: '', email: '', password: '', confirmPassword: ''});
  // same rules as the server's SignUpRequest
  protected readonly form = form(
    this.account,
    (path) => {
      required(path.username, {message: 'Username is required'});
      pattern(path.username, /^[a-z][a-z0-9]{2,11}$/, {
        message: '3–12 lowercase letters or digits, starting with a letter',
      });
      required(path.email, {message: 'Email is required'});
      email(path.email, {message: 'A valid email is required'});
      required(path.password, {message: 'Password is required'});
      pattern(path.password, /^[A-Za-z0-9]{3,16}$/, {message: '3–16 letters or digits'});
      validate(path.confirmPassword, ({value, valueOf}) =>
        value() === valueOf(path.password)
          ? undefined
          : {kind: 'mismatch', message: 'Passwords do not match'},
      );
    },
    {submission: {action: () => this.signUp()}},
  );
  private readonly authApi = inject(AuthApi);
  private readonly router = inject(Router);
  private readonly notify = inject(Notify);

  private async signUp(): Promise<undefined> {
    const {username, email, password} = this.account();
    try {
      await firstValueFrom(this.authApi.signUp({username, email, password}));
      this.notify.ok('Account created, please log in.');
      await this.router.navigate(['/auth/signin']);
    } catch (error) {
      this.error.set(errorMessage(error));
    }
    return undefined;
  }
}
