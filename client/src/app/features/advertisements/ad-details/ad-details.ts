import {CurrencyPipe, DatePipe} from '@angular/common';
import {
  Component,
  computed,
  effect,
  inject,
  input,
  linkedSignal,
  numberAttribute,
  signal,
  untracked,
} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatChipsModule} from '@angular/material/chips';
import {MatDialog} from '@angular/material/dialog';
import {MatIconModule} from '@angular/material/icon';
import {RouterLink} from '@angular/router';
import {Notify} from '../../../core/notify/notify';
import {Session} from '../../../core/session/session';
import {confirm} from '../../../shared/confirm-dialog/confirm-dialog';
import {readAsDataUrls} from '../../../shared/files/read-as-data-urls';
import {AdCard} from '../ad-card/ad-card';
import {AdSummary, PLACEHOLDER_IMAGE} from '../advertisement';
import {AdvertisementApi} from '../advertisement-api';

@Component({
  selector: 'app-ad-details',
  imports: [
    CurrencyPipe,
    DatePipe,
    RouterLink,
    AdCard,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
  ],
  templateUrl: './ad-details.html',
  styleUrl: './ad-details.scss',
})
export default class AdDetails {
  /** Route parameter. */
  readonly id = input.required({transform: numberAttribute});
  protected readonly placeholder = PLACEHOLDER_IMAGE;
  protected readonly selectedId = signal<number | null>(null);
  private readonly advertisementApi = inject(AdvertisementApi);
  protected readonly advertisement = this.advertisementApi.details(() => this.id());
  private readonly dialog = inject(MatDialog);
  private readonly notify = inject(Notify);
  private readonly session = inject(Session);
  private readonly loaded = computed(() =>
    this.advertisement.hasValue() ? this.advertisement.value() : undefined,
  );
  protected readonly selected = computed(() => {
    const images = this.loaded()?.images ?? [];
    return images.find((image) => image.id === this.selectedId()) ?? images[0] ?? null;
  });
  protected readonly mine = computed(
    () => this.loaded()?.seller.username === this.session.username(),
  );

  // the seller's other advertisements, one page at a time, restarting for every advertisement
  private readonly seller = computed(() => this.loaded()?.seller.username);
  private readonly morePage = linkedSignal({source: this.seller, computation: () => 0});
  private readonly morePageResult = this.advertisementApi.byCreator(() => {
    const username = this.seller();
    return username ? {username, page: this.morePage(), excludeId: this.id()} : undefined;
  });
  private readonly lastPage = computed(() =>
    this.morePageResult.hasValue() ? (this.morePageResult.value() ?? []) : [],
  );
  protected readonly more = linkedSignal<AdSummary[], AdSummary[]>({
    source: this.lastPage,
    computation: (page, previous) =>
      untracked(this.morePage) === 0 ? page : [...(previous?.value ?? []), ...page],
  });
  protected readonly hasMore = computed(() => this.lastPage().length > 0);

  constructor() {
    // once per opened advertisement: count the view (the server counts one per user)
    effect(() => {
      const id = this.id();
      untracked(() => this.advertisementApi.view(id).subscribe({error: () => undefined}));
    });
  }

  protected loadMore(): void {
    this.morePage.update((page) => page + 1);
  }

  protected async upload(event: Event): Promise<void> {
    const images = await readAsDataUrls((event.target as HTMLInputElement).files);
    if (images.length) {
      this.advertisementApi.addPhotos(this.id(), images).subscribe({
        next: (ad) => {
          this.notify.ok('Images uploaded');
          this.advertisement.set(ad);
        },
        error: (error) => this.notify.error(error),
      });
    }
  }

  protected deletePhoto(photoId: number): void {
    confirm(this.dialog, 'Delete this image?').subscribe(() =>
      this.advertisementApi.deletePhoto(this.id(), photoId).subscribe({
        next: () => {
          this.notify.ok('Image deleted');
          this.selectedId.set(null);
          this.advertisement.reload();
        },
        error: (error) => this.notify.error(error),
      }),
    );
  }
}
