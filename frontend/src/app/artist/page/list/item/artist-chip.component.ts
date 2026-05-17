import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { LikedArtist } from '../../../liked-artist.model';

@Component({
  selector: 'app-artist-chip',
  imports: [MatChipsModule, MatIconModule],
  templateUrl: './artist-chip.component.html',
  styleUrl: './artist-chip.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ArtistChipComponent {
  artist = input.required<LikedArtist>();
  deleted = output<number>();

  protected onRemove(): void {
    this.deleted.emit(this.artist().id);
  }
}
