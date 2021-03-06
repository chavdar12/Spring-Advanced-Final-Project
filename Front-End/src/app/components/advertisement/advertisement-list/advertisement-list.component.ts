import {Component, OnInit, ViewChild} from '@angular/core';
import {Observable} from 'rxjs';
import {AdvertisementInfoList} from '../../../core/models/advertisement-info-list';
import {AdvertisementService} from '../../../core/services/advertisement.service';
import {AdvertisementModalEditComponent} from '../advertisement-modal-edit/advertisement-modal-edit.component';
import {ActivatedRoute} from '@angular/router';
import {ProfileService} from 'src/app/core/services/profile.service';

const initialMinValue = 0;
const initialMaxValue = 10000;
const defaultState = 'All';
const defaultSelectedCriteria = 'none';
const defaultSortCriterias = ['price', 'views', 'title'];

@Component({
  selector: 'app-advertisement-list',
  templateUrl: './advertisement-list.component.html',
  styleUrls: ['./advertisement-list.component.css']
})
export class AdvertisementListComponent implements OnInit {

  noResultsImgUrl = 'https://res.cloudinary.com/knight-cloud/image/upload/v1589058809/aorml5wwdh5s1ymgejnz.png';

  @ViewChild(AdvertisementModalEditComponent) editComponent: AdvertisementModalEditComponent;

  config: any;
  isCompletedProfile: boolean;
  descending: boolean;
  sortCriterias: string[];
  selectedCondition: string;
  conditions$: Observable<String[]>;
  selectedSortCriteria: string;
  currentCategory: string;
  minPriceRange: number;
  maxPriceRange: number;
  advertisements$: Observable<AdvertisementInfoList[]>;
  startIndex: number = 0;
  endIndex: number = 8;
  currentPage: number = 1;
  searchText: string;
  advertisementToModifyId: number;

  constructor(private advertisementService: AdvertisementService,
              private route: ActivatedRoute,
              private profileService: ProfileService) {
  }

  ngOnInit() {
    this.profileService.isCompleted()
      .subscribe(
        successData => {
          this.isCompletedProfile = successData;
        });

    this.config = {id: 'list', itemsPerPage: 6, currentPage: 1};
    this.conditions$ = this.advertisementService.getAllConditions();
    this.descending = false;
    this.selectedSortCriteria = defaultSelectedCriteria;
    this.selectedCondition = defaultState;
    this.sortCriterias = defaultSortCriterias;

    this.currentCategory = this.route.snapshot.params.category;

    this.minPriceRange = initialMinValue;
    this.maxPriceRange = initialMaxValue;
    this.advertisementToModifyId = null;
    this.advertisementService.refreshNeeded$.subscribe(() => this.search());
    this.search();
  }

  changePage(event) {
    this.config.currentPage = event;
    this.search();
  }

  search() {

    const sortOrder = this.descending ? 'descending' : 'ascending';

    this.refreshPaginationCountBeforeSearch();

    const params = {
      search: this.searchText,
      min: this.minPriceRange,
      max: this.maxPriceRange,
      condition: this.selectedCondition,
      category: this.currentCategory,
      page: this.config.currentPage,
      order: sortOrder,
      sortBy: this.selectedSortCriteria
    };

    window.history.replaceState({}, '', `/advertisements/category/${params.category}`);

    this.advertisements$ = this.advertisementService.getByFullSearch(params);
  }

  changeCategory(data) {
    if (data !== undefined && data !== '' && data !== null) {
      this.currentCategory = data.trim();
      this.config.currentPage = 1;
      this.search();
    }
  }

  sort() {
    if (this.selectedSortCriteria !== defaultSelectedCriteria) {
      this.changePage(this.config.currentPage);
    }
  }

  order() {
    if (this.selectedSortCriteria !== defaultSelectedCriteria) {
      this.sort();
    }
  }

  changePriceRange(event) {
    this.minPriceRange = event[0];
    this.maxPriceRange = event[1];
  }

  changeAdvertisementToModifyId(event) {
    this.advertisementToModifyId = event;
    this.editComponent.refreshAdvertisement();
  }

  private refreshPaginationCountBeforeSearch() {

    const params = {
      min: this.minPriceRange,
      max: this.maxPriceRange,
      condition: this.selectedCondition,
      category: this.currentCategory,
      search: this.searchText
    };

    this.advertisementService.getCountBySearch(params)
      .subscribe(data => {
        this.config.totalItems = data;
      });
  }
}
