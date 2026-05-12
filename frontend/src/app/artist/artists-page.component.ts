import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ArtistInputComponent } from './artist-input.component';
import { ArtistListComponent } from './artist-list.component';
import { ArtistService } from './artist.service';

@Component({
  selector: 'app-artists-page',
  imports: [ArtistInputComponent, ArtistListComponent],
  templateUrl: './artists-page.component.html',
  styleUrl: './artists-page.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ArtistsPageComponent implements OnInit {
  private readonly artistService = inject(ArtistService);

  protected readonly artists = this.artistService.artists;
  protected readonly count = computed(() => this.artists().length);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.artistService.load().subscribe({
      error: () => this.error.set('Failed to load artists.'),
    });
  }

  protected onArtistAdded(name: string): void {
    this.error.set(null);
    this.artistService.add(name).subscribe({
      error: () => this.error.set('Failed to add artist.'),
    });
  }

  protected onArtistDeleted(id: number): void {
    this.error.set(null);
    this.artistService.remove(id).subscribe({
      error: () => this.error.set('Failed to remove artist.'),
    });
  }
}
