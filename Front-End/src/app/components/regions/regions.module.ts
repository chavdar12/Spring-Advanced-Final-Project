import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RegionsListComponent} from './regions-list/regions-list.component';
import {RegionItemComponent} from './region-item/region-item.component';
import {RegionsRoutingModule} from './regions-routing.module';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatListModule} from '@angular/material/list';

@NgModule({
  declarations: [RegionsListComponent, RegionItemComponent],
  imports: [
    CommonModule,
    RegionsRoutingModule,
    MatFormFieldModule,
    MatExpansionModule,
    MatListModule
  ]
})
export class RegionsModule {
}
