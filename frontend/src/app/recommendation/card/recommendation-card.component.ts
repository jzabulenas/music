import {
  ChangeDetectionStrategy,
  Component,
  input,
  linkedSignal,
  output,
  signal,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { Recommendation } from '../recommendation.model';

@Component({
  selector: 'app-recommendation-card',
  imports: [MatCardModule, MatButtonModule],
  templateUrl: './recommendation-card.component.html',
  styleUrl: './recommendation-card.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RecommendationCardComponent {
  recommendation = input.required<Recommendation>();
  alreadyBlocked = input(false);
  saved = output<Recommendation>();
  blocked = output<Recommendation>();

  protected readonly isSaved = signal(false);
  // Derived from `alreadyBlocked` on each load, but stays locally overridable so
  // clicking the button flips it immediately without waiting for a reload.
  protected readonly isBlocked = linkedSignal(() => this.alreadyBlocked());

  protected onSave(): void {
    this.isSaved.set(true);
    this.saved.emit(this.recommendation());
  }

  protected onBlock(): void {
    this.isBlocked.set(true);
    this.blocked.emit(this.recommendation());
  }
}
