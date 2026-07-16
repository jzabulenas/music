import { ChangeDetectionStrategy, Component, inject, output, viewChild } from '@angular/core';
import { FormBuilder, FormGroupDirective, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-artist-input',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './artist-input.component.html',
  styleUrl: './artist-input.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ArtistInputComponent {
  artistAdded = output<string>();

  private readonly fb = inject(FormBuilder);
  private readonly formDirective = viewChild.required(FormGroupDirective);

  protected form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(255)]],
  });
  protected nameControl = this.form.controls.name;

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();

      return;
    }

    this.artistAdded.emit(this.nameControl.value!);
    this.formDirective().resetForm();
  }
}
