import {httpResource} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Region, Town, TownInRegion} from './region';

@Injectable({providedIn: 'root'})
export class RegionApi {
  regions() {
    return httpResource<Region[]>(() => '/api/regions', {defaultValue: []});
  }

  townsIn(region: () => string) {
    return httpResource<TownInRegion[]>(
      () => `/api/regions/${encodeURIComponent(region())}/towns`,
      {defaultValue: []},
    );
  }

  towns() {
    return httpResource<Town[]>(() => '/api/towns', {defaultValue: []});
  }
}
