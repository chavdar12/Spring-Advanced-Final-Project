import {Component, inject, signal} from '@angular/core';
import {form, FormField, FormRoot, maxLength, required} from '@angular/forms/signals';
import {MatButtonModule} from '@angular/material/button';
import {MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {firstValueFrom} from 'rxjs';
import {Notify} from '../../../core/notify/notify';
import {readAsDataUrls} from '../../../shared/files/read-as-data-urls';
import {CategoryApi} from '../category-api';

@Component({
  selector: 'app-category-dialog',
  imports: [
    FormField,
    FormRoot,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './category-dialog.html',
  styleUrl: './category-dialog.scss',
})
export class CategoryDialog {
  protected readonly category = signal({name: '', image: ''});
  protected readonly form = form(
    this.category,
    (path) => {
      required(path.name, {message: 'Name is required'});
      maxLength(path.name, 50, {message: 'At most 50 characters'});
      required(path.image, {message: 'Pick an image'});
    },
    {submission: {action: () => this.save()}},
  );
  private readonly categoryApi = inject(CategoryApi);
  private readonly notify = inject(Notify);
  private readonly dialogRef = inject<MatDialogRef<CategoryDialog, boolean>>(MatDialogRef);

  protected async pick(event: Event): Promise<void> {
    const [image = ''] = await readAsDataUrls((event.target as HTMLInputElement).files);
    this.form.image().value.set(image);
  }

  private async save(): Promise<undefined> {
    const {name, image} = this.category();
    try {
      await firstValueFrom(this.categoryApi.create(name.trim(), image));
      this.notify.ok('Category created');
      this.dialogRef.close(true);
    } catch (error) {
      this.notify.error(error);
    }
    return undefined;
  }
}
