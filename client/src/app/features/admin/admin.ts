import {Component, computed, effect, inject, signal, viewChild} from '@angular/core';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {MatSelectModule} from '@angular/material/select';
import {MatSort, MatSortModule} from '@angular/material/sort';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {Notify} from '../../core/notify/notify';
import {Session} from '../../core/session/session';
import {AdminApi} from './admin-api';
import {AdminUser, canChangeRole} from './admin-user';

@Component({
  selector: 'app-admin',
  imports: [
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './admin.html',
  styleUrl: './admin.scss',
})
export default class Admin {
  protected readonly session = inject(Session);
  protected readonly columns = ['id', 'photoUrl', 'username', 'role'];
  protected readonly filter = signal('');
  protected readonly dataSource = new MatTableDataSource<AdminUser>([]);
  private readonly adminApi = inject(AdminApi);
  private readonly notify = inject(Notify);
  private readonly roleList = this.adminApi.roles();
  protected readonly roles = computed(() =>
    this.roleList.hasValue() ? this.roleList.value() : [],
  );
  private readonly users = this.adminApi.users();
  private readonly userList = computed(() => (this.users.hasValue() ? this.users.value() : []));
  private readonly myRole = computed(
    () => this.userList().find((user) => user.username === this.session.username())?.role,
  );
  private readonly paginator = viewChild.required(MatPaginator);
  private readonly sort = viewChild.required(MatSort);

  constructor() {
    // MatTableDataSource is not signal-based, so keep it in sync from the signals
    effect(() => {
      this.dataSource.data = this.userList();
      this.dataSource.paginator = this.paginator();
      this.dataSource.sort = this.sort();
    });
    effect(() => (this.dataSource.filter = this.filter().trim().toLowerCase()));
  }

  protected canChange(user: AdminUser): boolean {
    return user.username !== this.session.username() && canChangeRole(this.myRole(), user.role);
  }

  protected changeRole(user: AdminUser, role: string): void {
    this.adminApi.changeRole(user.id, role).subscribe({
      next: () => {
        this.notify.ok(`${user.username} is now ${role.replace('ROLE_', '').toLowerCase()}`);
        this.users.reload();
      },
      error: (error) => {
        this.notify.error(error);
        this.users.reload();
      },
    });
  }
}
