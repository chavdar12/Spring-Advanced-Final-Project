import {Component, OnInit} from '@angular/core';
import {FormGroup, FormBuilder, Validators} from '@angular/forms';
import {CategoryBindingModel} from 'src/app/core/models/category-create';
import {CategoryService} from 'src/app/core/services/category.service';
import {TokenStorageService} from 'src/app/core/services/token-storage.service';
import {AlertService} from '../../alert';

const created = 'Created successfully!';

@Component({
  selector: 'app-category-modal-create',
  templateUrl: './category-modal-create.component.html',
  styleUrls: ['./category-modal-create.component.css']
})
export class CategoryModalCreateComponent implements OnInit {

  image: string;
  form: FormGroup;
  category: CategoryBindingModel;

  constructor(private formBuilder: FormBuilder,
              private categoryService: CategoryService,
              private tokenService: TokenStorageService,
              private alertService: AlertService) {
  }

  ngOnInit(): void {
    this.form = this.formBuilder.group({
      name: ['', Validators.required],
      file: ['', Validators.required],
      fileSource: ['', Validators.required],
    });
  }

  submit() {
    this.category = new CategoryBindingModel(this.f.name.value, this.f.fileSource.value, this.tokenService.getUsername());
    this.categoryService.createCategory(this.category).subscribe(
      data => {
        console.log(data);
        this.alertService.success(created, {autoClose: true});
      },
      error => {
        this.alertService.error(error.error.message, {autoClose: true});
      });
  }

  get f() {
    return this.form.controls;
  }

  onFileChange(event) {
    if (event.target.files && event.target.files[0]) {
      var reader = new FileReader();
      reader.onload = (event: any) => {
        this.image = event.target.result;
        this.form.patchValue({fileSource: this.image});
      };
      reader.readAsDataURL(event.target.files[0]);
    }
  }

  resetForm() {
    this.image = undefined;
    this.form.reset();
  }
}
