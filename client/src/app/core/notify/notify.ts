import {inject, Injectable} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {errorMessage} from '../http/problem';

/** Snack-bar feedback for user actions. */
@Injectable({providedIn: 'root'})
export class Notify {
  private readonly snackBar = inject(MatSnackBar);

  ok(message: string): void {
    this.snackBar.open(message, 'OK', {duration: 3000});
  }

  error(error: unknown): void {
    const message = typeof error === 'string' ? error : errorMessage(error);
    this.snackBar.open(message, 'Dismiss', {duration: 5000});
  }
}
