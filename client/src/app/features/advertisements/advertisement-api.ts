import {HttpClient, httpResource} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {AdDetails, AdForm, AdOptions, AdSearch, AdSummary, Page} from './advertisement';

const BASE = '/api/advertisements';

function searchParams({search, category, condition, min, max, page, sortBy, order}: AdSearch) {
  const params: Record<string, string | number> = {min, max, page, order};
  if (search.trim()) params['search'] = search.trim();
  if (category !== 'All') params['category'] = category;
  if (condition !== 'All') params['condition'] = condition;
  if (sortBy !== 'none') params['sortBy'] = sortBy;
  return params;
}

/**
 * Reads are signal-based resources that refetch when their inputs change; create them in an
 * injection context (a component field). Writes are one-shot observables.
 */
@Injectable({providedIn: 'root'})
export class AdvertisementApi {
  private readonly http = inject(HttpClient);

  search(query: () => AdSearch) {
    return httpResource<Page<AdSummary>>(() => ({url: BASE, params: searchParams(query())}));
  }

  details(id: () => number | undefined) {
    return httpResource<AdDetails>(() => {
      const value = id();
      return value === undefined ? undefined : `${BASE}/${value}`;
    });
  }

  byCreator(query: () => {username: string; page: number; excludeId: number} | undefined) {
    return httpResource<AdSummary[]>(() => {
      const value = query();
      return (
        value && {
          url: `${BASE}/by/${value.username}`,
          params: {page: value.page, excludeId: value.excludeId},
        }
      );
    });
  }

  options() {
    return httpResource<AdOptions>(() => `${BASE}/options`);
  }

  create(ad: AdForm, images: string[]) {
    return this.http.post<AdDetails>(BASE, {...ad, images});
  }

  update(id: number, ad: AdForm) {
    return this.http.put<AdDetails>(`${BASE}/${id}`, ad);
  }

  delete(id: number) {
    return this.http.delete<void>(`${BASE}/${id}`);
  }

  addPhotos(id: number, images: string[]) {
    return this.http.post<AdDetails>(`${BASE}/${id}/photos`, {images});
  }

  deletePhoto(id: number, photoId: number) {
    return this.http.delete<void>(`${BASE}/${id}/photos/${photoId}`);
  }

  view(id: number) {
    return this.http.post<void>(`${BASE}/${id}/views`, null);
  }

  like(id: number) {
    return this.http.post<void>(`${BASE}/${id}/favorite`, null);
  }

  unlike(id: number) {
    return this.http.delete<void>(`${BASE}/${id}/favorite`);
  }
}
