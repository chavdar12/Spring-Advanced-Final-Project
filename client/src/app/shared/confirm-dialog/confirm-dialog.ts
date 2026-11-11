import {Component, inject} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MAT_DIALOG_DATA, MatDialog, MatDialogModule} from '@angular/material/dialog';
import {filter, Observable} from 'rxjs';

@Component({
  selector: 'app-confirm-dialog',
  imports: [MatDialogModule, MatButtonModule],
  templateUrl: './confirm-dialog.html',
})
export class ConfirmDialog {
  protected readonly message = inject<string>(MAT_DIALOG_DATA);
}

/** Emits once, only if the user confirmed. */
export function confirm(dialog: MatDialog, message: string): Observable<boolean> {
  return dialog.open(ConfirmDialog, {data: message}).afterClosed().pipe(filter(Boolean));
}
