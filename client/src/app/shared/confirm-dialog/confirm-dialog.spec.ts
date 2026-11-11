import {TestBed} from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';
import {firstValueFrom, of} from 'rxjs';
import {confirm, ConfirmDialog} from './confirm-dialog';

describe('confirm', () => {
  function dialogClosingWith(result: boolean | undefined) {
    const open = vi.fn().mockReturnValue({afterClosed: () => of(result)});
    return {dialog: {open} as unknown as MatDialog, open};
  }

  it('emits only when the user confirms', () => {
    const confirmed = vi.fn();
    const {dialog, open} = dialogClosingWith(true);
    confirm(dialog, 'Delete it?').subscribe(confirmed);

    expect(open).toHaveBeenCalledWith(ConfirmDialog, {data: 'Delete it?'});
    expect(confirmed).toHaveBeenCalledWith(true);

    const cancelled = vi.fn();
    confirm(dialogClosingWith(false).dialog, 'Delete it?').subscribe(cancelled);
    confirm(dialogClosingWith(undefined).dialog, 'Delete it?').subscribe(cancelled);
    expect(cancelled).not.toHaveBeenCalled();
  });

  it('shows the message', async () => {
    const dialog = TestBed.inject(MatDialog);
    dialog.open(ConfirmDialog, {data: 'Really delete?'});
    await firstValueFrom(dialog.openDialogs[0].afterOpened());

    expect(document.body.textContent).toContain('Really delete?');
    dialog.closeAll();
  });
});
