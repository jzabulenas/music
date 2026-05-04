import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-login',
  template: '<p>Login</p>',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {}
