import {Component, computed, inject} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {RouterLink} from '@angular/router';
import {Session} from '../../core/session/session';
import {AdCard} from '../advertisements/ad-card/ad-card';
import {CategoryApi} from '../categories/category-api';

const TOP_CATEGORIES = 3;

@Component({
  selector: 'app-home',
  imports: [RouterLink, MatButtonModule, AdCard],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export default class Home {
  protected readonly session = inject(Session);
  /** The most used categories, their advertisements ordered by likes. */
  protected readonly categories = computed(() =>
    (this.top.hasValue() ? this.top.value() : []).map((category) => ({
      ...category,
      advertisements: [...category.advertisements].sort(
        (a, b) => b.likedBy.length - a.likedBy.length,
      ),
    })),
  );
  private readonly categoryApi = inject(CategoryApi);
  protected readonly top = this.categoryApi.top(() =>
    this.session.loggedIn() ? TOP_CATEGORIES : undefined,
  );
}
