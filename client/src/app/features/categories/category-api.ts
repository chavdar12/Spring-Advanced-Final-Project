import {HttpClient, httpResource} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Category, TopCategory} from './category';

@Injectable({providedIn: 'root'})
export class CategoryApi {
  private readonly http = inject(HttpClient);

  all() {
    return httpResource<Category[]>(() => '/api/categories', {defaultValue: []});
  }

  /** The most used categories; no request while {@code count} is undefined. */
  top(count: () => number | undefined) {
    return httpResource<TopCategory[]>(
      () => {
        const value = count();
        return value === undefined
          ? undefined
          : {url: '/api/categories/top', params: {count: value}};
      },
      {defaultValue: []},
    );
  }

  create(name: string, image: string) {
    return this.http.post<Category>('/api/categories', {name, image});
  }
}
