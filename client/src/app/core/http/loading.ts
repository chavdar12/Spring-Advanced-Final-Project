import {computed, Injectable, signal} from '@angular/core';

/** Number of HTTP calls in flight, for the global progress bar. */
@Injectable({providedIn: 'root'})
export class Loading {
  private readonly pending = signal(0);

  readonly active = computed(() => this.pending() > 0);

  start(): void {
    this.pending.update((n) => n + 1);
  }

  stop(): void {
    this.pending.update((n) => n - 1);
  }
}
