import {CurrencyPipe} from '@angular/common';
import {Component, computed, inject, input, output, signal} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {RouterLink} from '@angular/router';
import {Notify} from '../../../core/notify/notify';
import {Session} from '../../../core/session/session';
import {AdSummary, PLACEHOLDER_IMAGE} from '../advertisement';
import {AdvertisementApi} from '../advertisement-api';

@Component({
  selector: 'app-ad-card',
  imports: [
    CurrencyPipe,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
  ],
  templateUrl: './ad-card.html',
  styleUrl: './ad-card.scss',
})
export class AdCard {
  readonly ad = input.required<AdSummary>();
  readonly edit = output<AdSummary>();
  readonly remove = output<AdSummary>();
  protected readonly placeholder = PLACEHOLDER_IMAGE;
  private readonly advertisementApi = inject(AdvertisementApi);
  private readonly notify = inject(Notify);
  private readonly session = inject(Session);
  protected readonly mine = computed(() => this.ad().creator === this.session.username());
  private readonly initiallyLiked = computed(() =>
    this.ad().likedBy.includes(this.session.username() ?? ''),
  );
  protected readonly likes = computed(() => {
    const change = this.liked() === this.initiallyLiked() ? 0 : this.liked() ? 1 : -1;
    return this.ad().likedBy.length + change;
  });
  // local override after toggling, so the UI updates without refetching the list
  private readonly likedOverride = signal<boolean | null>(null);
  protected readonly liked = computed(() => this.likedOverride() ?? this.initiallyLiked());

  protected toggleFavorite(): void {
    const liked = this.liked();
    const id = this.ad().id;
    const request = liked ? this.advertisementApi.unlike(id) : this.advertisementApi.like(id);
    request.subscribe({
      next: () => {
        this.likedOverride.set(!liked);
        this.notify.ok(liked ? 'Removed from favorites' : 'Added to favorites');
      },
      error: (error) => this.notify.error(error),
    });
  }
}
