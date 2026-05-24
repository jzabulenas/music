import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ArtistService } from '../artist/artist.service';
import { RecommendationService } from './recommendation.service';
import { GenerateButtonComponent } from './generate/generate-button.component';
import { RecommendationCardComponent } from './card/recommendation-card.component';
import { Recommendation } from './recommendation.model';

@Component({
  selector: 'app-recommendations-page',
  imports: [GenerateButtonComponent, RecommendationCardComponent],
  templateUrl: './recommendations-page.component.html',
  styleUrl: './recommendations-page.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RecommendationsPageComponent implements OnInit {
  private readonly artistService = inject(ArtistService);
  private readonly recommendationService = inject(RecommendationService);

  protected readonly artistCount = computed(() => this.artistService.artists().length);
  protected readonly recommendations = this.recommendationService.recommendations;
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.artistService.load().subscribe({
      error: () => this.error.set('Failed to load artists.'),
    });

    this.recommendationService.load().subscribe({
      error: () => this.error.set('Failed to load recommendations.'),
    });
  }

  protected onGenerate(): void {
    this.loading.set(true);
    this.error.set(null);

    this.recommendationService.generate().subscribe({
      error: () => {
        this.error.set('Failed to generate recommendations.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  protected onSave(recommendation: Recommendation): void {
    this.recommendationService.save(recommendation.name, recommendation.genre).subscribe({
      error: () => this.error.set('Failed to save artist.'),
    });
  }
}
