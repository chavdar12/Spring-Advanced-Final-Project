import {Component, computed, inject, linkedSignal, signal} from '@angular/core';
import {form, FormField, FormRoot, max, min, pattern, required} from '@angular/forms/signals';
import {MatButtonModule} from '@angular/material/button';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {firstValueFrom} from 'rxjs';
import {Notify} from '../../../core/notify/notify';
import {readAsDataUrls} from '../../../shared/files/read-as-data-urls';
import {CategoryApi} from '../../categories/category-api';
import {AdDetails, AdForm} from '../advertisement';
import {AdvertisementApi} from '../advertisement-api';

const EMPTY: AdForm = {
  title: '',
  description: '',
  price: 0,
  condition: '',
  delivery: '',
  categoryId: 0,
};

function toForm(ad: AdDetails | undefined): AdForm {
  if (!ad) {
    return EMPTY;
  }
  const {title, description, price, condition, delivery, categoryId} = ad;
  return {title, description, price, condition, delivery, categoryId};
}

/** Create (no id) or edit (id given) an advertisement. Closes with true on success. */
@Component({
  selector: 'app-ad-form-dialog',
  imports: [
    FormField,
    FormRoot,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './ad-form-dialog.html',
  styleUrl: './ad-form-dialog.scss',
})
export class AdFormDialog {
  protected readonly id = inject<number | null>(MAT_DIALOG_DATA, {optional: true});
  protected readonly images = signal<string[]>([]);
  // same rules as the server's AdvertisementRequest
  private readonly advertisementApi = inject(AdvertisementApi);
  private readonly categoryApi = inject(CategoryApi);
  private readonly notify = inject(Notify);
  private readonly dialogRef = inject<MatDialogRef<AdFormDialog, boolean>>(MatDialogRef);
  private readonly options = this.advertisementApi.options();
  protected readonly conditions = computed(() =>
    this.options.hasValue() ? this.options.value().conditions : [],
  );
  protected readonly deliveries = computed(() =>
    this.options.hasValue() ? this.options.value().deliveries : [],
  );
  private readonly categoryList = this.categoryApi.all();
  protected readonly categories = computed(() =>
    this.categoryList.hasValue() ? this.categoryList.value() : [],
  );
  private readonly existing = this.advertisementApi.details(() => this.id ?? undefined);
  protected readonly ad = linkedSignal(() =>
    toForm(this.existing.hasValue() ? this.existing.value() : undefined),
  );
  protected readonly form = form(
    this.ad,
    (path) => {
      required(path.title, {message: 'Title is required'});
      pattern(path.title, /^[A-ZА-Я][A-ZА-Яа-яa-z\s\d]{4,19}$/, {
        message: '5–20 letters/digits, starting with a capital letter',
      });
      required(path.description, {message: 'Description is required'});
      min(path.price, 0.1, {message: 'Between 0.1 and 10,000'});
      max(path.price, 10000, {message: 'Between 0.1 and 10,000'});
      required(path.condition, {message: 'Pick a condition'});
      required(path.delivery, {message: 'Pick a delivery option'});
      min(path.categoryId, 1, {message: 'Pick a category'});
    },
    {submission: {action: () => this.save()}},
  );

  protected async pick(event: Event): Promise<void> {
    this.images.set(await readAsDataUrls((event.target as HTMLInputElement).files));
  }

  private async save(): Promise<undefined> {
    try {
      await firstValueFrom(
        this.id
          ? this.advertisementApi.update(this.id, this.ad())
          : this.advertisementApi.create(this.ad(), this.images()),
      );
      this.notify.ok(this.id ? 'Advertisement updated' : 'Advertisement created');
      this.dialogRef.close(true);
    } catch (error) {
      this.notify.error(error);
    }
    return undefined;
  }
}
