import {Component, computed, inject, input, linkedSignal, signal} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatChipsModule} from '@angular/material/chips';
import {MatDialog} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatSelectModule} from '@angular/material/select';
import {MatSliderModule} from '@angular/material/slider';
import {Router, RouterLink} from '@angular/router';
import {Observable} from 'rxjs';
import {Notify} from '../../../core/notify/notify';
import {Session} from '../../../core/session/session';
import {confirm} from '../../../shared/confirm-dialog/confirm-dialog';
import {CategoryApi} from '../../categories/category-api';
import {CategoryDialog} from '../../categories/category-dialog/category-dialog';
import {ProfileApi} from '../../profile/profile-api';
import {AdCard} from '../ad-card/ad-card';
import {AdFormDialog} from '../ad-form-dialog/ad-form-dialog';
import {AdSearch, AdSummary, PAGE_SIZE} from '../advertisement';
import {AdvertisementApi} from '../advertisement-api';

@Component({
  selector: 'app-ad-list',
  imports: [
    RouterLink,
    AdCard,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatIconModule,
    MatSliderModule,
    MatPaginatorModule,
  ],
  templateUrl: './ad-list.html',
  styleUrl: './ad-list.scss',
})
export default class AdList {
  /** Route parameter; picking a category navigates, so the URL stays the source of truth. */
  readonly category = input('All');
  protected readonly session = inject(Session);
  protected readonly pageSize = PAGE_SIZE;
  protected readonly search = signal('');
  protected readonly condition = signal('All');
  protected readonly sortBy = signal('none');
  protected readonly order = signal<'ASC' | 'DESC'>('DESC');
  protected readonly min = signal(0);
  protected readonly max = signal(10000);
  // any filter change goes back to the first page
  protected readonly page = linkedSignal({
    source: () => [this.category(), this.search(), this.condition(), this.min(), this.max()],
    computation: () => 1,
  });
  protected readonly found = computed(() =>
    this.results.hasValue() ? this.results.value() : {content: [], total: 0},
  );
  private readonly advertisementApi = inject(AdvertisementApi);
  private readonly categoryApi = inject(CategoryApi);
  private readonly profileApi = inject(ProfileApi);
  private readonly dialog = inject(MatDialog);
  private readonly notify = inject(Notify);
  private readonly router = inject(Router);
  private readonly categoryList = this.categoryApi.all();
  protected readonly categories = computed(() =>
    this.categoryList.hasValue() ? this.categoryList.value() : [],
  );
  private readonly options = this.advertisementApi.options();
  protected readonly conditions = computed(() =>
    this.options.hasValue() ? this.options.value().conditions : [],
  );
  private readonly profile = this.profileApi.get();
  protected readonly profileCompleted = computed(
    () => this.profile.hasValue() && this.profile.value().completed,
  );
  private readonly query = computed<AdSearch>(() => ({
    search: this.search(),
    category: this.category(),
    condition: this.condition(),
    min: this.min(),
    max: this.max(),
    page: this.page(),
    sortBy: this.sortBy(),
    order: this.order(),
  }));
  protected readonly results = this.advertisementApi.search(() => this.query());

  protected selectCategory(name: string | undefined): void {
    const category = name ?? 'All';
    void this.router.navigate(['/advertisements/category', category], {replaceUrl: true});
  }

  protected onPage(event: PageEvent): void {
    this.page.set(event.pageIndex + 1);
  }

  protected create(): void {
    this.reloadWhenSaved(this.dialog.open(AdFormDialog).afterClosed());
  }

  protected edit(ad: AdSummary): void {
    this.reloadWhenSaved(this.dialog.open(AdFormDialog, {data: ad.id}).afterClosed());
  }

  protected newCategory(): void {
    this.dialog.open(CategoryDialog);
  }

  protected remove(ad: AdSummary): void {
    confirm(this.dialog, `Delete "${ad.title}" including all its photos?`).subscribe(() =>
      this.advertisementApi.delete(ad.id).subscribe({
        next: () => {
          this.notify.ok('Advertisement deleted');
          this.results.reload();
        },
        error: (error) => this.notify.error(error),
      }),
    );
  }

  private reloadWhenSaved(closed: Observable<boolean | undefined>): void {
    closed.subscribe((saved) => saved && this.results.reload());
  }
}
