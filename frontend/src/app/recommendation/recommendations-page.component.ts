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
import { SavedArtistService } from '../saved/saved-artist.service';
import { BlockedArtistService } from '../blocked/blocked-artist.service';
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
  private readonly savedArtistService = inject(SavedArtistService);
  private readonly blockedArtistService = inject(BlockedArtistService);

  protected readonly artistCount = computed(() => this.artistService.artists().length);
  protected readonly recommendations = this.recommendationService.recommendations;
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly blockedArtistNames = signal<ReadonlySet<string>>(new Set());
  protected readonly savedArtistNames = computed(
    () => new Set(this.savedArtistService.savedArtists().map((artist) => artist.name))
  );

  ngOnInit(): void {
    this.artistService.load().subscribe({
      error: () => this.error.set('Failed to load artists.'),
    });

    this.recommendationService.load().subscribe({
      error: () => this.error.set('Failed to load recommendations.'),
    });

    this.savedArtistService.load().subscribe({
      error: () => this.error.set('Failed to load saved artists.'),
    });

    this.blockedArtistService.list().subscribe({
      next: (names) => this.blockedArtistNames.set(new Set(names)),
      error: () => this.error.set('Failed to load blocked artists.'),
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
    this.savedArtistService.save(recommendation.name, recommendation.genre).subscribe({
      error: () => this.error.set('Failed to save artist.'),
    });
  }

  protected onBlock(recommendation: Recommendation): void {
    this.blockedArtistService.block(recommendation.name).subscribe({
      next: () =>
        this.blockedArtistNames.update(
          (names) => new Set([...names, recommendation.name])
        ),
      error: () => this.error.set('Failed to block artist.'),
    });
  }
}
