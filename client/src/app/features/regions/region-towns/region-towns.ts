import {Component, inject, input} from '@angular/core';
import {MatListModule} from '@angular/material/list';
import {RegionApi} from '../region-api';

/** Towns of one region with their advertisement counts. */
@Component({
  selector: 'app-region-towns',
  imports: [MatListModule],
  templateUrl: './region-towns.html',
})
export class RegionTowns {
  readonly region = input.required<string>();

  protected readonly towns = inject(RegionApi).townsIn(() => this.region());
}
