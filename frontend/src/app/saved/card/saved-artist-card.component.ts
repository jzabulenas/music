import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { SavedArtist } from '../saved-artist.model';

@Component({
  selector: 'app-saved-artist-card',
  imports: [MatCardModule, MatButtonModule],
  templateUrl: './saved-artist-card.component.html',
  styleUrl: './saved-artist-card.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SavedArtistCardComponent {
  savedArtist = input.required<SavedArtist>();
  removed = output<number>();

  protected onRemove(): void {
    this.removed.emit(this.savedArtist().id);
  }
}
