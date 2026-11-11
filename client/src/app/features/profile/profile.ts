import {Component, computed, inject, linkedSignal, signal} from '@angular/core';
import {form, FormField, FormRoot, min, pattern, required, validate} from '@angular/forms/signals';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatDialog} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatTabsModule} from '@angular/material/tabs';
import {firstValueFrom} from 'rxjs';
import {Notify} from '../../core/notify/notify';
import {confirm} from '../../shared/confirm-dialog/confirm-dialog';
import {AdCard} from '../advertisements/ad-card/ad-card';
import {AdFormDialog} from '../advertisements/ad-form-dialog/ad-form-dialog';
import {AdSummary} from '../advertisements/advertisement';
import {AdvertisementApi} from '../advertisements/advertisement-api';
import {Chat} from '../chat/chat';
import {RegionApi} from '../regions/region-api';
import {ProfileApi} from './profile-api';
import {ProfileInfo, UserProfile} from './user-profile';

const NAME = /^[A-ZА-Я][a-zа-я]{3,11}$/;
const NAME_MESSAGE = 'Capitalized, 4–12 letters';
const ROLES_BY_RANK = ['ROLE_ROOT', 'ROLE_ADMIN', 'ROLE_MODERATOR', 'ROLE_USER'];
const PLACEHOLDER_AVATAR =
  'https://res.cloudinary.com/knight-cloud/image/upload/v1586855234/opzt01swy5uezhzexcql.png';

function toInfo(profile: UserProfile | undefined): ProfileInfo {
  return {
    firstName: profile?.firstName ?? '',
    lastName: profile?.lastName ?? '',
    townId: profile?.town?.id ?? 0,
    aboutMe: profile?.aboutMe ?? '',
  };
}

@Component({
  selector: 'app-profile',
  imports: [
    FormField,
    FormRoot,
    AdCard,
    Chat,
    MatTabsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export default class Profile {
  protected readonly placeholderAvatar = PLACEHOLDER_AVATAR;
  protected readonly tab = signal(0);
  private readonly profileApi = inject(ProfileApi);
  protected readonly profile = this.profileApi.get();
  private readonly advertisementApi = inject(AdvertisementApi);
  private readonly regionApi = inject(RegionApi);
  private readonly dialog = inject(MatDialog);
  private readonly notify = inject(Notify);
  private readonly townList = this.regionApi.towns();
  protected readonly towns = computed(() =>
    this.townList.hasValue() ? this.townList.value() : [],
  );
  private readonly loaded = computed(() =>
    this.profile.hasValue() ? this.profile.value() : undefined,
  );
  protected readonly topRole = computed(() => {
    const roles = this.loaded()?.roles ?? [];
    return ROLES_BY_RANK.find((role) => roles.includes(role))
      ?.replace('ROLE_', '')
      .toLowerCase();
  });
  protected readonly info = linkedSignal(() => toInfo(this.loaded()));
  protected readonly infoForm = form(
    this.info,
    (path) => {
      required(path.firstName, {message: NAME_MESSAGE});
      pattern(path.firstName, NAME, {message: NAME_MESSAGE});
      required(path.lastName, {message: NAME_MESSAGE});
      pattern(path.lastName, NAME, {message: NAME_MESSAGE});
      min(path.townId, 1, {message: 'Pick your town'});
      required(path.aboutMe, {message: 'Tell others something about you'});
      pattern(path.aboutMe, /^[A-ZА-Я][a-zа-я ]{1,99}$/, {
        message: 'Starts with a capital letter, up to 100 letters',
      });
    },
    {submission: {action: () => this.saveInfo()}},
  );
  protected readonly password = signal({oldPassword: '', newPassword: '', confirmPassword: ''});
  protected readonly passwordForm = form(
    this.password,
    (path) => {
      required(path.oldPassword, {message: 'Enter your current password'});
      required(path.newPassword, {message: '3–16 characters, no spaces'});
      pattern(path.newPassword, /^\S{3,16}$/, {message: '3–16 characters, no spaces'});
      validate(path.confirmPassword, ({value, valueOf}) =>
        value() === valueOf(path.newPassword)
          ? undefined
          : {kind: 'mismatch', message: 'Passwords do not match'},
      );
    },
    {submission: {action: () => this.savePassword()}},
  );

  protected uploadPicture(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      this.profileApi.changePicture(file).subscribe({
        next: (profile) => this.done('Picture updated', profile),
        error: (error) => this.notify.error(error),
      });
    }
  }

  protected editAd(ad: AdSummary): void {
    this.dialog
      .open(AdFormDialog, {data: ad.id})
      .afterClosed()
      .subscribe((saved) => saved && this.profile.reload());
  }

  protected deleteAd(ad: AdSummary): void {
    confirm(this.dialog, `Delete "${ad.title}" including all its photos?`).subscribe(() =>
      this.advertisementApi.delete(ad.id).subscribe({
        next: () => {
          this.notify.ok('Advertisement deleted');
          this.profile.reload();
        },
        error: (error) => this.notify.error(error),
      }),
    );
  }

  private async saveInfo(): Promise<undefined> {
    try {
      this.done('Profile updated', await firstValueFrom(this.profileApi.update(this.info())));
    } catch (error) {
      this.notify.error(error);
    }
    return undefined;
  }

  private async savePassword(): Promise<undefined> {
    const {oldPassword, newPassword} = this.password();
    try {
      await firstValueFrom(this.profileApi.changePassword(oldPassword, newPassword));
      this.password.set({oldPassword: '', newPassword: '', confirmPassword: ''});
      this.passwordForm().reset();
      this.done('Password changed');
    } catch (error) {
      this.notify.error(error);
    }
    return undefined;
  }

  private done(message: string, profile?: UserProfile): void {
    this.notify.ok(message);
    if (profile) {
      this.profile.set(profile);
    }
    this.tab.set(0);
  }
}
