import {Component, OnInit} from '@angular/core';
import {LoginService} from 'src/app/core/services/login.service';
import {TokenStorageService} from 'src/app/core/services/token-storage.service';
import {Router} from '@angular/router';
import {NavigationLink} from 'src/app/core/models/nav-link';
import {ProfileService} from 'src/app/core/services/profile.service';

const navLinks = [new NavigationLink('/advertisement/all', 'Advertisements'),
  new NavigationLink('/home', 'Home'),
  new NavigationLink('/user/profile', 'Profile'),
  new NavigationLink('/user/admin"', 'Admin'),
  new NavigationLink('auth/signin', 'Login'),
];

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {

  status: boolean = false;
  roles: string[];
  links: NavigationLink[];

  constructor(private tokenStorage: TokenStorageService,
              private loginService: LoginService,
              private router: Router,
              private profileService: ProfileService) {

    this.loginService.getRoles().subscribe(data => {

      if (data['roles']) {
        this.roles = data['roles'].map(x => x['authority']);
      } else {
        this.roles = null;
      }

    });
  }

  toggleStatus() {
    this.status = !this.status;
  }

  ngOnInit() {
    this.links = navLinks;
    if (this.tokenStorage.getToken()) {
      this.roles = this.tokenStorage.getAuthorities();
    }
  }

  logout() {
    this.loginService.clearRoles();
    this.profileService.disconnect().subscribe(res => console.log(res));
    this.tokenStorage.signOut();
    this.router.navigate(['auth/signin']);
  }
}
