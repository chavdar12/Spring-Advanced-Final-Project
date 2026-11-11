import {Component, inject} from '@angular/core';
import {MatExpansionModule} from '@angular/material/expansion';
import {RegionApi} from './region-api';
import {RegionTowns} from './region-towns/region-towns';

@Component({
  selector: 'app-regions',
  imports: [MatExpansionModule, RegionTowns],
  templateUrl: './regions.html',
})
export default class Regions {
  protected readonly regions = inject(RegionApi).regions();
}
