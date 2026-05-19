import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DOCUMENT } from '@angular/common';

@Component({
  selector: 'app-login-ott',
  templateUrl: './login-ott.component.html',
  styleUrl: './login-ott.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginOttComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private document = inject(DOCUMENT);

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.router.navigate(['/login']);

      return;
    }

    const form = this.document.createElement('form');
    form.method = 'POST';
    form.action = '/login/ott';

    const input = this.document.createElement('input');
    input.type = 'hidden';
    input.name = 'token';
    input.value = token;

    form.appendChild(input);
    this.document.body.appendChild(form);
    form.submit();
  }
}
