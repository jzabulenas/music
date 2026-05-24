import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { SavedArtistService } from './saved-artist.service';
import { SavedArtistCardComponent } from './card/saved-artist-card.component';
import { EmptyStateComponent } from './empty/empty-state.component';

@Component({
  selector: 'app-saved-artists-page',
  imports: [SavedArtistCardComponent, EmptyStateComponent],
  templateUrl: './saved-artists-page.component.html',
  styleUrl: './saved-artists-page.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SavedArtistsPageComponent implements OnInit {
  private readonly savedArtistService = inject(SavedArtistService);

  protected readonly savedArtists = this.savedArtistService.savedArtists;
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.savedArtistService.load().subscribe({
      error: () => this.error.set('Failed to load saved artists.'),
    });
  }

  protected onRemove(id: number): void {
    this.error.set(null);
    this.savedArtistService.remove(id).subscribe({
      error: () => this.error.set('Failed to remove artist.'),
    });
  }
}
