import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-login-confirm',
  templateUrl: './login-confirm.component.html',
  styleUrl: './login-confirm.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginConfirmComponent {}
