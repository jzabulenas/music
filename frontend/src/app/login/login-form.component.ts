import { ChangeDetectionStrategy, Component, inject, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-login-form',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './login-form.component.html',
  styleUrl: './login-form.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginFormComponent {
  submitted = output<void>();

  private http = inject(HttpClient);
  private fb = inject(FormBuilder);

  protected form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
  });
  protected loading = signal(false);
  protected error = signal<string | null>(null);
  protected emailControl = this.form.controls.email;

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();

      return;
    }
    this.loading.set(true);
    this.error.set(null);

    // `value` is non-null: Validators.required guarantees a string at this point
    // The `!` is added to pass error checking
    const body = new HttpParams().set('username', this.form.controls.email.value!);

    this.http.post('/ott/generate', body).subscribe({
      next: () => {
        this.submitted.emit();
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Something went wrong. Please try again.');
      },
    });
  }
}
