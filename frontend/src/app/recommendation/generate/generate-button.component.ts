import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-generate-button',
  imports: [MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './generate-button.component.html',
  styleUrl: './generate-button.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GenerateButtonComponent {
  artistCount = input.required<number>();
  loading = input<boolean>(false);
  generate = output<void>();

  protected readonly needsMoreArtists = computed(() => this.artistCount() < 3);
  protected readonly remaining = computed(() => 3 - this.artistCount());
  protected readonly isDisabled = computed(() => this.loading() || this.needsMoreArtists());

  protected onGenerate(): void {
    this.generate.emit();
  }
}
