import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { LoginConfirmComponent } from './login-confirm.component';
import { LoginFormComponent } from './login-form.component';

@Component({
  selector: 'app-login',
  imports: [MatCardModule, LoginConfirmComponent, LoginFormComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  protected confirmed = signal(false);
}
