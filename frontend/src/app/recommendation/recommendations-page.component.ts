import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ArtistService } from '../artist/artist.service';
import { AuthService } from '../core/auth.service';
import { RecommendationService } from './recommendation.service';
import { SavedArtistService } from '../saved/saved-artist.service';
import { BlockedArtistService } from '../blocked/blocked-artist.service';
import { GenerateButtonComponent } from './generate/generate-button.component';
import { RecommendationCardComponent } from './card/recommendation-card.component';
import { RecommendationsEmptyStateComponent } from './empty/recommendations-empty-state.component';
import { Recommendation } from './recommendation.model';

@Component({
  selector: 'app-recommendations-page',
  imports: [
    GenerateButtonComponent,
    RecommendationCardComponent,
    RecommendationsEmptyStateComponent,
  ],
  templateUrl: './recommendations-page.component.html',
  styleUrl: './recommendations-page.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RecommendationsPageComponent implements OnInit {
  private readonly artistService = inject(ArtistService);
  private readonly recommendationService = inject(RecommendationService);
  private readonly savedArtistService = inject(SavedArtistService);
  private readonly blockedArtistService = inject(BlockedArtistService);
  private readonly authService = inject(AuthService);

  protected readonly artistCount = computed(() => this.artistService.artists().length);
  protected readonly recommendations = this.recommendationService.recommendations;
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly remainingRequests = computed(
    () => this.authService.currentUser()?.remainingRequests ?? null,
  );

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
      next: () => this.authService.fetchCurrentUser().subscribe(),
      error: (httpError: HttpErrorResponse) => {
        this.error.set(
          httpError.status === 429
            ? 'Daily generation limit reached. Try again tomorrow.'
            : 'Failed to generate recommendations.',
        );
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  protected onSave(recommendation: Recommendation): void {
    this.reviewRecommendation(
      recommendation,
      this.savedArtistService.save(recommendation.name, recommendation.genre),
      'Failed to save artist.',
    );
  }

  protected onBlock(recommendation: Recommendation): void {
    this.reviewRecommendation(
      recommendation,
      this.blockedArtistService.block(recommendation.name),
      'Failed to block artist.',
    );
  }

  // Removing the card right away makes reviewing the queue feel instant. If persisting the
  // decision fails, put the card back at its original position so nothing is lost.
  private reviewRecommendation(
    recommendation: Recommendation,
    action: Observable<unknown>,
    failureMessage: string,
  ): void {
    this.error.set(null);

    const index = this.recommendations().indexOf(recommendation);

    this.recommendationService.remove(recommendation);

    action.subscribe({
      error: () => {
        this.recommendationService.restore(recommendation, index);
        this.error.set(failureMessage);
      },
    });
  }
}
