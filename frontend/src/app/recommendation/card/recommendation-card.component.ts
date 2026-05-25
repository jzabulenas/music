import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';
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
  saved = output<Recommendation>();

  protected readonly isSaved = signal(false);

  protected onSave(): void {
    this.isSaved.set(true);
    this.saved.emit(this.recommendation());
  }
}
